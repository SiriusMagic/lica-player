# Stone 1.4: especificación de la traducción

**Stone 1.4** es **Stone 1.3.2** con la misma apariencia, los mismos apartados y los mismos gestos, pero reescrita
en **Kotlin nativo** sobre la base de Lica (Jetpack Compose + Media3). Este documento es el contrato: **cada fila es
algo que Stone 1.3.2 hace y que Stone 1.4 debe hacer igual o mejor**. Si algo se mueve en Stone, aquí está.

- Código original: [`../legado/stone/`](../legado/stone) (no se borra nunca). APK: `apk/legado/stone-1.3.2.apk`.
- Código nuevo: [`../sren/`](../sren) (pasará a llamarse `stone/` en el PR de identidad).
- Estado de cada fila: ⬜ pendiente · 🟡 existe en la base Lica pero falta el aspecto de Stone · ✅ hecho.
  Al terminar cada PR se actualiza la columna **Estado**.

## 1. Por qué traducir (lo que se gana)

| | Stone 1.3.2 | Stone 1.4 |
|---|---|---|
| Lenguaje | JavaScript (React Native 0.81 / Expo 54) + puentes a Kotlin | Kotlin nativo, sin motor de JavaScript |
| Tamaño del APK | 26 MB | ~3–4 MB (objetivo: menos de 6 MB) |
| Motor de audio | `MediaPlayer` de Android | Media3 / ExoPlayer, con *offload* (el chip de audio reproduce y el procesador duerme) |
| Posición de la canción | Se envía **cada 250 ms** a JavaScript, siempre (`StonePlaybackEngine.reporter`) | Por eventos; la barra se mueve **1 vez por segundo**, solo con la pantalla visible |
| Biblioteca | Escaneo manual de carpeta; datos en AsyncStorage (JSON) | MediaStore + Room; al abrir no escanea si nada cambió |
| Ahorro de energía | Bajar el brillo de la pantalla (12 % y luego 1 %) | Perfiles de audio configurables + la pantalla de ahorro de Stone |
| Datos del usuario | AsyncStorage (se pierden si el JSON se corrompe) | Base de datos `stats.db` con migraciones (nunca se borra al actualizar) |

## 2. Identidad (aspecto)

| Qué | En Stone 1.3.2 | Stone 1.4 | Estado |
|---|---|---|---|
| Nombre | "Stone" | "Stone" | ⬜ |
| Paquete | `com.zirakusa.stone` (versión 19) | `com.zirakusa.stone`, `versionCode` 20, `versionName` 1.4.0 (firma: ver §11) | ⬜ |
| Ícono | `frontend/android/app/src/main/res/mipmap-*` (+ `ic_stat_stone.xml` para la notificación) | Los mismos archivos copiados | ⬜ |
| Fondo según tema | Negro `#0A0A0E`, Gris `#1A1C20`, Claro `#F3F1EB` (`LibraryPage.tsx`) | Tres temas iguales | ⬜ |
| Colores | `design_guidelines.json`: amarillo `#F5C518`, superficies `#1C1C24` / `#2A2A35`, texto gris `#A0A0A5`, rojo `#FF4C4C` | Mismo archivo pasado a `ui/theme/Color.kt` | 🟡 |
| Íconos de botones | Ionicons | Material Symbols equivalentes (se dibujan como vectores, sin fuente aparte) | ⬜ |

## 3. Bienvenida (primer arranque)

| Apartado / gesto | En Stone 1.3.2 | Estado |
|---|---|---|
| Elegir idioma: tocar un idioma lo marca; botón amarillo **Empezar** | `app/onboarding/language.tsx` | ⬜ |
| Enlaces de Términos y Privacidad (en Stone apuntan a `example.com`: faltan las páginas reales) | `language.tsx` | ⬜ |
| "Reproduce tu música" con el pingüino y botón **Permitir acceso** (pide el permiso de música y no vuelve a salir) | `app/onboarding/welcome.tsx` | ⬜ |
| 6 idiomas: es, en, pt, fr, it, de | `src/i18n/translations.ts` → `res/values-*/strings.xml` | ⬜ |

