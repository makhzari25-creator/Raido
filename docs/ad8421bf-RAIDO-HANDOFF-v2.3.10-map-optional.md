# RAIDO HANDOFF v2.3.10 — نقشه اختیاری جدا از APK

## هدف
نقشه داخل APK نباشد؛ محدوده فقط با آپلود `.mbtiles` توسط پرمیوم فعال شود؛ بدون نقشه/بدون پرمیوم تحلیل قیمتی و ساعتی عادی بماند و کرش نشود.

## انجام‌شده (سورس)
- حذف `mazandaran.mbtiles` از assets
- `ActivityZoneStore.isZoneActiveForAnalysis()` = enabled + premium + mapFile + center
- `GeoLayer`: فقط اگر `azReady` → مسیر محدوده؛ وگرنه **ShiftAnalyzer شهری / تحلیل عادی**
- `ActivityZoneEvaluator`: بدون نقشه/پرمیوم → UNKNOWN، verd دست‌نخورده
- `ActivityZoneActivity`: آپلود نقشه، WebView فقط بعد از وجود فایل
- versionCode **130** · versionName **2.3.10**

## رفتار
| کاربر | محدوده روی verd |
|--------|------------------|
| غیرپرمیوم | خیر — تحلیل عادی |
| پرمیوم بدون نقشه | خیر — تحلیل عادی |
| پرمیوم + نقشه + مرکز + سوییچ روشن | بله — داخل/خارج + H_شیفت |

## بیلد
هنوز بیلد گرفته نشده. **حتماً با `./gradlew assembleRelease` کامل** روی سیستم ≥۴GB RAM — نه بسته‌بندی دستی dex.

## فایل
- `RAIDO-source-v2.3.10.zip`
