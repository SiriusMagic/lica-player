# Compilar Stone y seguir programando

Todo lo necesario para **reconstruir la app desde cero, saber dónde queda cada APK y continuar el código** sin perder nada.

## Requisitos
- **JDK 21.** Gradle 8.11 no funciona con JDK 25.
- **Android SDK** con la plataforma 35. La ruta va en `stone/local.properties` (`sdk.dir=...`), que no se sube a git.
- En el Codespace ya está todo: el SDK en `/home/codespace/android-sdk` y el JDK 21 en `/home/codespace/java/21.0.12+1-ms`.

## Comandos (desde `stone/`)

```bash
cd sren
export JAVA_HOME=/home/codespace/java/21.0.12+1-ms   # en el Codespace

# Pruebas unitarias
./gradlew --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process :app:testDebugUnitTest

# APK de prueba (rápido, sin optimizar, ~20 MB)
./gradlew --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process :app:assembleDebug

# APK final (optimizado con R8, ~3 MB). Necesita ~5 GB de memoria libre.
./gradlew --no-daemon --max-workers=1 -Pkotlin.compiler.execution.strategy=in-process :app:assembleRelease
```

Las opciones `--max-workers=1` e `in-process` existen porque el Codespace tiene poca memoria. En una PC normal basta con `./gradlew :app:assembleDebug`.

**"Gradle build daemon disappeared"** significa que se acabó la memoria. Cierra otras compilaciones y repite.

## Dónde queda cada compilación

| Qué | Dónde |
|---|---|
| APK de prueba | `stone/app/build/outputs/apk/debug/app-debug.apk` |
| APK final | `stone/app/build/outputs/apk/release/app-release.apk` |
| Resultados de las pruebas | `stone/app/build/reports/tests/testDebugUnitTest/index.html` |
| **Copia guardada por versión** (Codespace) | `/apk/vX.Y.Z/stone-X.Y.Z.apk`, explicado en `apk/README.md` |
| **Copia permanente** (GitHub) | Pestaña **Releases** del repositorio, una por versión |
| APK de cada PR | Pestaña **Actions** → la ejecución "App Android" del PR → *Artifacts* (30 días) |

`stone/app/build/` se borra y regenera sola; nunca guardes nada a mano ahí.

## Publicar una versión nueva
1. Sube `versionCode` (+1) y `versionName` en `stone/app/build.gradle.kts`.
2. Añade la sección `## [X.Y.Z] - fecha` en `CHANGELOG.md`.
3. Fusiona el PR a `main`.
4. Crea y sube la etiqueta: `git tag vX.Y.Z && git push origin vX.Y.Z`.
5. GitHub compila el APK final y crea la Release sola (flujo `.github/workflows/publicar-apk.yml`).
6. Copia ese APK a `/apk/vX.Y.Z/` del Codespace.

## Firma (para poder actualizar sin desinstalar)
Android solo actualiza una app si cada versión está firmada con **la misma clave**.
- **Sin clave propia**, el APK final se firma con la clave de prueba de la máquina que compila. Las del Codespace y las de GitHub son distintas, así que para pasar de una a otra hay que desinstalar.
- **Con clave propia:**
  1. Crea la clave una sola vez: `keytool -genkeypair -v -keystore sren-release.jks -alias sren -keyalg RSA -keysize 4096 -validity 36500`.
  2. **Guarda el archivo `.jks` y sus contraseñas en un lugar privado** (gestor de contraseñas o Drive privado). Si se pierden, no se puede volver a actualizar la app.
  3. En GitHub → Settings → Secrets and variables → Actions, crea: `SREN_KEYSTORE_BASE64` (el resultado de `base64 -w0 sren-release.jks`), `SREN_KEYSTORE_PASSWORD`, `SREN_KEY_ALIAS` (`sren`) y `SREN_KEY_PASSWORD`.
  4. Para firmar en el Codespace con la misma clave, exporta `SREN_KEYSTORE_FILE=/ruta/sren-release.jks` y las otras tres variables antes de `assembleRelease`.
- **Nunca** subas el `.jks` ni las contraseñas al repositorio: `.gitignore` ya los bloquea.

## Seguir programando
1. Lee [`ARQUITECTURA.md`](ARQUITECTURA.md): dice en qué archivo va cada cosa.
2. Trabaja en una rama: `git checkout -b mi-cambio origin/main`.
3. Respeta las reglas del proyecto:
   - **Batería:** nada en segundo plano sin necesidad, nada de temporizadores que revisen cada segundo, y leer datos solo mientras la pantalla se ve.
   - **Textos** en `res/values*/strings.xml` (inglés y español; los demás idiomas cuando lleguen), nunca escritos en el código.
   - **`stats.db`** (datos del usuario): cada cambio de tablas necesita una migración en `data/user/UserDatabase.kt`.
   - **Gestos:** documéntalos en [`GESTOS.md`](GESTOS.md).
   - **Librerías:** solo si se justifican; quita lo que no se use.
4. Compila sin errores **ni advertencias**, y corre las pruebas.
5. Abre un PR a `main`. La CI corre las pruebas y deja el APK de prueba.
6. Anota el cambio en `CHANGELOG.md` (sección "Sin publicar").

## Versiones antiguas
Su código y cómo compilarlas está en [`../legado/`](../legado). La historia, en [`VERSIONES.md`](VERSIONES.md).