## 4. Biblioteca (pantalla principal)

| Apartado / gesto | En Stone 1.3.2 | Estado |
|---|---|---|
| Barra superior: buscar · carpeta de Stone · escanear · descarga rápida · ordenar · ajustes | `zones/01-header/HeaderZone.tsx` | 🟡 (sin descarga rápida, ver §10) |
| Pestañas: Videos · Canciones · Playlists · Carpetas · Artistas · Álbumes | `LibraryTabs.tsx` | 🟡 |
| **Deslizar a izquierda o derecha** sobre la lista cambia de pestaña (se activa con 18 px de movimiento horizontal y cambia al soltar con más de 42 px) | `LibraryPage.tsx` (`contentSwipeResponder`) → `HorizontalPager` de Compose | ⬜ |
| Tocar una canción la reproduce y usa la lista visible como cola | `LibraryTabContent.tsx` | ✅ |
| Carpetas: tocar abre la carpeta; botón atrás la cierra. Videos: lista de carpetas de video | `LibraryTabContent.tsx` | 🟡 |
| Artistas / Álbumes: tocar reproduce el grupo | `LibraryTabContent.tsx` | 🟡 (Lica abre la página con "Reproducir todo") |
| Playlists: **Nueva playlist** (cuadro con nombre) y abrir una playlist | `NewPlaylistDialog.tsx` | ⬜ |
| Favoritas: reproducir todo, aleatorio, invertir orden, menú ⋮ | `FavoritesPlaylistScreen.tsx` | 🟡 |
| Ordenar: Más recientes · Más reproducido · A a Z · Artista · Duración más larga (tocar de nuevo invierte el orden) | `LibrarySortSheet.tsx` | 🟡 |
| Buscar por título, artista o álbum, con botón para borrar el texto | `HeaderZone.tsx` | ✅ |
| Escanear con animación de disco girando y porcentaje | `LibraryPage.tsx` (`spinAnim`) | ⬜ |
| Mini reproductor: tocar abre el reproductor · ⋮ abre el reproductor con el menú abierto · play/pausa | `src/components/MiniPlayer.tsx` | 🟡 |

## 5. Reproductor

| Apartado / gesto | En Stone 1.3.2 | Estado |
|---|---|---|
| Portada, título, artista; flecha atrás cierra | `app/player.tsx` | ✅ |
| Corazón: favorita | `player.tsx` | ✅ |
| Barra de progreso arrastrable (salta al soltar) | `player.tsx` (`onSlidingComplete`) | ✅ |
| Anterior · play/pausa · siguiente | `player.tsx` | ✅ |
| Fila inferior: ecualizador · letras · cola | `player.tsx` | ⬜ |
| **Girar el teléfono** (horizontal) abre la vista **Vinilo**: disco con la portada, aguja, letras que avanzan solas, favorita y controles | `player.tsx` (`vinylVisible`) | ⬜ |
| Chip de letras que muestra la línea actual | `LyricsChip.tsx` | ⬜ |
| Cola: lista, ir a la canción actual, repetir y aleatorio | `QueueSheet.tsx`, `QueueRow.tsx` | 🟡 |

