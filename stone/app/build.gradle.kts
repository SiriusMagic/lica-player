plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.zirakusa.stone"
    // Compose 2026.09 pide compilar contra Android 37; targetSdk no cambia (la app se comporta igual).
    compileSdk = 37

    defaultConfig {
        applicationId = "com.zirakusa.stone"
        minSdk = 25
        targetSdk = 35
        // Subir los dos en cada versión publicada (ver docs/COMPILAR.md y CHANGELOG.md).
        versionCode = 20
        versionName = "1.4.0-alfa.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // Release key from environment variables (GitHub secrets in CI, or exported in the
    // Codespace). Never committed. Without them, release builds fall back to the debug key.
    val releaseKeystore = System.getenv("SREN_KEYSTORE_FILE")?.let(::file)?.takeIf { it.exists() }
    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = System.getenv("SREN_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("SREN_KEY_ALIAS")
                keyPassword = System.getenv("SREN_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    // Room writes each UserDatabase schema version here, so migrations can be reviewed.
    ksp { arg("room.schemaLocation", "$projectDir/schemas") }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // Solo los 6 idiomas de Stone: quita las traducciones de las librerías (APK más pequeño).
    // AGP 9: localeFilters reemplaza a resourceConfigurations.
    androidResources { localeFilters += listOf("en", "es", "pt", "fr", "it", "de") }
    buildFeatures { compose = true; buildConfig = true }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    implementation(libs.androidx.palette.ktx)

    testImplementation(libs.junit)
}

// AGP 9 trae Kotlin incluido: las opciones del compilador van aquí (antes, kotlinOptions dentro de android).
kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}
