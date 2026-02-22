# AegisLink

AegisLink is an Android app that intercepts web links (`http`/`https`) and helps users avoid malicious URLs before they open in a browser.

## Core capabilities
- Intercepts links through a dedicated `InterceptActivity`.
- Sanitizes and classifies URLs (trusted, unknown, blocked).
- Uses local whitelist/blacklist management.
- Supports optional VirusTotal checks for unknown links.
- Stores app settings and stats locally using Room.

## Tech stack
- Kotlin + Android SDK
- Gradle (Kotlin DSL)
- Room (local database)
- Retrofit + OkHttp + Moshi (networking)
- AndroidX Security Crypto (secure preferences)

## Requirements
- Android Studio (recent stable version)
- JDK 17
- Android SDK 35
- Minimum Android version: API 26

## Quick start
```bash
./gradlew clean testDebugUnitTest
./gradlew assembleDebug
```

Install debug APK:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Documentation
- `docs/README.md`: documentation index and what each document covers.
- `docs/SETUP.md`: local setup, build/test commands, and troubleshooting.
- `docs/ARCHITECTURE.md`: app layers, data flow, and major components.
- `RELEASE.md`: release signing, build, verification, and Play Console runbook.

## Project structure
```text
app/src/main/java/com/aegislink/
  data/        # Room entities/DAO/repositories
  domain/      # URL sanitizer + classifier logic
  network/     # VirusTotal API integration
  ui/          # Activities and view models
  util/        # App container and browser forwarding helpers
```

## Security notes
- Do not commit API keys, signing keys, or keystore files.
- Prefer defining sensitive values in local Gradle properties.
- Keep `local.properties` and private tokens out of version control.
