import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}
compose {
    resources {
        publicResClass = true
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)

            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.bundles.ktor)

            api(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.coil.compose)
            implementation(libs.coil.mp)
            implementation(libs.coil.svg)
            implementation(libs.coil.network.ktor3)

            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            implementation(libs.zoomimage.compose)
            implementation(libs.qrcode.kotlin)



        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)

            implementation(libs.ktor.client.okhttp)

            implementation(libs.koin.android)
            implementation(libs.play.services.location)

            // ML Kit Text Recognition for OCR
            implementation("com.google.mlkit:text-recognition:16.0.1")

            // ML Kit Document Scanner — edge detection + perspective correction scanning UI
            implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.coil.network.ktor3)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}


android {
    namespace = "com.siteflow.signature"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.siteflow.signature"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            manifestPlaceholders["firebaseAnalyticsDebugMode"] = "true"
        }
        create("uat") {
            initWith(getByName("debug"))
            manifestPlaceholders["firebaseAnalyticsDebugMode"] = "true"
            matchingFallbacks += listOf("debug")
        }
        getByName("release") {
            isMinifyEnabled = false
            manifestPlaceholders["firebaseAnalyticsDebugMode"] = "false"
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)

    // Firebase (must use top-level dependencies block for platform/BOM in KMP)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
}

