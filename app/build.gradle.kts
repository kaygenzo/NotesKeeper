import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// ---------------------------------------------------------------------------
// Dev keystore auto-creation.
// Generates keystore/dev.jks with keytool on first build if it does not exist.
// ---------------------------------------------------------------------------
val devKeystoreFile: File = rootProject.file("keystore/dev.jks")
val devStorePassword = "noteskeeper"
val devKeyAlias = "dev"
val devKeyPassword = "noteskeeper"

val generateDevKeystore by tasks.registering {
    outputs.file(devKeystoreFile)
    onlyIf { !devKeystoreFile.exists() }
    doLast {
        devKeystoreFile.parentFile.mkdirs()
        val keytool = File(System.getProperty("java.home"), "bin/keytool").absolutePath
        val process = ProcessBuilder(
            keytool,
            "-genkeypair",
            "-keystore", devKeystoreFile.absolutePath,
            "-storepass", devStorePassword,
            "-alias", devKeyAlias,
            "-keypass", devKeyPassword,
            "-keyalg", "RSA",
            "-keysize", "2048",
            "-validity", "10000",
            "-dname", "CN=NotesKeeper Dev, O=Telen, C=FR",
        ).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "keytool failed: $output" }
        logger.lifecycle("Generated dev keystore at ${devKeystoreFile.absolutePath}")
    }
}

val keystoreProperties = Properties()
keystoreProperties.setProperty("storeFile", "../telen.release.jks")
keystoreProperties.setProperty("storePassword", System.getenv("KEYSTORE_PASSWORD").orEmpty())
keystoreProperties.setProperty("keyAlias", System.getenv("KEY_ALIAS").orEmpty())
keystoreProperties.setProperty("keyPassword", System.getenv("KEY_PASSWORD").orEmpty())

android {
    namespace = "com.telen.noteskeeper"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.telen.noteskeeper"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "com.telen.noteskeeper.TestRunner"
    }

    signingConfigs {
        create("dev") {
            storeFile = devKeystoreFile
            storePassword = devStorePassword
            keyAlias = devKeyAlias
            keyPassword = devKeyPassword
        }

        create("release") {
            storeFile = file(keystoreProperties.getProperty("storeFile"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("dev")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.named("preBuild") {
    dependsOn(generateDevKeystore)
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.coil.compose)
    implementation(libs.timber)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
