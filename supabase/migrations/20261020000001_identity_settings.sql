-- هویت یکپارچهٔ شماره‌محور: گوگل یا تلگرام، و تنظیمات قابل‌تغییر از پنل (لینک ویدیوی آموزشی)
alter table public.users add column if not exists phone_verified boolean not null default true;
alter table public.users add column if not exists signup_via     text    not null default 'telegram';
alter table public.users add column if not exists last_via       text    not null default 'telegram';

create table if not exists public.app_settings (
  key        text primary key,
  value      text not null default '',
  updated_at timestamptz not null default now()
);
alter table public.app_settings enable row level security;
insert into public.app_settings (key, value) values ('tutorial_url', '') on conflict do nothing;
