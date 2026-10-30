# Sren Kotlin (27 jun 2026)

La versión de Sren que se probaba en Android Studio (carpeta `Documents\Codex\sren-music-clean` de la PC). Paquete `com.sren.music`.

## Qué tenía
- Pantallas: Inicio, Biblioteca (Canciones, Playlists, Carpetas, Artistas, Álbumes, deslizables con `HorizontalPager`), Buscar, Reproductor, Playlists, Favoritos, Asistente, Ajustes.
- Menú **⋮** por canción: Reproducir, Agregar a playlist, Compartir, Usar como tono, Ocultar, Enviar a papelera.
- Ajustes: canciones ocultas y "Eliminado recientemente" (papelera) con restaurar o borrar.
- Reproductor: barra de progreso, aleatorio, repetir, velocidad, cola, letras `.lrc`, temporizador.
- Asistente de comandos con reglas locales (sin internet).
- Pruebas: `LocalRuleEngineTest`, `PlaylistRepositoryTest`, `SongDaoTest`, `FakePlayerControllerTest`.

## Errores conocidos (por qué no se usa tal cual)
1. El reproductor no estaba en un servicio: con la pantalla apagada la música podía cortarse, no había controles en la notificación y no respetaba llamadas ni audífonos.
2. El escaneo abría cada canción con `MediaMetadataRetriever`: lento y gasta batería.
3. `REPLACE` sobre la tabla de canciones + `CASCADE` en las playlists: cada escaneo podía vaciar las playlists.
4. Las letras solo funcionaban hasta Android 9 (usaba rutas absolutas).
5. Actualizaba la pantalla cada medio segundo, incluso con la pantalla apagada.

## Cómo compilarla
Los archivos raíz (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradlew`) **faltaban** en el zip y se reconstruyeron el 2026-10-29. `applicationId` se cambió a `com.sren.music.kotlin` para instalarse junto a la Sren nueva.

```bash
cd legado/sren-kotlin
echo "sdk.dir=$ANDROID_HOME" > local.properties
JAVA_HOME=<ruta a JDK 21> ./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```
