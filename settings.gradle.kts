pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    plugins {
        // ép TẤT CẢ plugin Kotlin xuống 1.9.23
        id("org.jetbrains.kotlin.android") version "1.9.23" apply false
        id("org.jetbrains.kotlin.jvm")     version "1.9.23" apply false
        // (nếu có dòng version 2.0.x hãy xoá hoặc đổi về 1.9.23)
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "QuanLyKhachSan"
include(":app")
