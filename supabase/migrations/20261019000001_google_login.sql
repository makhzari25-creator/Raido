-- ورود با گوگل در کنار تلگرام + سخت‌گیری روی تست رایگان
alter table public.users alter column phone drop not null;
alter table public.users add column if not exists google_sub    text;
alter table public.users add column if not exists email         text;
alter table public.users add column if not exists email_norm    text;
alter table public.users add column if not exists contact_phone text;
create unique index if not exists users_google_sub_uidx on public.users (google_sub) where google_sub is not null;
create index if not exists users_email_norm_idx on public.users (email_norm) where email_norm is not null;
create index if not exists users_contact_phone_idx on public.users (contact_phone) where contact_phone is not null;

alter table public.login_sessions add column if not exists via           text not null default 'telegram';
alter table public.login_sessions add column if not exists contact_phone text;
alter table public.login_sessions add column if not exists contact_email text;
alter table public.login_sessions add column if not exists ip            text;
create index if not exists login_sessions_ip_idx on public.login_sessions (ip, created_at);

-- تست ۷ روزه: یک بار برای هر کاربر، هر دستگاه، هر ایمیل (نرمال‌شده) و هر شمارهٔ تماس
create or replace function public.start_trial(p_user uuid, p_fp text)
returns jsonb language plpgsql security definer set search_path = public as $$
declare s public.subscriptions; me public.users;
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
  select * into me from public.users where id = p_user;
  if me.email_norm is not null and exists (
       select 1 from public.users u join public.subscriptions x on x.user_id = u.id
        where u.id <> p_user and u.email_norm = me.email_norm and x.trial_used) then
    return jsonb_build_object('ok', false, 'error', 'trial_used');
  end if;
  if me.contact_phone is not null and exists (
       select 1 from public.users u join public.subscriptions x on x.user_id = u.id
        where u.id <> p_user and x.trial_used
          and (u.phone = me.contact_phone or u.contact_phone = me.contact_phone)) then
    return jsonb_build_object('ok', false, 'error', 'trial_used');
  end if;
  insert into public.trial_devices (device_fp, user_id) values (p_fp, p_user);
  update public.subscriptions
     set plan = 'TRIAL_7', is_premium = false, expires_at = now() + interval '7 days',
         trial_used = true, trial_started_at = now(), updated_at = now()
   where user_id = p_user;
  return jsonb_build_object('ok', true);
end $$;
