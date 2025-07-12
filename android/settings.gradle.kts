pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    
    plugins {
        id("com.android.library") version "8.5.1"
        id("org.jetbrains.kotlin.android") version "1.9.23"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "otp_autofill"