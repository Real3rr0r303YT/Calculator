# Rechner · Nothing Edition

Minimalistischer Android-Taschenrechner im Nothing-OS-Look — Jetpack Compose & Material 3.

- Dot-Matrix-Display (eigene 5×7-LED-Schrift, live gezeichnet)
- Mattschwarz, Clean White, Nothing-Rot `#D71921`
- Runde Glas-Tasten, Dot-Grid-Hintergrund, Haptik
- `+ − × ÷`, `%`, `( )`, `±`, Live-Zwischenergebnis, BigDecimal-Genauigkeit
- Keine Berechtigungen, kein Internet, kein Tracking

## Build

```bash
./gradlew testDebugUnitTest assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Voraussetzungen: JDK 17, Android SDK (Platform 35).

## CI/CD

`.github/workflows/build.yml` baut bei jedem Push auf `main` und bei PRs Debug + Release-APK (Artifact `Rechner-Nothing-APK`).
Ein Tag `v*` erstellt automatisch ein GitHub-Release mit APK:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

Optional eigener Release-Key (sonst Debug-Signatur) — Repo-Secrets:

| Secret | Inhalt |
|---|---|
| `KEYSTORE_BASE64` | `base64 -w0 release.jks` |
| `KEYSTORE_PASSWORD` | Keystore-Passwort |
| `KEY_ALIAS` | Key-Alias |
| `KEY_PASSWORD` | Key-Passwort |

```bash
keytool -genkeypair -v -keystore release.jks -alias rechner -keyalg RSA -keysize 4096 -validity 10000
```

## Support

[![Buy Me a Coffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-3rr0r303YT-D71921?logo=buymeacoffee&logoColor=white)](https://buymeacoffee.com/3rr0r303YT)

## Lizenz

MIT © 3rr0r

---

<p align="center"><b>Made by <a href="https://github.com/Real3rr0r303YT">Real3rr0r303YT</a></b></p>
