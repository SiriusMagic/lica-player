# Legado: versiones anteriores de Sren

Aquí está guardado el código de **todas las versiones anteriores** de la app, para que nunca se vuelva a perder. **Nada de esta carpeta forma parte de la app actual** (que vive en [`../sren/`](../sren)); es solo referencia.

- Etiqueta de git para encontrarlo siempre: **`legado-2026-10`** (en GitHub: *Tags* → `legado-2026-10`).
- Historia completa y qué se tomó de cada una: [`../docs/VERSIONES.md`](../docs/VERSIONES.md).

| Carpeta | Qué es | Tecnología | Fecha del código | ¿Compila? |
|---|---|---|---|---|
| [`sren-kotlin/`](sren-kotlin) | La versión que se probaba en Android Studio: biblioteca, playlists, ⋮, papelera, letras, asistente | Kotlin + Compose | 27 jun 2026 | Sí (ver su README) |
| [`sren-source/`](sren-source) | Variante con el asistente más completo (orquestador, lista de acciones permitidas, búsqueda sin tildes) | Kotlin + Compose | 25 oct 2026 (fecha del zip) | Sí (ver su README) |
| [`sren-flutter/`](sren-flutter) | Bienvenida con idioma, permisos e inicio con el pingüino | Flutter (Dart) | 28 jun 2026 | Sí, con la corrección indicada |
| [`aura-player/`](aura-player) | Solo la descripción (README) de *Aura Player*; su código no estaba en ningún zip | — | — | No hay código |

**Pingüino Music** (versión web, Expo/React Native) no está copiada aquí porque ya tiene su propio repositorio: [SiriusMagic/pinguino-music](https://github.com/SiriusMagic/pinguino-music).

## Reglas
- No se edita para añadir funciones: lo nuevo se hace en `sren/`.
- Si algo de aquí se pasa a la app, se anota en `docs/VERSIONES.md`.
