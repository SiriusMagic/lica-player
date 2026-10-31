# Inventario: qué es cada carpeta

Qué hay en el repositorio y en el Codespace, si **forma parte de la app** y si **está guardado en git**. Si algo no aparece aquí, no debería existir: documéntalo o bórralo.

## En el repositorio (GitHub)

| Ruta | ¿Parte de la app? | Qué es |
|---|---|---|
| `stone/` | **Sí, es la app** | Código de Stone 1.4 (Android). Ver [`ARQUITECTURA.md`](ARQUITECTURA.md). |
| `stone/app/schemas/` | Sí | Esquemas de `stats.db` por versión, para revisar migraciones. Se generan al compilar; se suben a git. |
| `docs/` | Documentación | Arquitectura, gestos, compilar, versiones, este inventario y `diseno/` (fotos de diseño). |
| `CHANGELOG.md` | Documentación | Cambios de cada versión publicada. |
| `legado/stone/` | No | Código de **Stone 1.3.2**, la referencia de aspecto y funciones. Las versiones anteriores están en la etiqueta `legado-2026-10`. |
| `.github/` | No (automatización) | CI: `android.yml` (compila la app en cada PR), `publicar-apk.yml` (Release con APK al crear una etiqueta `v*`), Dependabot (librerías de la app). |
| `README.md`, `LICENSE`, `SECURITY.md`, `CLAUDE.md` | No | Presentación, licencia Apache 2.0, cómo reportar fallos y notas para el asistente de código. |

## Solo en el Codespace (no se sube a git)

| Ruta | ¿Parte de la app? | Qué es |
|---|---|---|
| `apk/` | No | **Todos los APK**, por versión. Ver `apk/README.md`. Solo se sube a git `apk/README.md`. |
| `stone/local.properties` | No | Ruta del Android SDK de esta máquina. |
| `stone/app/build/` | No | Resultados de compilación; se regeneran solos. |
| `~/.gradle/` | No | Caché de Gradle (se puede borrar; se vuelve a descargar). |

## Fuera del Codespace

| Dónde | Qué es | Qué hacer |
|---|---|---|
| Drive: zip de la carpeta **Codex** (2,1 GB) | Copia de `Documents\Codex` de la PC | Lo propio ya se extrajo y se guardó en git. Borrarlo o dejarlo **restringido**. |
| Drive: **Sren-source.zip** | Variante Sren-source y el README de Aura Player | Ya extraído. Borrarlo o dejarlo **restringido**. |
| Repositorio **pinguino-music** | Pingüino Music (web) | Se mantiene como repositorio aparte. |

## Eliminado (oct 2026)
- Versiones anteriores, `herramientas/` (SARAP y cuaderno de Colab), sus pruebas, la copia local de REA y los APK de LarkPlayer. Lo propio sigue en la etiqueta `legado-2026-10`.
