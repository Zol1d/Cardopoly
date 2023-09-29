import org.jetbrains.kotlin.konan.properties.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("plugin.serialization")
}

android {
    namespace = "cool.zolid.cardopoly"
    compileSdk = 34
    val versionPropsFile = file("version.properties")
    val versionProps = Properties()
    var code = 0
    if (versionPropsFile.canRead()) {
        versionProps.load(FileInputStream(versionPropsFile))
        code = versionProps["VERSION_CODE"].toString().toIntOrNull()?.plus(1) ?: 1
    }
    versionProps["VERSION_CODE"] = code.toString()
    versionProps.store(versionPropsFile.writer(), null)
    defaultConfig {
        applicationId = "cool.zolid.cardopoly"
        minSdk = 28
        //noinspection OldTargetApi
        targetSdk = 33
        versionCode = code
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
        debug {
            isDebuggable = true
            versionNameSuffix = "-debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("androidx.navigation:navigation-compose:2.7.3")
    implementation("org.mariuszgromada.math:MathParser.org-mXparser:5.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
}