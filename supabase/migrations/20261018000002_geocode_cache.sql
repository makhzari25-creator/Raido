-- کش اشتراکی ژئوکد: فقط هش آدرس نرمال‌شده + مختصات (خود آدرس ذخیره نمی‌شود).
create table if not exists public.geocode_cache (
  k text primary key,
  x double precision not null,
  y double precision not null,
  created_at timestamptz not null default now()
);
alter table public.geocode_cache enable row level security;
revoke all on public.geocode_cache from anon, authenticated;
create index if not exists geocode_cache_created_idx on public.geocode_cache (created_at);
