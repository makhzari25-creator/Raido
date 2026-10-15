# RAIDO

اپلیکیشن اندروید برای رانندگان اسنپ: تحلیل سریع (زیر ۲ ثانیه) اینکه درخواست سفر به‌صرفه است یا نه.

## بیلد
- GitHub → **Actions → Build RAIDO APK → Run workflow**؛ فایل APK در Artifacts قرار می‌گیرد.
- یا تگ بزنید (`git tag v2.3.10 && git push --tags`) تا APK در Releases منتشر شود.
- محلی: `./gradlew assembleRelease` (JDK 17، Android SDK 34).

## امضای release (اختیاری)
بدون تنظیم، با کلید debug امضا می‌شود. برای امضای ثابت، در Settings → Secrets → Actions اضافه کنید:
`RAIDO_KEYSTORE_BASE64`, `RAIDO_KEYSTORE_PASSWORD`, `RAIDO_KEY_ALIAS`, `RAIDO_KEY_PASSWORD`

> نقشه (`.mbtiles`) داخل APK نیست و فقط توسط کاربر پرمیوم آپلود می‌شود.
