// Reconstruido el 2026-10-29: el zip de Codex no traía los archivos raíz del proyecto.
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "SrenKotlin"
include(":app")
