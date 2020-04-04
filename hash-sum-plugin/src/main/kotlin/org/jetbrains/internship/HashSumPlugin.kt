package org.jetbrains.internship

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.kotlin.dsl.create
import org.jetbrains.internship.tasks.HashTask

open class HashSumPluginExtension {
    var algorithm = "SHA-1"
    var fileExtensions = listOf("kt", "java")
    var outputFileName = "hash_sum.txt"
}

class HashSumPlugin : Plugin<Project> {
    override fun apply(p0: Project) {
        val ext = p0.extensions.create<HashSumPluginExtension>("greeting")

        p0.tasks.register("calculate", HashTask::class.java) {
            val inputDirs = mutableListOf<Directory>()
            val outFiles = mutableListOf<RegularFile>()
            for (project in p0.allprojects) {
                val projectDir = project.layout.projectDirectory
                inputDirs.add(projectDir.dir("src"))
                outFiles.add((projectDir.file("build/" + ext.outputFileName)))
            }
            inputDirectories.set(inputDirs)
            outputFiles.set(outFiles)
            fileExtensions.set(ext.fileExtensions)
            algorithm.set(ext.algorithm)
        }
    }
}
