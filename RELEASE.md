# AegisLink Public Release Runbook

## 1. Configure local signing (one-time)
Set these in `~/.gradle/gradle.properties`:

```
AEGIS_RELEASE_STORE_FILE=/absolute/path/to/release-keystore.jks
AEGIS_RELEASE_STORE_PASSWORD=*****
AEGIS_RELEASE_KEY_ALIAS=*****
AEGIS_RELEASE_KEY_PASSWORD=*****
```

If you do not have a keystore yet:

```
keytool -genkeypair -v \
  -keystore aegis-release.jks \
  -alias aegis \
  -keyalg RSA -keysize 4096 -validity 10000
```

## 2. Clean and verify
```
./gradlew clean testDebugUnitTest lintDebug
```

## 3. Build release artifact
APK:
```
./gradlew assembleRelease
```

App Bundle (Play Store preferred):
```
./gradlew bundleRelease
```

## 4. Verify release artifact
Local install test:
```
adb install -r app/build/outputs/apk/release/app-release.apk
```

Verify:
- link interception works for `http` and `https`
- unknown dialog buttons work (`Proceed`, `Scan`, `Cancel`)
- blacklist blocks, whitelist auto-proceeds
- login + manual VT key paste works

## 5. Prepare Play Console assets (manual)
- app icon (512x512)
- feature graphic (1024x500)
- screenshots (phone + optional tablet)
- privacy policy URL
- app description (short + full)
- data safety form answers

## 6. Upload
- create app in Play Console
- upload `app-release.aab` from:
  `app/build/outputs/bundle/release/app-release.aab`
- complete store listing + content rating + target audience + data safety
- submit for review

## 7. Post-release
- bump `versionCode` and `versionName` for each update
- keep mapping files from each release build
- rotate any backend tokens if they were exposed during testing
