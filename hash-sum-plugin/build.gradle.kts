plugins {
    base
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
}

version = "1.0.0"

tasks.jar {
    destinationDirectory.set(file("$projectDir/repo"))
    archiveBaseName.set("org.jetbrains.internship.gradle.plugin")
}