plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

group = "ru.surfstudio.otp_autofill"
version = "1.0-SNAPSHOT"

android {
    namespace = "ru.surfstudio.otp_autofill"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    sourceSets {
        named("main") {
            java.srcDirs("src/main/kotlin")
        }
        named("test") {
            java.srcDirs("src/test/kotlin")
        }
    }

    defaultConfig {
        minSdk = 19
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.1.0")
    implementation("androidx.activity:activity:1.9.2")
    implementation("androidx.fragment:fragment:1.8.3")
}