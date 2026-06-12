# 🧆 Falafel Rush — Street Food Empire

משחק ארקייד-טייקון מהיר לאנדרואיד: נהלו דוכן פלאפל, הרכיבו פיתות במהירות, שמרו על סבלנות הלקוחות, שדרגו את העסק ובנו אימפריית פלאפל עולמית.

**100% אופליין** — בלי הרשאות, בלי רשת, בלי איסוף מידע.

## Features
- 6 ארכיטיפים של לקוחות מצוירים ב-Canvas עם הבעות פנים דינמיות
- גרירה/הקשה של מרכיבים, אנימציות serve/exit, אינדיקטור "+🪙" מרחף
- שעת עומס עם vignette זהוב פועם + אזעקה
- מנוע אודיו סינתטי (AudioTrack) — אפס קבצי מדיה
- קושי מתקדם: סבלנות יורדת 5% ליום, הזמנות כפולות, עומס גובר
- תפריט השהיה + הדרכת פתיחה + ביקורות עבריות מקומיות
- שמירה מלאה ב-Room (מטבעות, ימים, שדרוגים, ביקורות)

## Build

**Prerequisites:** JDK 17+ (Android Studio JBR works), Android SDK 36

```powershell
.\gradlew.bat assembleDebug      # debug APK
.\gradlew.bat bundleRelease      # Play Store AAB
```

`local.properties` needs `sdk.dir` pointing at your Android SDK.

## Release signing

Create an upload keystore (one time):

```powershell
keytool -genkeypair -v -keystore upload-keystore.jks -keyalias upload -keyalg RSA -keysize 2048 -validity 10000
```

Then create `keystore.properties` in the project root (gitignored):

```properties
storeFile=upload-keystore.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=upload
keyPassword=YOUR_KEY_PASSWORD
```

`bundleRelease` will pick it up automatically. Without it, release artifacts build unsigned.

## Play Store notes
- App is fully offline, zero permissions → Data Safety form: "No data collected or shared"
- A privacy policy URL is still required on the listing — a one-paragraph static page stating no data is collected is enough
- Known issue: unit tests fail to execute on Windows hosts with non-ASCII project paths (Gradle test-worker limitation); they run fine on CI/Linux
