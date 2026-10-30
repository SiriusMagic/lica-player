# Inventario: qué es cada carpeta

Qué hay en el repositorio y en el Codespace, si **forma parte de la app** y si **está guardado en git**. Si algo no aparece aquí, no debería existir: documéntalo o bórralo.

## En el repositorio (GitHub)

| Ruta | ¿Parte de la app? | Qué es |
|---|---|---|
| `sren/` | **Sí, es la app** | Código de Sren music (Android). Ver [`ARQUITECTURA.md`](ARQUITECTURA.md). |
| `sren/app/schemas/` | Sí | Esquemas de `stats.db` por versión, para revisar migraciones. Se generan al compilar; se suben a git. |
| `docs/` | Documentación | Arquitectura, gestos, compilar, versiones, este inventario y `diseno/` (fotos de diseño). |
| `CHANGELOG.md` | Documentación | Cambios de cada versión publicada. |
| `legado/` | No | Código de las versiones anteriores (Sren Kotlin, Sren-source, Sren Flutter y el README de Aura Player). Solo referencia. |
| `herramientas/` | No | Prototipos en Python: **SARAP** (frases → acciones) y el cuaderno de Colab **biblioteca musical** (descarga y clasificación por ánimo). Sus ideas pasan a la app. |
| `tests/` | No | Pruebas de `herramientas/` (pytest). |
| `.github/` | No (automatización) | CI: `pruebas.yml` (herramientas), `android.yml` (compila la app en cada PR), `publicar-apk.yml` (Release con APK al crear una etiqueta `v*`), Dependabot. |
| `README.md`, `LICENSE`, `SECURITY.md`, `CLAUDE.md` | No | Presentación, licencia Apache 2.0, cómo reportar fallos y notas para el asistente de código. |

## Solo en el Codespace (no se sube a git)

| Ruta | ¿Parte de la app? | Qué es |
|---|---|---|
| `apk/` | No | **Todos los APK**, por versión. Ver `apk/README.md`. Solo se sube a git `apk/README.md`. |
| `morluto-rea/` | No | **REA**, la herramienta de análisis de APK (repositorio público aparte: github.com/INTECHNetworks/morluto-rea). Es la única herramienta que se conserva. |
| `sren/local.properties`, `legado/*/local.properties` | No | Ruta del Android SDK de esta máquina. |
| `sren/app/build/`, `legado/*/app/build/` | No | Resultados de compilación; se regeneran solos. |
| `~/.gradle/` | No | Caché de Gradle (se puede borrar; se vuelve a descargar). |

## Fuera del Codespace

| Dónde | Qué es | Qué hacer |
|---|---|---|
| Drive: zip de la carpeta **Codex** (2,1 GB) | Copia de `Documents\Codex` de la PC | Ya se extrajo lo propio a `legado/` y `docs/diseno/`. Borrarlo o dejarlo **restringido**. |
| Drive: **Sren-source.zip** | Variante Sren-source y el README de Aura Player | Ya extraído a `legado/`. Borrarlo o dejarlo **restringido**. |
| Repositorio **pinguino-music** | Pingüino Music (web) | Se mantiene como repositorio aparte. |

## Lo que se elimina (no es de la app ni del usuario)
- `base.apk`, `split_config.*.apk` (raíz) y el material de LarkPlayer dentro de `morluto-rea/_reference/`: son **LarkPlayer**, una app de otra empresa que se analizó solo como inspiración. Se borran en el PR de limpieza final.
