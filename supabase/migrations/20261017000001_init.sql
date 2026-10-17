-- RAIDO backend: users (phone-unique, verified via Telegram), subscriptions, codes, usage.
-- همهٔ دسترسی‌ها فقط از طریق Edge Functions (service_role) است؛ RLS روشن و بدون policy.


create table public.users (
  id           uuid primary key default gen_random_uuid(),
  phone        text not null unique check (phone ~ '^09[0-9]{9}$'),
  telegram_id  bigint,
  created_at   timestamptz not null default now(),
  last_seen    timestamptz,
  app_version  text,
  device_model text
);
create index users_last_seen_idx on public.users (last_seen);

create table public.login_sessions (
  code              text primary key,
  device_fp         text not null,
  telegram_chat_id  bigint,
  telegram_user_id  bigint,
  user_id           uuid references public.users(id) on delete cascade,
  status            text not null default 'pending'
                      check (status in ('pending','awaiting_contact','verified','consumed')),
  created_at        timestamptz not null default now(),
  expires_at        timestamptz not null default now() + interval '10 minutes'
);
create index login_sessions_tg_idx on public.login_sessions (telegram_user_id, status);
create index login_sessions_fp_idx on public.login_sessions (device_fp, created_at);

-- توکن دستگاه (فقط هش ذخیره می‌شود). هر کاربر یک دستگاه فعال.
create table public.sessions (
  token_hash text primary key,
  user_id    uuid not null references public.users(id) on delete cascade,
  device_fp  text not null,
  created_at timestamptz not null default now(),
  last_used  timestamptz
);
create index sessions_user_idx on public.sessions (user_id);

create table public.subscriptions (
  user_id          uuid primary key references public.users(id) on delete cascade,
  plan             text not null default 'NONE',
  is_premium       boolean not null default false,
  expires_at       timestamptz,
  trial_used       boolean not null default false,
  trial_started_at timestamptz,
  updated_at       timestamptz not null default now()
);

create table public.trial_devices (
  device_fp  text primary key,
  user_id    uuid references public.users(id) on delete set null,
  created_at timestamptz not null default now()
);

create table public.activation_codes (
  code_hash   text primary key,
  plan        text not null,
  days        int  not null check (days > 0),
  is_premium  boolean not null,
  created_at  timestamptz not null default now(),
  valid_until timestamptz not null,
  note        text,
  redeemed_by uuid references public.users(id) on delete set null,
  redeemed_at timestamptz
);

create table public.redeem_log (
  id      bigserial primary key,
  user_id uuid not null references public.users(id) on delete cascade,
  ok      boolean not null,
  at      timestamptz not null default now()
);
create index redeem_log_idx on public.redeem_log (user_id, at);

create table public.usage_daily (
  user_id     uuid not null references public.users(id) on delete cascade,
  day         date not null,
  analyses    int  not null default 0,
  accepted    int  not null default 0,
  app_version text,
  primary key (user_id, day)
);
create index usage_daily_day_idx on public.usage_daily (day);

-- RLS: روشن، بدون policy → anon/authenticated هیچ دسترسی ندارند.
alter table public.users            enable row level security;
alter table public.login_sessions   enable row level security;
alter table public.sessions         enable row level security;
alter table public.subscriptions    enable row level security;
alter table public.trial_devices    enable row level security;
alter table public.activation_codes enable row level security;
alter table public.redeem_log       enable row level security;
alter table public.usage_daily      enable row level security;

revoke all on all tables    in schema public from anon, authenticated;
revoke all on all sequences in schema public from anon, authenticated;

-- اعمال پلن (تمدید: از max(انقضای فعلی، الان) ادامه می‌دهد)
create or replace function public.apply_plan(p_user uuid, p_plan text, p_days int, p_premium boolean)
returns timestamptz language plpgsql security definer set search_path = public as $$
declare base timestamptz; s public.subscriptions; new_exp timestamptz;
begin
  insert into public.subscriptions (user_id) values (p_user) on conflict do nothing;
  select * into s from public.subscriptions where user_id = p_user for update;
  base := greatest(coalesce(s.expires_at, now()), now());
  new_exp := base + make_interval(days => p_days);
  update public.subscriptions
     set plan = p_plan, is_premium = p_premium, expires_at = new_exp, updated_at = now()
   where user_id = p_user;
  return new_exp;
