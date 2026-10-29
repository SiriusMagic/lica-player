# lica-player

Espacio del reproductor de música Lica. El código de la app Android (Sren music) está en `sren/` (notas técnicas abajo). También están las herramientas de apoyo: `herramientas/sarap` (asistente de navegación) y `herramientas/biblioteca` (cuaderno de Colab).

## Comandos

```bash
pip install pytest
pytest -q                       # pruebas de SARAP y del cuaderno
python herramientas/sarap/sarap.py   # SARAP interactivo
```

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

- `sren/` — **Sren music** (formerly Lica Player; renamed 2026-10-29 at the user's request:
  name, icon, package `com.sren.music`, dark+orange theme from the original Sren). The active project.
- `sren-music/` — reference copy of the user's original Sren code (not built; delete after the merge).
- `morluto-rea/` — the REA reverse-engineering CLI (github.com/INTECHNetworks/morluto-rea),
  used once to analyze a third-party app for inspiration.
- `base.apk`, `split_config.*.apk` — the third-party app that was analyzed (LarkPlayer).
- `apk/` — empty folder.

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
  (`repeatOnLifecycle(STARTED)` in `LicaNavHost`).
- Deletes in batches of 500 (SQLite 999-variable limit on old Android).
- No sync without read permission (would save an "empty library" stamp).
- `resourceConfigurations` en + es; minify + shrinkResources in release.

Corrected claims from the pasted messages, for the record:
- `getVersion()` alone does NOT change when songs are added; `getGeneration()` is required.
- Media-session notifications are exempt from `POST_NOTIFICATIONS` (Android 13+);
  the permission is not requested yet.
- The app targets API 35, not Android 16 (API 36). Bumping needs an AGP upgrade.

## Stack

Kotlin 2.0.21, AGP 8.7.3, Gradle 8.11.1, compileSdk/targetSdk 35, minSdk 25,
Compose BOM 2024.12.01 (Material 3), Navigation Compose, Media3 1.5.1, Hilt 2.52 + KSP,
Room 2.6.1, Coil 2.7.0 (+ coil-video), Palette. Package/applicationId `com.sren.music`
(installs as a new app next to old Lica builds). Cache DB file `library.db` (`LibraryDatabase`).

## Status (as of 2026-10-26)

**Phases 1–4 done and `assembleDebug` compiles with zero errors and warnings.**
`app/build/outputs/apk/debug/app-debug.apk` (21 MB, debug, unoptimized).
Not yet tested on a real phone.

Done:
- Data: `Song`, `Video` Room entities; `Album` derived by GROUP BY. `LicaDatabase` v1
  (destructive migration — it is only a cache). `MediaStoreScanner`, `LibraryStamp`,
  `MediaRepository` (sync plan + `watchLibrary`), Hilt modules.
- Audio: `PlaybackService` (MediaSessionService, foreground `mediaPlayback`),
  `PlaybackConnection` (shared MediaController), `PlayerViewModel`, `PaletteCache`.
  Default Media3 notification with artwork (Android 12+ tints it from the cover).
- UI: PermissionGate, 4-tab nav (Songs, Videos, Playlists, Settings), Songs list
  plays on tap, Videos list (tap does nothing yet), Playlists/Settings are
  placeholders, NowPlayingBar mini player with cover-color gradient
  (soft-blue fallback 0xFF1E3A5F / 0xFF2F6690).

Audio profiles (2026-10-28, Settings tab): `AudioProfile` LONG_LIFE / BALANCED (default) /
HIGH_FIDELITY in `PlaybackSettings` (SharedPreferences `playback_settings`). LONG_LIFE = offload
without gapless requirement + UI capped to the lowest refresh rate >= 60 Hz; BALANCED = offload only
if gapless works; HIGH_FIDELITY = no offload, float output (player rebuilt via `MediaSession.setPlayer`).
Verified in Media3 1.5.1 bytecode: the wake lock is released while sleeping for offload.
Lowering bitrate was rejected on purpose: re-encoding costs more battery than it saves.
Release is signed with the debug key for personal testing (2.8 MB, non-debuggable).
User test (debug build, speaker 50 %, screen off): ~4.5 %/h.
The user has another app (sleep timer, screen-off mode, more) to merge into Lica.

