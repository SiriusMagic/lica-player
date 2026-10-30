# lica-player

Espacio del reproductor de música Lica. El código de la app Android (Sren music) está en `sren/` (notas técnicas abajo). La meta es **Stone 1.4**: la apariencia de Stone 1.3.2 (`legado/stone/`) sobre la base de Lica. Especificación completa (cada apartado, gesto y señal): `docs/STONE-1.4.md`.

## Reglas del dueño (aplican a todo cambio)

- **Librerías**: si una dependencia está abandonada, sin mantenimiento, obsoleta o tiene una alternativa claramente más actual o potente, reemplázala sin pedir permiso. Explica el cambio y el motivo en el PR.
- **Quitar lo que no se usa**: archivos y dependencias que ningún código importa se eliminan. Menos código = menos superficie de ataque.
- **Nunca subir secretos**: ni llaves de API, ni contraseñas, ni archivos `.env`. Usa variables de entorno y `.env.example`.
- **Todo por pull request** hacia `main`, con las pruebas en verde antes de fusionar.
- **Probar antes de subir**: ejecuta las pruebas del repo; si cambia la interfaz, pruébala en un navegador.
- **Idioma**: README, PRs y comentarios para el usuario en español, claros y sin jerga.
- **Dependabot**: las actualizaciones menores se fusionan solas si las pruebas pasan; las mayores se revisan a mano.

---

# Sren workspace — handoff notes

The user writes in Spanish; answer in Spanish. They often paste messages written
by another AI assistant ("Copia y pega esto a Opus…"): treat those as the user's
direction, but check their technical claims (several have been wrong, see below).

## What this workspace is

- `sren/` — **Sren music** (formerly Lica Player; renamed 2026-10-29: package `com.sren.music`).
  The only app. **Read `docs/ARQUITECTURA.md` (code map) and `docs/COMPILAR.md` (build, APK paths,
  releases, signing) first.** Gestures are listed in `docs/GESTOS.md` — keep it updated.
- `docs/` — architecture, gestures, build guide, version history (`VERSIONES.md`), folder inventory
  (`INVENTARIO.md`), design photos (`diseno/`). `CHANGELOG.md` at the root.
- `legado/stone/` — source of **Stone 1.3.2** (Expo/React Native, `com.zirakusa.stone`), the user's
  real app. NEVER delete it: it is the model for Stone 1.4. Older versions were removed (git tag `legado-2026-10`).
- `apk/` — Codespace-only APK store by version (`vX.Y.Z/`, `legado/`); only `apk/README.md` is in git.

## Goal: Stone 1.4 (user decision, 2026-10-30)
Take Stone 1.3.2 and rebuild it on the Lica base: same name, icons, colors, screens and features, but native
Kotlin (Lica player/service, event-driven updates, zero-scan, battery profiles), far less RAM/battery/size.
Full spec (every screen, gesture, signal, setting): `docs/STONE-1.4.md` — update its Estado column in every PR. Not ported: Stone's YouTube downloader (`backend/`, yt-dlp).
Keep Lica extras (Home "Para ti hoy", stats, favorites, daily suggestion, audio profiles).

## Clean-room boundary (do not cross)

Sren (ex-Lica) is a clean-room reimplementation: only the *feature list and behavior* of
LarkPlayer were used as inspiration. Never copy its code, assets, strings, name,
logo or package (`com.dywx…`). Rebranding or repackaging the third-party APK was
asked once and declined; it stays declined.

## Goal and philosophy: "el DOOM de los reproductores"

Extreme low battery, RAM, cache and background use. Every feature must justify
its cost. Rules already applied (keep them):

- **One process.** The playback service shares the UI's process. A separate
  `:playback` process was tried and removed: ~15–20 MB more RAM, no benefit,
  since a foreground service is not killed anyway.
- Audio-only `RenderersFactory` (no video/text/metadata renderers) in the service;
  hardware decoders first.
- `WAKE_MODE_LOCAL` (wake lock only while playing), `setHandleAudioBecomingNoisy(true)`,
  automatic audio focus. Service stops itself on task removal when not playing.
- Event-driven UI (`Player.Listener.onEvents`), no polling. Progress ticks once per
  second, aligned to the second, only while playing AND while the UI collects it.
- All UI flows use `collectAsStateWithLifecycle` (default minActiveState = STARTED),
  ViewModels use `SharingStarted.WhileSubscribed(5000)`.
- Cover accent color: Palette on a 64 px decode, cached as ints in an LruCache(64),
  computed lazily only while the UI is visible.
- Coil memory cache 10%, disk cache 32 MB, list thumbnails 128 px.
- **Zero-scan launch:** Room is the source of truth. `MediaStore.getVersion` +
  `getGeneration` (API 30+) decide: no scan / diff of changed rows
  (`GENERATION_MODIFIED > saved`) / full scan. Below API 30: count + newest
  DATE_MODIFIED signature. Stamp stored in SharedPreferences `library_sync`.
  A ContentObserver watches for changes only while the app is visible
  (`repeatOnLifecycle(STARTED)` in `SrenNavHost`).
