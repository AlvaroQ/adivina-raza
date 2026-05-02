@file:Suppress("DEPRECATION")

import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

fun getSecretValue(name: String): String =
    try {
        val secretsFile = file("./secrets/secrets.xml")
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(secretsFile)
        val nodes = doc.getElementsByTagName("string")
        (0 until nodes.length)
            .map { nodes.item(it) }
            .first { it.attributes.getNamedItem("name")?.nodeValue == name }
            .textContent
    } catch (_: Exception) {
        ""
    }

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "AdivinaRazaShared"
            isStatic = true
            export(project(":core"))
            export(project(":data"))
            export(project(":usecases"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(project(":data"))
            api(project(":usecases"))
            implementation(libs.kotlin.stdlib)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Coil 3 (multiplatform)
            implementation(libs.coil.compose)

            // Lifecycle ViewModel (multiplatform)
            implementation(libs.androidx.lifecycle.viewmodel)

            // Koin multiplatform
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navigation Compose (multiplatform — JetBrains fork)
            implementation(libs.jetbrains.navigation.compose)
        }
        getByName("androidMain").dependencies {
            implementation(libs.kotlinx.coroutines.android)

            // AndroidX
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.androidx.preference.ktx)
            implementation(libs.androidx.swiperefreshlayout)

            // Firebase (Android SDK directly + gitlive bridge)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.crashlytics)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.crashlytics)
            implementation(libs.guava.listenablefuture)

            // DI
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.koin.compose.viewmodel)

            // Images
            implementation(libs.coil.compose)
            implementation(libs.coil.network.okhttp)

            // Ads
            implementation(libs.play.services.ads)
            implementation(libs.ump)

            implementation(libs.androidx.material3.window.size.class1)

            // Jetpack Compose
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(libs.compose.material3)
            implementation(libs.compose.material3.window.size)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.navigation.compose)
        }
        getByName("iosMain").dependencies {
            // Coil on iOS needs a network backend to resolve http/https URLs.
            implementation(libs.coil.network.ktor3)
            implementation(libs.ktor.client.darwin)
        }
        getByName("androidUnitTest").dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        getByName("androidInstrumentedTest").dependencies {
            implementation(project.dependencies.platform(libs.compose.bom))
            implementation(libs.compose.ui.test.junit4)
            implementation(libs.test.ext.junit)
            implementation(libs.espresso.core)
            implementation(libs.test.runner)
            implementation(libs.test.rules)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    compileSdk = 36
    namespace = "com.alvaroquintana.adivinaperro"

    defaultConfig {
        applicationId = "com.alvaroquintana.adivinaperro"
        minSdk = 23
        targetSdk = 36
        versionCode = 34
        versionName = "3.0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            keyAlias = findProperty("ADIVINAPERRO_RELEASE_KEY_ALIAS") as? String
            keyPassword = findProperty("ADIVINAPERRO_RELEASE_KEY_PASSWORD") as? String
            storeFile = (findProperty("ADIVINAPERRO_RELEASE_STORE_FILE") as? String)?.let { file(it) }
            storePassword = findProperty("ADIVINAPERRO_RELEASE_STORE_PASSWORD") as? String
        }
    }

    buildTypes {
        debug {
            isJniDebuggable = true
            isDebuggable = true
            isMinifyEnabled = false
            configure<CrashlyticsExtension> { mappingFileUploadEnabled = false }

            resValue("string", "admob_id", getSecretValue("admob_id"))
            resValue("string", "BANNER_GAME", getSecretValue("admob_banner_test_id"))
            resValue("string", "BANNER_INFO", getSecretValue("admob_banner_test_id"))
            resValue("string", "BONIFICADO_GAME", getSecretValue("admob_bonificado_test_id"))
            resValue("string", "BONIFICADO_GAME_OVER", getSecretValue("admob_bonificado_test_id"))
            resValue("string", "INTERSTICIAL_GAME_OVER", "ca-app-pub-3940256099942544/1033173712")

            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            isDebuggable = false
            ndk { debugSymbolLevel = "FULL" }
            signingConfig = signingConfigs.getByName("release")
            configure<CrashlyticsExtension> { mappingFileUploadEnabled = true }

            resValue("string", "admob_id", getSecretValue("admob_id"))
            resValue("string", "BANNER_GAME", getSecretValue("admob_banner_game"))
            resValue("string", "BANNER_INFO", getSecretValue("admob_banner_info"))
            resValue("string", "BONIFICADO_GAME", getSecretValue("admob_bonificado_game"))
            resValue("string", "BONIFICADO_GAME_OVER", getSecretValue("admob_bonificado_game_over"))
            resValue("string", "INTERSTICIAL_GAME_OVER", getSecretValue("admob_intersticial"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = true
        resValues = true
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

configurations.all {
    exclude(group = "com.google.android.gms", module = "play-services-safetynet")
}