### Menú ⋮ del reproductor (en este orden)
| Opción | En Stone 1.3.2 | Estado |
|---|---|---|
| Editor de etiquetas (título, artista, álbum; se guardan dentro de la app sin tocar el archivo) | `player.tsx` (`trackEdits`) | ⬜ |
| Ecualizador | `StoneEqualizer.kt` | ⬜ |
| Agregar a la lista de reproducción | `player.tsx` (en Stone no hace nada todavía) | ⬜ |
| Compartir | `StoneShareModule.kt` | ⬜ |
| Velocidad: 0.5 · 0.75 · 1 · 1.25 · 1.5 · 2 | `player.tsx` | ✅ (Lica: 0.5–2x) |
| Establecer como tono de llamada | `RingtoneEditor.tsx` + `StoneRingtoneModule.kt` | ⬜ |
| Temporizador para dormir | `SleepTimerSheet.tsx` | 🟡 |
| Modo de ahorro de energía | `EnergySaverScreen.tsx` | ⬜ |
| Ocultar (con confirmación) | `player.tsx` | ⬜ |
| Borrar (con confirmación; va a "Eliminado recientemente") | `player.tsx` | ⬜ |

### Ecualizador
- 10 bandas: 31, 63, 125, 250, 500, 1K, 2K, 4K, 8K, 16K Hz; interruptor de encendido.
- Preajustes: Personalizado, Aumento de graves, Aumento de voz, Aumento de agudos, Normal, Pop (valores en `player.tsx`, `equalizerPresets`).
- Mejora en 1.4: el `Equalizer` de Android solo existe mientras suena y se quita cuando está plano (no gasta procesador si no se usa). ⬜

### Editor de tono
- Deslizador para elegir el inicio; el tono dura **30 s** (`CLIP_MS`); botón para escucharlo y botón **Establecer como tono de llamada** (pide el permiso "modificar ajustes del sistema").
- Mejora en 1.4: el recorte con Media3 Transformer, que copia el audio sin volver a codificarlo. ⬜

## 6. Señales y botones físicos

| Señal | En Stone 1.3.2 | Stone 1.4 | Estado |
|---|---|---|---|
| **Botones de volumen** dentro de la app | `MainActivity.dispatchKeyEvent` → `handleVolumeDelta` | Igual | ⬜ |
| Volumen por encima del 100 %, **hasta 300 %** | `LoudnessEnhancer` en `StonePlaybackEngine.applyBoost` | Igual (`LoudnessEnhancer` sobre la sesión de Media3); se apaga a ≤100 % | ⬜ |
| Cartel propio de volumen con porcentaje | `StoneVolumeOverlay.kt` (PopupWindow) | Componente de Compose dentro de la app | ⬜ |
| Botón atrás en ahorro de energía: sale del modo | `EnergySaverScreen.tsx` | Igual | ⬜ |
| Volver a la app: recupera el brillo | `EnergySaverScreen.tsx` (AppState) | Igual | ⬜ |
| Auriculares desconectados: pausa | — | Lica ✅ (`setHandleAudioBecomingNoisy`) | ✅ |
| Controles en la notificación y en la pantalla bloqueada | `StoneMediaNotification.kt` | Lica ✅ (MediaSession) | ✅ |

## 7. Ajustes (en el orden de Stone)

| Fila | Qué hace | Estado |
|---|---|---|
| Carpeta de Stone | Elegir la carpeta de música | ⬜ |
| Escanear biblioteca | Vuelve a leer la música | 🟡 |
| Tema | Claro · Gris · Negro | ⬜ |
| Temporizador para dormir | Apagar · 10 · 20 · 30 · 60 min · Personalizado (horas y minutos) · "Terminar la canción antes de parar" | 🟡 (Lica ya termina la canción y no despierta el procesador) |
| Archivos ocultos | Pestañas audio/video, buscar, ordenar (Más recientes · Más reproducido · A a Z), "Configuración de archivo" (mostrar audios de WhatsApp, convertir videos en audio), tocar para mostrar de nuevo | ⬜ |
| Eliminado recientemente | Pestañas, **mantener pulsado** para seleccionar varios, "Seleccionar todo", restaurar o borrar del todo | ⬜ |
| **Ajustes de reproducción** | Reproducir junto con otras apps · Reproducción sin pausas · Crossfade de 0 a 12 s | ⬜ |
| └ **Perfil de batería** (nuevo en 1.4, dentro de esta pantalla) | Máxima duración · Equilibrado · Alta fidelidad | 🟡 (existe en Lica) |
| Configuración de notificaciones | Aviso de archivos nuevos · Recomendación de música | 🟡 (la recomendación diaria existe en Lica) |
| Idioma | Los 6 idiomas | ⬜ |
| Opinión | Inicio, formulario (categoría, captura, enviar) y preguntas frecuentes | ⬜ |
| Sobre | Versión de la app | ⬜ |

