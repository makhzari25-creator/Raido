-- v3.1: مسدودسازی کاربر، لغو اشتراک، دلیل پایان نشست، قفل تلاش ناموفق ادمین.

alter table public.users
  add column if not exists banned     boolean not null default false,
  add column if not exists ban_reason text,
  add column if not exists banned_at  timestamptz;

-- نشست‌های پایان‌یافته (فقط هش توکن) تا دستگاه قدیمی دلیل خروج را بفهمد.
create table if not exists public.ended_sessions (
  token_hash text primary key,
  reason     text not null check (reason in ('other_device','banned','logout','revoked')),
  ended_at   timestamptz not null default now()
);
create index if not exists ended_sessions_at_idx on public.ended_sessions (ended_at);

-- تلاش‌های ناموفق ورود ادمین (برای قفل موقت)
create table if not exists public.admin_fail (
  id bigint generated always as identity primary key,
  ip text not null,
  at timestamptz not null default now()
);
create index if not exists admin_fail_ip_at_idx on public.admin_fail (ip, at);

alter table public.ended_sessions enable row level security;
alter table public.admin_fail     enable row level security;
revoke all on public.ended_sessions from anon, authenticated;
revoke all on public.admin_fail     from anon, authenticated;

-- لغو فوری اشتراک (تاریخچهٔ trial_used حفظ می‌شود)
create or replace function public.revoke_plan(p_user uuid)
returns void language plpgsql security definer set search_path = public as $$
begin
  insert into public.subscriptions (user_id) values (p_user) on conflict do nothing;
  update public.subscriptions
     set plan = 'NONE', is_premium = false, expires_at = now(), updated_at = now()
   where user_id = p_user;
end $$;

-- مسدود/آزاد کردن کاربر؛ مسدودسازی همهٔ نشست‌های او را می‌بندد
create or replace function public.set_ban(p_user uuid, p_banned boolean, p_reason text)
returns void language plpgsql security definer set search_path = public as $$
begin
  update public.users
     set banned = p_banned,
         ban_reason = case when p_banned then left(p_reason, 200) else null end,
         banned_at  = case when p_banned then now() else null end
   where id = p_user;
  if p_banned then
    insert into public.ended_sessions (token_hash, reason)
      select token_hash, 'banned' from public.sessions where user_id = p_user
      on conflict (token_hash) do update set reason = 'banned', ended_at = now();
    delete from public.sessions where user_id = p_user;
  end if;
end $$;

-- ورود جدید: نشست‌های قبلی با دلیل other_device بسته می‌شوند
create or replace function public.end_other_sessions(p_user uuid)
returns void language plpgsql security definer set search_path = public as $$
begin
  insert into public.ended_sessions (token_hash, reason)
    select token_hash, 'other_device' from public.sessions where user_id = p_user
    on conflict (token_hash) do update set reason = 'other_device', ended_at = now();
  delete from public.sessions where user_id = p_user;
  delete from public.ended_sessions where ended_at < now() - interval '30 days';
  delete from public.admin_fail where at < now() - interval '1 day';
end $$;

revoke all on function public.revoke_plan(uuid)               from public, anon, authenticated;
revoke all on function public.set_ban(uuid,boolean,text)      from public, anon, authenticated;
revoke all on function public.end_other_sessions(uuid)        from public, anon, authenticated;
grant execute on function public.revoke_plan(uuid)            to service_role;
grant execute on function public.set_ban(uuid,boolean,text)   to service_role;
grant execute on function public.end_other_sessions(uuid)     to service_role;

-- آمار: تعداد مسدودها هم اضافه شد
create or replace function public.admin_stats()
returns jsonb language sql security definer set search_path = public as $$
  select jsonb_build_object(
    'users_total',   (select count(*) from public.users),
    'new_today',     (select count(*) from public.users where created_at >= date_trunc('day', now())),
    'active_1d',     (select count(*) from public.users where last_seen >= now() - interval '1 day'),
    'active_7d',     (select count(*) from public.users where last_seen >= now() - interval '7 days'),
    'active_30d',    (select count(*) from public.users where last_seen >= now() - interval '30 days'),
    'premium_active',(select count(*) from public.subscriptions where is_premium and expires_at > now()),
    'paid_active',   (select count(*) from public.subscriptions where plan not in ('NONE','TRIAL_7') and expires_at > now()),
    'trial_active',  (select count(*) from public.subscriptions where plan = 'TRIAL_7' and expires_at > now()),
    'analyses_7d',   (select coalesce(sum(analyses),0) from public.usage_daily where day >= current_date - 6),
    'accepted_7d',   (select coalesce(sum(accepted),0) from public.usage_daily where day >= current_date - 6),
    'codes_unused',  (select count(*) from public.activation_codes where redeemed_at is null and valid_until > now()),
    'banned',        (select count(*) from public.users where banned),
    'versions',      (select coalesce(jsonb_object_agg(v, n), '{}'::jsonb) from
                       (select coalesce(app_version,'?') v, count(*) n from public.users group by 1) t)
  );
$$;
revoke all on function public.admin_stats() from public, anon, authenticated;
grant execute on function public.admin_stats() to service_role;

-- سقف مصرف روزانهٔ ژئوکد برای هر کاربر
create table if not exists public.geocode_usage (
  user_id uuid not null references public.users(id) on delete cascade,
  day     date not null,
  n       int  not null default 0,
  primary key (user_id, day)
);
alter table public.geocode_usage enable row level security;
revoke all on public.geocode_usage from anon, authenticated;

create or replace function public.geocode_hit(p_user uuid, p_day date)
returns int language sql security definer set search_path = public as $$
  insert into public.geocode_usage (user_id, day, n) values (p_user, p_day, 1)
  on conflict (user_id, day) do update set n = public.geocode_usage.n + 1
  returning n;
$$;
revoke all on function public.geocode_hit(uuid,date) from public, anon, authenticated;
grant execute on function public.geocode_hit(uuid,date) to service_role;
