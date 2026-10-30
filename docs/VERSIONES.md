# Historia de versiones

Todas las apps de música que existieron, de dónde salieron y qué se toma de cada una para la Sren única. El código de las antiguas está en [`../legado/`](../legado) (etiqueta git **`legado-2026-10`**).

## Línea de tiempo

| Fecha | Versión | Tecnología | Dónde está | Estado |
|---|---|---|---|---|
| 27 jun 2026 | **Sren Kotlin** (`com.sren.music`) | Kotlin + Compose | `legado/sren-kotlin/` · APK `apk/legado/sren-kotlin-1.0.0.apk` | Compila |
| 28 jun 2026 | **Sren Flutter** (`app.sren.mobile`) | Flutter | `legado/sren-flutter/` · APK `apk/legado/sren-flutter-1.0.0.apk` | Compila (con corrección) |
| 28 jun 2026 → oct | **Pingüino Music** | Expo / React Native (web y móvil) | Repositorio [SiriusMagic/pinguino-music](https://github.com/SiriusMagic/pinguino-music) | Funciona en el navegador |
| ? | **Aura Player** (`com.codex.auraplayer`) | Kotlin | Solo su README: `legado/aura-player/` | **Código perdido** (no estaba en ningún zip) |
| 25 oct 2026 | **Sren-source** (`com.sren`) | Kotlin + Compose | `legado/sren-source/` · APK `apk/legado/sren-source-0.1.0.apk` | Compila (con corrección), 6 pruebas pasan |
| 26 oct 2026 | **Lica Player 0.1.0** | Kotlin + Compose + Media3 | Este repositorio (historial de git) | Base actual |
| 29 oct 2026 | **Sren music 0.2.0** | Kotlin + Compose + Media3 | `sren/` | **La app actual** |

**Lo que no son versiones propias:** los APK "Sren v0.3", "v0.4" y "v0.5" que había en la carpeta Codex (`output/`), y el `phone_dump/` sacado del teléfono, eran **LarkPlayer** (`com.dywx.larkplayer`) re-firmado. No se usan y se eliminan.

## Qué se toma de cada una

| Función | Viene de | Estado en Sren |
|---|---|---|
| Servicio de reproducción, batería, escaneo sin repetir | Lica | ✅ Hecho |
| Inicio, reproductor completo, temporizador, biblioteca, buscar, favoritas | Sren Kotlin + Lica | ✅ Hecho (0.2.0) |
| Menú ⋮ (siguiente, playlist, compartir, usar como tono, ocultar, papelera) | Sren Kotlin | Planificado (PR C) |
| Playlists, ocultas, papelera con restaurar | Sren Kotlin (sin su error que vaciaba playlists) | Planificado (PR C) |
| Listas por estado de ánimo | Cuaderno de Colab (`MOOD_WORDS`, de Python a Kotlin) | Planificado (PR C) |
| Pingüino amarillo, colores, botones píldora, chips | Fotos de diseño + Pingüino Music + Flutter | Planificado (PR B) |
| Bienvenida: elegir idioma, "Reproduce tu música", términos | Flutter + Pingüino Music | Planificado (PR B) |
| 6 idiomas (es, en, pt, fr, it, de) | Pingüino Music (`translations.ts`) | Planificado (PR B) |
| Mini reproductor flotante, arrastrar la barra de progreso | Pingüino Music | Barra ✅; mini flotante planificado (PR D) |
| Pestañas deslizables, letras `.lrc` | Sren Kotlin (letras corregidas para Android 10+) | Planificado (PR D) |
| Volumen, gestos de deslizar, recortar audio, editar información | Ideas de lo que el usuario recordaba (hechas desde cero) | Planificado (PR D/E) |
| Asistente: lista de acciones permitidas, aclaraciones, búsqueda sin tildes | Sren-source + SARAP (`herramientas/`) + comandos en Buscar de Aura | Planificado (PR F) |
| Ajustes por secciones (biblioteca, limpieza, información) | Aura Player + Flutter | Planificado (PR F) |
| Descargas de YouTube/Instagram | Aura Player | ❌ Descartado (sin código y requiere fuentes legales) |
| Proveedores de IA (OpenAI, Gemini, Ollama, Alexa) | Sren Kotlin / Sren-source | ❌ Descartado (estaban vacíos) |

## Errores aprendidos (para no repetirlos)
1. **Reproductor sin servicio** (Sren Kotlin): la música se cortaba con la pantalla apagada. Sren usa `PlaybackService`.
2. **Escanear abriendo cada archivo** (Sren Kotlin): lento. Sren usa los datos de MediaStore.
3. **`REPLACE` + `CASCADE`** vaciaba las playlists (Sren Kotlin). En Sren, los datos del usuario van en `stats.db`, con migraciones y sin `REPLACE`.
4. **Código sin compilar ni guardar en git** (Flutter, Sren-source, Aura): por eso se perdió Aura Player. Ahora todo va por PR, con CI y Releases.
5. **APK con nombres engañosos** ("Sren v0.5" que era LarkPlayer): los APK se nombran `sren-music-X.Y.Z.apk` y se documentan en `apk/README.md`.
