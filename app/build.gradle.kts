plugins {
    // 1. Сначала Android и Kotlin плагины (ОБЯЗАТЕЛЬНО)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // 2. Потом плагины-обработчики (они ищут Android-конфигурацию)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.readspread"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.readspread"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ✅ ОДИН блок compileOptions с Java 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // ✅ Kotlin настройки (обязательно!)
    kotlinOptions {
        jvmTarget = "17"
    }

    // ✅ Compose настройки
    buildFeatures {
        compose = true
    }

    // ✅ Compose Compiler версия (для Kotlin 1.9.22)
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

// ✅ ОДИН объединённый блок зависимостей
dependencies {
    // === Core Android ===
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // === Compose (используем BOM для управления версиями) ===
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // === Navigation ===
    implementation(libs.androidx.navigation.compose)

    // === Room ===
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // === Hilt ===
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)

    // === Coroutines ===
    implementation(libs.kotlinx.coroutines.android)

    // === Testing ===
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}