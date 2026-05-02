plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    applyDefaultHierarchyTemplate()

    android {
        namespace = "com.alvaroquintana.data"
        compileSdk = 36
        minSdk = 23

        withHostTestBuilder {}
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.gitlive.firebase.firestore)
            implementation(libs.gitlive.firebase.crashlytics)
        }
        getByName("androidMain").dependencies {
            implementation(libs.sqldelight.android.driver)
            // gitlive Firebase delegates to Firebase Android SDK; pin via BoM
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
        getByName("iosMain").dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        getByName("androidHostTest").dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

sqldelight {
    databases {
        create("AdivinaRazaDatabase") {
            packageName.set("com.alvaroquintana.data.db")
        }
    }
}
