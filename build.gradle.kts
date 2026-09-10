// Top-level build file. Individual module build files apply what they need.
buildscript {
    extra.apply {
        set("compileSdk", 34)
        set("targetSdk", 34)
        set("minSdk", 26)
        set("kotlinVersion", "1.9.24")
        set("composeCompilerVersion", "1.5.14")
        set("hiltVersion", "2.51.1")
    }
}

plugins {
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
