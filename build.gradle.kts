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
        classpath("org.jetbrains.internship:hash-sum-plugin")
    }
}

allprojects {
    apply(plugin = "org.jetbrains.internship")
}