Daily suggestion (2026-10-28, Settings → Control avanzado, on by default): `PlayTracker` counts a
play after 30 s or half the track (event-driven, no timers) into a separate `stats.db`
(`StatsDatabase`, user data, NO destructive migration — write real migrations). `SuggestionPicker`
picks the least-played song of one of the 5 most-played albums (skips last 3 days and yesterday's
pick). Inexact non-wakeup `AlarmManager.RTC` ~18:00, re-armed on app start and by `BootReceiver`.
Silent channel, cover as large icon (Coil, 256 px), tap plays the whole album. Skipped while music
plays. POST_NOTIFICATIONS asked once on first launch (Android 13+).
Home tab (2026-10-29, start destination): suggestion card, recently played, most played,
favorites, recently added (LazyRows, 128 dp covers at 256 px). User data now in `UserDatabase`
(file still `stats.db`; v2 adds `favorites`; MIGRATION_1_2; schemas exported to `app/schemas/`).
`UserLibrary` joins user-data ids with the song cache. `SongActionsSheet` = long-press menu
(favorites now; playlists/hide later).

Sren merge (user's own earlier app, `sren-music/` = reference copy, not built): port in phases —
1 Home (done), 2 full player (done: route `player`, bars hidden; queue built only while its sheet
is open; speed != 1x requires speed-capable offload). Sleep timer = `SleepTimer` + `SleepTimerEnforcer`:
no delay/countdown (a coroutine delay freezes while the CPU sleeps in offload); on player events it
sets `pauseAtEndOfMediaItems` when the time runs out during the current song; UI shows the end time. 3 Library (done): tabs Home/Library/Search/Settings; Library sub-tabs
Songs (sort in memory with Collator, saved in AppSettings), Favorites, Folders (songs.folder from
RELATIVE_PATH/DATA, LibraryDatabase v2), Artists, Albums, Videos; route `collection/{kind}/{key}`;
Search = LIKE with ESCAPE '\\', debounced 250 ms. 4 playlists (in UserDatabase; never
REPLACE on parent rows), 5 hide/trash/restore + .lrc lyrics working on Android 10+,
6 local text-command assistant (skip the empty AI stubs), 7 delete `sren-music/` with the user's OK.
The "Sren v0.4/v0.5" APKs the user had were repackaged LarkPlayer (com.dywx.larkplayer): never use.
Auto-merging PRs is blocked by the permission classifier: the user merges PRs themselves.

Owner rule: remove unused code and libraries (media3-ui and Compose tooling were removed).

Not done: sleep timer (coming from the user's other app), equalizer, shuffle/repeat UI, queue
persistence, full-screen Now Playing, album/artist screens, video player,
playlists/favorites/history tables.

Open question to verify on a device: whether deleting a file with the app closed
bumps `getGeneration` (if not, the deletion shows up only after the next change).

## Next: Phase 5 (approved in principle, waiting for the user's go-ahead)

1. **Audio offload only while the screen is off**, disabled when the screen is on
   (so EQ/speed work). Settings toggle, on by default, automatic fallback if the
   device rejects it (some Xiaomi/Oppo ROMs are buggy).
2. **Sleep timer** with coroutines, no system alarms; pauses and lets the service stop.

Rejected by the user: forcing AMOLED black / auto screen-off tricks (the phone already
has a power button and system settings).
Saved for later: optional "video → audio-only if the user falls asleep" for the video player.

Later phases: video player (gestures, subtitles, PiP), playlists/favorites/history,
equalizer, widget, themes, targetSdk 36.

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
