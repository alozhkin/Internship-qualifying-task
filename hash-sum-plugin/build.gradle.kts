plugins {
    java
    kotlin("jvm") version("1.3.71")
    `kotlin-dsl`
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    testImplementation(gradleTestKit())
    testImplementation("junit", "junit", "4.12")
    implementation(kotlin("stdlib-jdk8"))
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

version = "1.0.0"