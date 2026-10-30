// Reconstruido el 2026-10-29. Versiones deducidas de app/build.gradle.kts:
// Compose compiler 1.5.14 → Kotlin 1.9.24; KSP a juego; AGP 8.5 funciona con Gradle 8.7.
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}
