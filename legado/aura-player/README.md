> **Nota (2026-10-29):** este es el README original de *Aura Player* (paquete `com.codex.auraplayer`), que venía dentro de `Sren-source.zip`. **Su código no estaba en ningún zip**: la carpeta `com/codex/auraplayer` venía vacía. Lo que se toma de aquí para Sren (solo ideas) está en [`docs/VERSIONES.md`](../../docs/VERSIONES.md). No se implementan las descargas de YouTube/Instagram/etc.: no hay código y requieren definir fuentes legales.

---

# Aura Player

Prototipo Android nativo de reproductor musical con busqueda inteligente funcionando por debajo, sin una capa visual de IA.

## Funciona en esta version

- Nueva experiencia visual clara inspirada en pantallas de busqueda/descarga, adaptada a la marca `Aura Player`.
- Navegacion inferior con `Descargar`, `Reproducir` y `Configuracion`.
- Secciones visuales `Buscar`, `YouTube`, `Musica` y `Mas`.
- Lista extendida de servicios en `Mas`: Instagram, Pinterest, Kwai, Threads, SnapChat, DailyMotion, SoundCloud, BlueSky, OkRu y otros.
- Pantalla `Descargado` en `Reproducir` cuando todavia no hay permisos de almacenamiento/audio.
- Configuracion redisenada en lista con secciones de descarga, herramientas, limpieza, informacion y biblioteca local.
- Permiso de lectura de audio local.
- Escaneo automatico de canciones via MediaStore.
- Seleccion manual de carpetas con el selector de Android.
- Vistas de canciones, albumes, artistas, carpetas y playlists locales.
- Reproduccion basica con `MediaPlayer`.
- Orden por recientes, mas reproducido, A-Z y duracion.
- Interpretacion local de comandos en la busqueda, por ejemplo: `reproduce Dragon`, `pausa`, `continua`, `aleatorio`, `album Ringtones`.

La zona `Descargar` es interfaz preparada; no descarga contenido todavia. Antes de activar descargas hay que definir fuentes legales, permisos y reglas de publicacion.

## Compilar APK

```powershell
.\build-apk.cmd
```

Si prefieres PowerShell directo:

```powershell
powershell.exe -ExecutionPolicy Bypass -File .\build-apk.ps1
```

El APK debug queda en:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Pendiente para una version publica

- Servicio en segundo plano con notificacion multimedia.
- Controles Bluetooth/auriculares.
- Caratulas reales de albumes.
- Modelo de IA real o backend local/remoto para entender comandos mas complejos.
- Descargas de musica, solo despues de definir fuentes legales y permisos.
