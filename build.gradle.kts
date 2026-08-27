buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.13.1")
    }
}

plugins {
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}

tasks.register("agpTest") {
    doLast {
        println("AGP dependency resolved successfully")
    }
}
