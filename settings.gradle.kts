pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    plugins {
        id("org.jetbrains.kotlin.android") version "1.9.23" apply false
        id("org.jetbrains.kotlin.jvm")     version "1.9.23" apply false
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "QuanLyKhachSan"
include(":app")
