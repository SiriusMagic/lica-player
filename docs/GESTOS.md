# Gestos de Stone

Cada gesto de la app, qué hace y **en qué archivo y función está** (rutas relativas a `stone/app/src/main/java/com/zirakusa/stone/`). Al añadir o cambiar un gesto, actualiza esta tabla.

## Gestos actuales

| Dónde | Gesto | Qué hace | Archivo → función |
|---|---|---|---|
| Bienvenida, idiomas | Tocar un idioma / **Empezar** | Lo marca / lo guarda y pasa a "Reproduce tu música" | `ui/screens/onboarding/Onboarding.kt` → `LanguageRow` / `LanguageScreen` → `MainActivity` (`STEP_LANGUAGE`) |
| Bienvenida, idiomas | Tocar Términos o Privacidad | Abre la página en el navegador | `ui/screens/onboarding/Onboarding.kt` → `TermsNotice` |
| Bienvenida | **Permitir acceso** | Pide el permiso de música; la bienvenida no vuelve a salir | `ui/screens/onboarding/Onboarding.kt` → `WelcomeScreen` |
| Cualquier lista de canciones | Tocar | Reproduce esa lista desde la canción tocada | `ui/components/SongList.kt` → `SongRow` (`onClick`) / `SongList` |
| Cualquier lista de canciones | Mantener pulsado | Abre el menú de la canción (favorita) | `ui/components/SongList.kt` → `SongRow` (`onLongClick`) → `ui/components/SongActionsSheet.kt` |
| Inicio, filas de portadas | Deslizar a los lados | Recorre la fila | `ui/screens/home/HomeScreen.kt` → `songRow` (`LazyRow`) |
| Inicio, portada | Tocar / mantener pulsado | Reproduce la fila / abre el menú | `ui/screens/home/HomeScreen.kt` → `SongCard` |
| Inicio, "Para ti hoy" | Tocar / mantener pulsado | Reproduce su álbum / abre el menú | `ui/screens/home/HomeScreen.kt` → `SuggestionCard` |
| Mini reproductor | Tocar | Abre el reproductor completo | `ui/player/NowPlayingBar.kt` → `NowPlayingBar` (`clickable(onOpen)`) |
| Mini reproductor | Botones ⏮ ⏯ ⏭ | Anterior / reproducir-pausar / siguiente | `ui/player/NowPlayingBar.kt` → `IconButton`s |
| Reproductor completo | Arrastrar la barra de progreso | Adelanta o retrocede al soltar | `ui/player/PlayerScreen.kt` → `SeekBar` (`Slider`) |
| Reproductor completo | Botón ↓ o "atrás" del teléfono | Cierra el reproductor | `ui/player/PlayerScreen.kt` → `PlayerScreen` (`onClose`) |
| Reproductor completo | Botones aleatorio / repetir | Activa aleatorio; repetir: no → toda la cola → una | `ui/player/PlayerScreen.kt` → `PlayerScreen` → `PlayerViewModel.toggleShuffle` / `cycleRepeat` |
| Reproductor completo | Botón velocidad / luna / cola | Abre la hoja correspondiente | `ui/player/PlayerScreen.kt` → `SpeedSheet`, `TimerSheet`, `QueueSheet` |
| Hoja de la cola | Tocar / ✕ | Salta a esa canción / la quita de la cola | `ui/player/PlayerScreen.kt` → `QueueSheet` |
| Biblioteca | Tocar una subpestaña | Cambia de subpestaña | `ui/screens/library/LibraryScreen.kt` → `LibraryScreen` (`ScrollableTabRow`) |
| Biblioteca, Carpetas / Artistas / Álbumes | Tocar | Abre esa colección | `ui/screens/library/LibraryScreen.kt` → `GroupRow` |
| Biblioteca, Canciones | Tocar "orden" | Cambia el orden (se recuerda) | `ui/screens/library/LibraryScreen.kt` → `SortRow` |
| Barra superior | Botón ⟳ | Vuelve a buscar música en el teléfono | `ui/navigation/StoneNavHost.kt` → `StoneTopBar` → `LibraryViewModel.rescan` |
| Ajustes | Tocar un perfil / el interruptor | Elige el perfil de audio / activa la recomendación diaria | `ui/screens/settings/SettingsScreen.kt` → `ProfileRow` / `SwitchRow` |
| Ajustes, Idioma | Tocar un idioma | Cambia el idioma y vuelve a abrir la pantalla traducida | `ui/screens/settings/SettingsScreen.kt` → `LanguageSection` |
| Notificación diaria | Tocar | Abre la app y reproduce el álbum recomendado | `suggestion/SuggestionNotifier.kt` → `MainActivity.handleSuggestion` |
| Videos | Tocar | Aún no hace nada (reproductor de video pendiente) | `ui/screens/videos/VideosScreen.kt` → `VideoItem` |

## Gestos planificados (del plan de fusión)

| Dónde | Gesto | Qué hará | Archivo previsto |
|---|---|---|---|
| Fila de canción | Botón ⋮ | Menú completo (reproducir siguiente, playlist, compartir, tono, ocultar, papelera…) | `ui/components/SongList.kt`, `SongActionsSheet.kt` |
| Mini reproductor | Deslizar izquierda / derecha | Siguiente / anterior | `ui/player/NowPlayingBar.kt` |
| Mini reproductor | Deslizar arriba | Abre el reproductor | `ui/player/NowPlayingBar.kt` |
| Reproductor completo | Deslizar abajo | Cierra | `ui/player/PlayerScreen.kt` |
| Reproductor completo | Deslizar la portada | Siguiente / anterior | `ui/player/PlayerScreen.kt` |
| Biblioteca | Deslizar a los lados | Cambia de subpestaña (`HorizontalPager`, como en Sren Kotlin) | `ui/screens/library/LibraryScreen.kt` |
