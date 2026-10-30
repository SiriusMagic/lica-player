# Cambios de Sren music

Todas las versiones de la app, de la más nueva a la más vieja. Cada versión publicada tiene su APK en la pestaña **Releases** de GitHub y en `/apk/` del Codespace.

## [Sin publicar]

## [0.2.0] - 2026-10-29
Primera versión como **Sren music** (antes Lica Player).

### Añadido
- **Inicio**: bienvenida de Sren, "Para ti hoy", escuchadas recientemente, las que más escuchas, favoritas y añadidas hace poco.
- **Biblioteca**: Canciones (con orden), Favoritas, Carpetas, Artistas, Álbumes y Videos; abrir un álbum, artista o carpeta con "Reproducir todo" y "Aleatorio".
- **Buscar** por título, artista o álbum.
- **Reproductor completo**: portada grande, aleatorio, repetir, velocidad (0.5x–2x), cola y favorita.
- **Temporizador para dormir** que no corta canciones a la mitad y no despierta el procesador.
- **Favoritas** (mantener pulsada una canción).
- **Recomendación diaria** con la portada, que se puede apagar en Ajustes → Control avanzado.
- **Perfiles de audio y batería**: Máxima duración, Equilibrado y Alta fidelidad.
- Pruebas unitarias, CI que compila cada PR, y publicación automática de APK en Releases.
- Documentación: `docs/` (arquitectura, gestos, compilar, versiones, inventario) y `legado/` con el código de todas las versiones anteriores.

### Cambiado
- Nombre, ícono, colores y paquete: de Lica Player (`com.lica.player`) a Sren music (`com.sren.music`). Se instala como app nueva.

## [0.1.0] - 2026-10-26
Lica Player, fases 1–4: biblioteca de canciones y videos sin escaneos repetidos, servicio de reproducción en segundo plano, lista de canciones y mini reproductor.
