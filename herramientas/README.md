# Herramientas de música

Prototipos de apoyo para el reproductor Lica, traídos desde cuadernos antiguos de Google Colab y limpiados.

## `sarap/sarap.py` — asistente de navegación (prototipo)

Simula cómo un asistente entiende frases como *"quiero música"*, *"busca canción"*, *"pon Aver"* o *"atrás"* y las convierte en acciones sobre las pantallas de la app (inicio, biblioteca, búsqueda, reproduciendo y buscador general). Las reglas hacen de "LLM simulado".

```bash
python herramientas/sarap/sarap.py   # escribe "salir" para terminar
```

Antes era `Copia de Untitled4.ipynb`. Se reorganizó en una clase `Sarap` reutilizable y se corrigió que el buscador general mostraba la búsqueda anterior de música.

## `biblioteca/biblioteca_musical.ipynb` — biblioteca en Colab

Descarga audio a MP3 con carátula y metadatos, escanea la carpeta, guarda la biblioteca en `library.json` y clasifica cada canción por estado de ánimo (amor, tristeza, fiesta, energía, calma, ira) según título, artista y álbum.

Ábrelo en Colab con **Archivo → Abrir cuaderno → GitHub** → `SiriusMagic/lica-player` y ejecuta las celdas en orden.

Antes era `Copia de Untitled9.ipynb`. Cambios:
- Celdas ordenadas: primero la instalación y luego un paso por celda.
- Corregida la plantilla de nombres de archivo, que tenía espacios (`% (title)s`) y habría guardado las canciones con nombres rotos.
- Un MP3 dañado ya no detiene el escaneo.

> Úsala solo con música que tengas derecho a descargar.

El descargador anterior con `pytube` (`Untitled0.ipynb`) no se trajo: `pytube` dejó de funcionar y este cuaderno lo reemplaza.
