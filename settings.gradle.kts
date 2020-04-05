pluginManagement {
    repositories {
        maven("https://teamcity.jetbrains.com/guestAuth/app/rest/builds/buildType:(id:Kotlin_KotlinPublic_Compiler),number:1.4.0-dev-3529,branch:(default:any)/artifacts/content/maven")
        flatDir {
            dirs("hash-sum-plugin/repo")
        }
        mavenCentral()

        maven("https://plugins.gradle.org/m2/")
    }
}
rootProject.name = "internship"
include("hash-sum-plugin", "example-project")