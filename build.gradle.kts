plugins {
    kotlin("jvm") version "1.3.70"
}

repositories {
    jcenter()
}

buildscript {
    repositories {
        flatDir {
            dirs("hash-sum-plugin/build/libs")
        }
    }
    dependencies {
        classpath("org.jetbrains.internship:hash-sum-plugin:1.0.0")
    }
}

apply(plugin = "org.jetbrains.internship")