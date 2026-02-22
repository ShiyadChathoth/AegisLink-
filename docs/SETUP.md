# Local Setup

## Prerequisites
- Android Studio (latest stable recommended)
- JDK 17
- Android SDK Platform 35
- `adb` for local install/testing

## Clone and open
1. Clone the repository.
2. Open the project root in Android Studio.
3. Let Gradle sync complete.

## Build and test
```bash
./gradlew clean testDebugUnitTest lintDebug
./gradlew assembleDebug
```

## Run on device/emulator
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Release signing (optional for release builds)
Add these to `~/.gradle/gradle.properties`:
```properties
AEGIS_RELEASE_STORE_FILE=/absolute/path/to/release-keystore.jks
AEGIS_RELEASE_STORE_PASSWORD=*****
AEGIS_RELEASE_KEY_ALIAS=*****
AEGIS_RELEASE_KEY_PASSWORD=*****
```

Then build:
```bash
./gradlew assembleRelease
```

Full release process is documented in `RELEASE.md`.

## Common issues
- Gradle/JDK mismatch: ensure project is using JDK 17.
- SDK missing: install Android API 35 from SDK Manager.
- Device install failure: check `adb devices` and USB debugging status.
