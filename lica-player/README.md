# Lica Player

A clean-room Android media player, built from scratch in Kotlin. No third-party
app code or assets are reused — only standard Android/Jetpack libraries.

## Stack
- Kotlin + Jetpack Compose (Material 3)
- Media3 / ExoPlayer + MediaSessionService (audio & video)
- Room (library, playlists, history)
- Hilt (DI), MediaStore (device library scan)

## Build
Requires the Android SDK (`compileSdk 35`) and JDK 17+.

```sh
./gradlew :app:assembleDebug
```

Open in Android Studio for the full toolchain, or set `ANDROID_HOME` and run the
wrapper from the CLI.

## Roadmap
- [x] **Phase 1 — Skeleton**: project, identity, Compose navigation (4 tabs),
      runtime media permissions, theme, original launcher icon.
- [x] **Phase 2 — Local library**: MediaStore scan in chunks, Room cache, songs & videos tabs.
- [x] **Phase 3 — Audio engine**: single-process MediaSessionService, audio-only renderer,
      notification + lock screen + headset controls, mini player tinted from the cover.
- [ ] Phase 4 — Video player (fullscreen, gestures, subtitles, PiP).
- [ ] Phase 5 — Playlists, favorites, history (Room).
- [ ] Phase 6 — Equalizer/visualizer, sleep timer, home-screen widget, floating player, i18n, themes.

## Low-resource rules
- One process. The playback service shares the UI's runtime (no second ART instance).
- Audio-only renderer and hardware decoders in the playback service.
- Partial wake lock only while playing; pause on headphone unplug; automatic audio focus.
- Progress updates tick once per second, only while playing and while the UI is visible.
- Cover colors computed once per album from a 64 px decode, cached as ints, and only while the UI is visible.
- Zero-scan launch: Room is the source of truth. MediaStore version + generation decide between no scan, a diff of changed rows, or a full scan.
- Library change observer registered only while the app is visible.
- Image cache capped at 10% of app memory and 32 MB on disk; list thumbnails at 128 px.
