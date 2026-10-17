# RAIDO

اپلیکیشن اندروید برای رانندگان اسنپ: تحلیل سریع اینکه درخواست سفر به‌صرفه است یا نه.

## نسخه
**3.1.0** · versionCode **143**

### تغییرات 3.1.0
- شمارش «پذیرفته‌شده» فقط یک‌بار برای هر سفر (رفع شمارش دوباره)
- همگام‌سازی کم‌هزینه از مسیر تحلیل و بازگشت به برنامه (قفل اشتباه کاربر آنلاین رخ نمی‌دهد)
- خروج از حساب (در صفحهٔ اشتراک) + باطل‌شدن نشست سمت سرور
- مسدودسازی کاربر و لغو اشتراک از پنل ادمین؛ پیام روشن هنگام ورود از دستگاه دیگر
- ژئوکد نشان از طریق سرور (کلید دیگر داخل APK نیست؛ فقط پرمیوم فعال)
- قفل موقت تلاش ناموفق ورود ادمین

## بیلد
- GitHub → **Actions → Build RAIDO APK → Run workflow**
- یا تگ: `git tag v3.1.0 && git push --tags`
- محلی: `./gradlew assembleRelease` (JDK 17، SDK 34)

## امضا
Secrets: `RAIDO_KEYSTORE_BASE64`, `RAIDO_KEYSTORE_PASSWORD`, `RAIDO_KEY_ALIAS`, `RAIDO_KEY_PASSWORD`

> نقشه `.mbtiles` داخل APK نیست؛ پرمیوم آپلود می‌کند.

## بک‌اند (Supabase)
- ورود با ربات تلگرام (شمارهٔ یونیک) · لایسنس و تست ۷ روزه سمت سرور · آمار استفاده.
- کد سرور: `supabase/` (migration + Edge Functions). Deploy خودکار با `Supabase Deploy`.
- راه‌اندازی اولیه: Secrets (`SUPABASE_ACCESS_TOKEN`, `SUPABASE_DB_PASSWORD`, `TELEGRAM_BOT_TOKEN`, `ADMIN_KEY`) ← Deploy ← Bootstrap ← Smoke Test.
- پنل ادمین: پوشهٔ `admin/` (روی GitHub Pages) — ساخت کد، اعطا، مسدودسازی، لغو اشتراک.
- کلید نشان: Secret سرور `NESHAN_KEY` (workflow ‏`Set Neshan Key`)؛ اپ فقط تابع `geocode` را صدا می‌زند.
- هیچ کلید یا الگوریتم تولید کدی داخل APK نیست؛ وضعیت اشتراک با RSA امضا و در اپ با کلید عمومی تأیید می‌شود.