end $$;

create or replace function public.redeem_code(p_user uuid, p_hash text)
returns jsonb language plpgsql security definer set search_path = public as $$
declare c public.activation_codes;
begin
  select * into c from public.activation_codes where code_hash = p_hash for update;
  if not found then
    insert into public.redeem_log(user_id, ok) values (p_user, false);
    return jsonb_build_object('ok', false, 'error', 'invalid');
  end if;
  if c.redeemed_at is not null then
    insert into public.redeem_log(user_id, ok) values (p_user, false);
    return jsonb_build_object('ok', false, 'error', 'used');
  end if;
  if c.valid_until < now() then
    return jsonb_build_object('ok', false, 'error', 'expired');
  end if;
  perform public.apply_plan(p_user, c.plan, c.days, c.is_premium);
  update public.activation_codes set redeemed_by = p_user, redeemed_at = now() where code_hash = p_hash;
  insert into public.redeem_log(user_id, ok) values (p_user, true);
  return jsonb_build_object('ok', true);
end $$;

-- تست ۷ روزه: یک بار برای هر کاربر و هر دستگاه
create or replace function public.start_trial(p_user uuid, p_fp text)
returns jsonb language plpgsql security definer set search_path = public as $$
declare s public.subscriptions;
begin
  insert into public.subscriptions (user_id) values (p_user) on conflict do nothing;
  select * into s from public.subscriptions where user_id = p_user for update;
  if s.trial_used then return jsonb_build_object('ok', false, 'error', 'trial_used'); end if;
  if s.expires_at is not null and s.expires_at > now() then
    return jsonb_build_object('ok', false, 'error', 'already_active');
  end if;
  if exists (select 1 from public.trial_devices where device_fp = p_fp) then
    return jsonb_build_object('ok', false, 'error', 'trial_used');
  end if;
  insert into public.trial_devices (device_fp, user_id) values (p_fp, p_user);
  update public.subscriptions
     set plan = 'TRIAL_7', is_premium = false, expires_at = now() + interval '7 days',
         trial_used = true, trial_started_at = now(), updated_at = now()
   where user_id = p_user;
  return jsonb_build_object('ok', true);
end $$;

-- آمار روزانه (idempotent: بزرگ‌ترین مقدار نگه داشته می‌شود)
create or replace function public.put_usage(p_user uuid, p_day date, p_analyses int, p_accepted int, p_version text)
returns void language sql security definer set search_path = public as $$
  insert into public.usage_daily (user_id, day, analyses, accepted, app_version)
  values (p_user, p_day, greatest(p_analyses,0), greatest(p_accepted,0), p_version)
  on conflict (user_id, day) do update
    set analyses = greatest(public.usage_daily.analyses, excluded.analyses),
        accepted = greatest(public.usage_daily.accepted, excluded.accepted),
        app_version = excluded.app_version;
$$;

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
    'versions',      (select coalesce(jsonb_object_agg(v, n), '{}'::jsonb) from
                       (select coalesce(app_version,'?') v, count(*) n from public.users group by 1) t)
  );
$$;

revoke all on function public.apply_plan(uuid,text,int,boolean)       from public, anon, authenticated;
revoke all on function public.redeem_code(uuid,text)                  from public, anon, authenticated;
revoke all on function public.start_trial(uuid,text)                  from public, anon, authenticated;
revoke all on function public.put_usage(uuid,date,int,int,text)       from public, anon, authenticated;
revoke all on function public.admin_stats()                           from public, anon, authenticated;
grant execute on function public.apply_plan(uuid,text,int,boolean)    to service_role;
grant execute on function public.redeem_code(uuid,text)               to service_role;
grant execute on function public.start_trial(uuid,text)               to service_role;
grant execute on function public.put_usage(uuid,date,int,int,text)    to service_role;
grant execute on function public.admin_stats()                        to service_role;
