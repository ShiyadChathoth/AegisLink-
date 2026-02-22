# Architecture

## High-level flow
1. User opens a web link.
2. Android routes the link to `InterceptActivity`.
3. URL is sanitized and classified.
4. App decides to block, allow, or request deeper scan.
5. If needed, VirusTotal lookup is performed.
6. User proceeds or cancels; stats and lists are updated.

## Layers
- `ui/`: screen logic (`MainActivity`, `SettingsActivity`, `InterceptActivity`, etc.).
- `domain/`: URL sanitation and classification models/rules.
- `data/`: Room entities/DAOs and repositories for local state.
- `network/`: VirusTotal API service/repository.
- `util/`: wiring and browser forwarding helpers.

## Key components
- `InterceptActivity`: entry point for intercepted links.
- `InterceptViewModel`: orchestrates classification and actions.
- `UrlSanitizer`: strips tracking/query noise from URLs.
- `UrlClassifier`: determines trusted/unknown/blocked status.
- `AegisDatabase`: stores whitelist, blacklist, settings, and stats.

## Storage and security
- Room persists app data locally.
- Sensitive app settings use encrypted/shared preference patterns.
- App backup is disabled in manifest to reduce data exposure.

## External integration
- VirusTotal is optional and driven by user-provided API key.
- Browser package can be configured for forwarding.
