# RAIDO

اپلیکیشن اندروید برای رانندگان اسنپ: تحلیل سریع اینکه درخواست سفر به‌صرفه است یا نه.

## نسخه
**2.3.12** · versionCode **132**

### تغییرات 2.3.12
- یکتاسازی پای‌های تکراری Accessibility (فیکس دو برابر شدن مسافت/زمان)
- تشخیص ROUND_TRIP از لوپ / بازگشت
- توقف «X تا Y دقیقه» جدا از مدل سفر
- بیلد release با **R8** (minify + shrink)

## بیلد
- GitHub → **Actions → Build RAIDO APK → Run workflow**
- یا تگ: `git tag v2.3.12 && git push --tags`
- محلی: `./gradlew assembleRelease` (JDK 17، SDK 34)

## امضا
Secrets: `RAIDO_KEYSTORE_BASE64`, `RAIDO_KEYSTORE_PASSWORD`, `RAIDO_KEY_ALIAS`, `RAIDO_KEY_PASSWORD`

> نقشه `.mbtiles` داخل APK نیست؛ پرمیوم آپلود می‌کند.