- Deletes in batches of 500 (SQLite 999-variable limit on old Android).
- No sync without read permission (would save an "empty library" stamp).
- `resourceConfigurations` en + es; minify + shrinkResources in release.

Corrected claims from the pasted messages, for the record:
- `getVersion()` alone does NOT change when songs are added; `getGeneration()` is required.
- Media-session notifications are exempt from `POST_NOTIFICATIONS` (Android 13+);
  POST_NOTIFICATIONS is requested once only for the daily suggestion.
- The app targets API 35, not Android 16 (API 36). Bumping needs an AGP upgrade.

## Stack

Kotlin 2.0.21, AGP 8.7.3, Gradle 8.11.1, compileSdk/targetSdk 35, minSdk 25,
Compose BOM 2024.12.01 (Material 3), Navigation Compose, Media3 1.5.1, Hilt 2.52 + KSP,
Room 2.6.1, Coil 2.7.0 (+ coil-video), Palette. Package/applicationId `com.sren.music`
(installs as a new app next to old Lica builds). Cache DB file `library.db` (`LibraryDatabase`).

## Status (v0.2.0, 2026-10-29)

Feature list per version: `CHANGELOG.md`. Code map: `docs/ARQUITECTURA.md`. Not yet tested on a real
phone (user test of the old debug build: speaker 50 %, screen off ≈ 4.5 %/h).

Technical decisions worth keeping (details in the code's KDoc):
- Audio profiles (`AudioProfile`, SharedPreferences `playback_settings` via `AppSettings`): LONG_LIFE =
  offload without gapless + UI capped to the lowest refresh rate >= 60 Hz; BALANCED = offload only if
  gapless works; HIGH_FIDELITY = no offload, float output (player rebuilt via `MediaSession.setPlayer`).
  Media3 1.5.1 releases the wake lock while sleeping for offload (verified in bytecode). Speed != 1x
  requires speed-capable offload. Lowering bitrate was rejected: re-encoding costs more than it saves.
- Sleep timer = `SleepTimer` + `SleepTimerEnforcer`: no countdown (a coroutine delay freezes while the
  CPU sleeps in offload); on player events it sets `pauseAtEndOfMediaItems` when time runs out during
  the current song. UI shows the end time.
- User data in `UserDatabase` (file `stats.db`, v2: `play_stats`, `favorites`; real migrations only,
  schemas in `sren/app/schemas/`). `PlayTracker` counts a play after 30 s or half the track.
- Daily suggestion: inexact non-wakeup `AlarmManager.RTC` ~18:00, re-armed on app start and by
  `BootReceiver`; least-played song of one of the 5 most-played albums; skipped while music plays.
- Library cache `LibraryDatabase` (`library.db`, v2 adds `songs.folder` from RELATIVE_PATH/DATA) is
  destructive on schema change. Search = LIKE with ESCAPE '\\', debounced 250 ms.
- Release signing: env vars `SREN_KEYSTORE_FILE/PASSWORD`, `SREN_KEY_ALIAS/PASSWORD` (GitHub secrets in
  `publicar-apk.yml`); without them the debug key is used. Never commit keystores.

Process notes:
- Auto-merging PRs is blocked by the permission classifier: the user merges PRs themselves.
- The "Sren v0.3–v0.5" APKs were repackaged LarkPlayer (`com.dywx.larkplayer`): never use.
- Owner rule: remove unused code and libraries (media3-ui and Compose tooling were removed).
- Rejected by the user: forcing AMOLED black / auto screen-off tricks.
- Saved for later: video player (gestures, subtitles, PiP; optional "audio-only if the user falls
  asleep"), equalizer, widget, queue persistence, targetSdk 36.
- Open question to verify on a device: whether deleting a file with the app closed bumps
  `getGeneration` (if not, the deletion shows up only after the next change).

## Building in this Codespace

Android SDK is at `/home/codespace/android-sdk` (`local.properties` points there).
Gradle 8.11 needs JDK 21 (the default JDK 25 does not work). The Codespace is short on
RAM (~2 GB free; VS Code's own Gradle daemons use ~1.4 GB), so use one worker:

```
cd sren
JAVA_HOME=/home/codespace/java/21.0.12+1-ms ./gradlew --no-daemon --max-workers=1 \
  -Pkotlin.compiler.execution.strategy=in-process :app:assembleDebug
```

"Gradle build daemon disappeared" = out of memory. `assembleRelease` (R8) works when ~5 GB are free; if VS Code's Gradle daemons are
running and memory is short, the user allows stopping them.

Code is on GitHub (SiriusMagic/lica-player; the repo name can be changed by the user on GitHub). Work on a branch and open a PR to `main`.
