# Sren-source (zip del 25 oct 2026)

Variante de Sren en Kotlin, recuperada del archivo `Sren-source.zip` que estaba en Drive. Paquete `com.sren` (se instala aparte de la app actual).

El zip guardaba cada archivo con la ruta "aplanada" en el nombre (`app_src_main_java_com_sren_assistant_LocalRuleEngine.kt`). Se reconstruyeron las carpetas originales; cada archivo coincide con su `package`. Se añadió el lanzador de Gradle (`gradlew`, Gradle 8.7), que no venía.

## Lo más valioso: el asistente
- `assistant/AssistantOrchestrator.kt`: busca antes de decidir, pide aclaración si hay varias coincidencias, búsqueda "inteligente" con respaldo (entrenar → música energética; estudiar → calma).
- `assistant/ToolRegistry.kt`: **lista de acciones permitidas**; el asistente no puede ejecutar nada fuera de ella.
- `core/TextNormalizer.kt`: compara sin tildes ni mayúsculas ("canción" = "cancion").
- `core/SrenError.kt`: errores con nombre (permiso denegado, no encontrado, acción inválida…).
- Pruebas: `LocalRuleEngineTest`, `PlaylistRepositoryTest`, `SearchSongsUseCaseTest`, `FakePlayerControllerTest`.

Los proveedores `OpenAiEngine`, `GeminiEngine`, `OllamaEngine` y `AlexaBridgeEngine` están vacíos (solo devuelven "no implementado").

## Nota
El mismo zip traía el README de **Aura Player** (otra app), guardado en [`../aura-player/`](../aura-player). La carpeta de código `com/codex/auraplayer` venía **vacía**.

## Correcciones para que compile (2026-10-29)
El código recuperado **no compilaba**: `CardBlock` usaba `Column.()` como tipo del receptor en `presentation/MainActivity.kt`. Se cambió por `ColumnScope.()` (una línea, marcada con un comentario). Con eso compila y sus 6 pruebas pasan.

## Cómo compilarla
```bash
cd legado/sren-source
echo "sdk.dir=$ANDROID_HOME" > local.properties
JAVA_HOME=<ruta a JDK 21> ./gradlew :app:assembleDebug :app:testDebugUnitTest
```
