import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.bscompanion.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bscompanion.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 74
        versionName = "1.68.0"
        // Jedno źródło URL aktualizacji — reszta czyta BuildConfig.UPDATE_URL.
        // Surowy plik z main, żeby apka nie czekała na Pages.
        buildConfigField(
            "String", "UPDATE_URL",
            "\"https://raw.githubusercontent.com/royallorde-maker/bs-companion/main/backend/api/version.json\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Podpis release: utwórz keystore.properties (nie commituj) z:
            // storeFile=..., storePassword=..., keyAlias=..., keyPassword=...
            // Klucz podpinamy gdy jest; gdy go NIE ma, taski pakujące release
            // (packageRelease/bundleRelease) padają z jawnym błędem w CZASIE
            // wykonania — precyzyjnie tam, gdzie podpis jest potrzebny.
            // Debug/testy i parasolki (assemble/build/check) bez release
            // w grafie działają bez klucza. Cichy unsigned APK łamie OTA.
            val ksPropsFile = rootProject.file("keystore.properties")
            if (ksPropsFile.exists()) {
                signingConfigs {
                    create("release") {
                        val ksProps = Properties()
                        FileInputStream(ksPropsFile).use { ksProps.load(it) }
                        storeFile = file(ksProps["storeFile"] as String)
                        storePassword = ksProps["storePassword"] as String
                        keyAlias = ksProps["keyAlias"] as String
                        keyPassword = ksProps["keyPassword"] as String
                    }
                }
                signingConfig = signingConfigs.getByName("release")
            } else {
                logger.warn("Brak keystore.properties — packageRelease/bundleRelease padnie z błędem; reszta działa.")
                tasks.matching { it.name == "packageRelease" || it.name == "bundleRelease" }.configureEach {
                    doFirst {
                        throw GradleException(
                            "Brak keystore.properties — release MUSI być podpisany tym samym kluczem. " +
                                "Utwórz plik wg backend/AGENTS.md; nie omijaj tego (unsigned APK łamie aktualizacje)."
                        )
                    }
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.0.21")
}