### Perfiles de batería (configurables)
| Perfil | Qué hace | Cuándo usarlo |
|---|---|---|
| **Máxima duración** | El chip de audio reproduce solo y el procesador duerme; la pantalla se limita a 60 Hz | Escuchar horas con la pantalla apagada |
| **Equilibrado** (por defecto) | Igual, pero solo si no se cortan los silencios entre canciones | Uso normal |
| **Alta fidelidad** | Sin *offload*, salida de 32 bits | Auriculares buenos, cargando el teléfono |

El **modo de ahorro de energía** de Stone (pantalla negra con brillo al 12 % y luego al 1 % tras 15 s; tocar la
vuelve a encender) se conserva tal cual y además activa el perfil **Máxima duración** mientras está abierto. ⬜

## 8. Letras

| Qué | En Stone 1.3.2 | Estado |
|---|---|---|
| Leer un `.lrc` con el mismo nombre que la canción | `library-lyrics.ts` | ⬜ |
| Hoja de letras: mostrar, agregar, buscar | `LyricsSheet.tsx` | ⬜ |
| Agregar: pegar texto o importar un archivo | `LyricsAddFlow.tsx` | ⬜ |
| Letras sincronizadas que avanzan con la canción | `findActiveLyricIndex` | ⬜ (en 1.4 se calcula solo cuando cambia de línea, no cada 250 ms) |

## 9. Lo que Stone 1.4 conserva de Lica (Stone no lo tenía)
Inicio "Para ti hoy" · escuchadas recientemente · las que más escuchas · recomendación diaria con la portada ·
estadísticas de reproducción · temporizador que no despierta el procesador · escaneo que no se repite al abrir.
**Pendiente de decidir por el dueño:** listas por estado de ánimo y asistente por frases (código en la etiqueta `legado-2026-10`).

## 10. Lo que no se traduce
| Qué | Por qué |
|---|---|
| Descarga rápida (YouTube / playlists, `backend/` con yt-dlp, `QuickDownload*`) | Necesita una PC encendida como servidor y descargar de YouTube va contra sus reglas |
| Versión web (`react-native-web`, `levantar-web.cmd`) | Stone 1.4 es solo Android |

## 11. Versiones y firma (para que nada se pierda)
- Cada versión: `versionName` X.Y.Z + `versionCode` +1, una entrada en `CHANGELOG.md`, una etiqueta `vX.Y.Z`
  y su Release en GitHub con el APK. La primera es **v1.4.0** (`versionCode` 20, siguiente a la 19 de Stone 1.3.2).
- **Clave de firma:** Stone 1.3.2 está firmada con la **clave de prueba pública de React Native**
  (`debug.keystore`, SHA-1 `5e8f1606…f625`), la que trae cualquier proyecto nuevo: cualquiera podría firmar una
  "actualización" falsa con ella. Stone 1.4 usa una **clave propia** (secreto de GitHub `SREN_KEYSTORE_BASE64`,
  ver `COMPILAR.md`; nunca en el repositorio). Por eso, al pasar de 1.3.2 a 1.4 hay que **desinstalar una sola vez**
  la 1.3.2. No se pierde nada útil: la música está en el teléfono y los datos de 1.3.2 (JSON de React Native)
  no los puede leer la 1.4. Desde la 1.4, cada versión se instala encima de la anterior.
- Guardado: todo por PR a `main`; `main` protegida; etiquetas `legado-2026-10` y `stone-1.3.2` como respaldo.
