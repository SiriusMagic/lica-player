# Cambios de Stone (antes Sren music)

Todas las versiones de la app, de la más nueva a la más vieja. Cada versión publicada tiene su APK en la pestaña **Releases** de GitHub y en `/apk/` del Codespace.

## [Sin publicar]
Camino a **Stone 1.4** (lista completa en `docs/STONE-1.4.md`).

### Cambiado
- La app pasa a ser **Stone**: nombre, ícono del pingüino, ícono de notificación y colores de Stone 1.3.2.
  Paquete `com.zirakusa.stone` (el de Stone), `versionCode` 20. La carpeta del código pasa de `sren/` a `stone/`.

### Añadido
- **Bienvenida de Stone**: elegir idioma (Empezar), Términos y Privacidad con páginas reales
  (`docs/TERMINOS.md`, `docs/PRIVACIDAD.md`), y "Reproduce tu música" con **Permitir acceso**. Solo sale la primera vez.
- **6 idiomas**: español, inglés, portugués, francés, italiano y alemán; también se cambia en Ajustes → Idioma.
- **Verificador de apartados** (`herramientas/verificar_stone.py`, también en la CI): cada apartado marcado como
  hecho debe citar su código, y los 6 idiomas deben estar completos. Skill del proyecto: `/verificar-stone`.

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
