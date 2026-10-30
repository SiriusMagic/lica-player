# Historia de versiones y plan de Stone 1.4

## La meta
**Stone 1.4** = la misma apariencia y funciones de **Stone 1.3.2** (pingüino, íconos, colores, pantallas),
reconstruida sobre la base de **Lica** (Kotlin + Jetpack Compose + Media3): reproductor, servicio, escaneo
sin repetir, tiempos de actualización por eventos y perfiles de batería. Resultado: más rápida, menos memoria,
más batería y un APK de ~3 MB en vez de 26 MB.

Stone 1.3.2 está hecha en React Native (JavaScript). Stone 1.4 traduce cada pantalla a Kotlin nativo.
El código de Stone 1.3.2 se queda en [`../legado/stone/`](../legado/stone) como modelo hasta terminar.

## Línea de tiempo

| Fecha | Versión | Tecnología | Dónde está |
|---|---|---|---|
| jun–oct 2026 | Sren Kotlin, Sren Flutter, Aura Player, Sren-source | Varias | Eliminadas. Código en la etiqueta git `legado-2026-10` |
| 26 oct 2026 | **Lica Player 0.1.0** | Kotlin + Compose + Media3 | Historial de git: es la **base** |
| 29 oct 2026 | **Sren music 0.2.0** | Kotlin + Compose + Media3 | `sren/` (base Lica, se convierte en Stone 1.4) |
| 29 oct 2026 | **Stone 1.3.2** (`com.zirakusa.stone`) | Expo / React Native + módulos Kotlin | `legado/stone/` · APK `apk/legado/stone-1.3.2.apk` (26 MB) |
| próximo | **Stone 1.4** | Kotlin + Compose + Media3 (base Lica) | `sren/` |

## Qué se traduce de Stone 1.3.2 (pantalla por pantalla)

| Función de Stone | Archivo en Stone | Estado en 1.4 |
|---|---|---|
| Nombre, íconos, colores, pingüino | `app.json`, `res/mipmap-*`, `design_guidelines.json` | Pendiente |
| Bienvenida: idioma y "Reproduce tu música" | `app/onboarding/` | Pendiente |
| 6 idiomas (es, en, pt, fr, it, de) | `src/i18n/translations.ts` | Pendiente |
| Biblioteca por pestañas, orden, búsqueda | `features/library/` | Base Lica ✅, falta aspecto Stone |
| Ocultos y papelera | `HiddenFilesModal.tsx`, `RecentDeletedModal.tsx` | Pendiente |
| Reproductor, cola, mini reproductor | `app/player.tsx`, `QueueSheet.tsx`, `MiniPlayer.tsx` | Base Lica ✅, falta aspecto Stone |
| Letras `.lrc` | `library-lyrics.ts`, `LyricsSheet.tsx` | Pendiente |
| Temporizador personalizado | `CustomSleepTimerSheet.tsx` | Base Lica ✅ (más eficiente), falta aspecto |
| Ecualizador | `StoneEqualizer.kt` | Pendiente |
| Volumen propio en pantalla | `StoneVolumeOverlay.kt` | Pendiente |
| Editor de tonos | `RingtoneEditor.tsx`, `StoneRingtoneModule.kt` | Pendiente |
| Ahorro de energía | `EnergySaverScreen.tsx` | Base Lica ✅ (perfiles de audio) |
| Temas, notificaciones, ajustes | `ThemeSheet.tsx`, `SettingsSheet.tsx` | Pendiente |
| Opiniones y preguntas frecuentes | `Feedback*.tsx` | Pendiente |
| Descarga de YouTube (`backend/`, yt-dlp) | `backend/` | ❌ No se pasa: necesita una PC y va contra las reglas de YouTube |

## Lo de la base Lica que Stone no tenía (se queda)
Inicio "Para ti hoy", estadísticas de lo más escuchado, favoritas, recomendación diaria, perfiles de batería,
escaneo sin repetir, servicio en el mismo proceso.

## Errores aprendidos
1. Código sin guardar en git se pierde (Aura Player). Todo va por PR, con CI y Releases.
2. APK con nombres engañosos: se nombran por app y versión y se documentan en `apk/README.md`.
3. React Native gasta más: 26 MB de APK y un motor de JavaScript en memoria. Stone 1.4 es nativa.
