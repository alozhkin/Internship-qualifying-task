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
}

class HashSumPlugin : Plugin<Project> {
    override fun apply(p0: Project) {
        val ext = p0.extensions.create<HashSumPluginExtension>("greeting")

        val inputDirs = mutableListOf<Directory>()
        val outFiles = mutableListOf<RegularFile>()
        for (project in p0.allprojects) {
            val projectDir = project.layout.projectDirectory;
            inputDirs.add(projectDir)
            outFiles.add((projectDir.file("build/hash_sum.txt")))
        }
        p0.tasks.addRule("Pattern: calculate<ID>") {
            val taskName = this
            if (startsWith("calculate")) {
                p0.tasks.create(taskName, HashTask::class.java) {
                    inputDirectories.set(inputDirs)
                    outputFiles.set(outFiles)
                    fileExtensions.set(ext.fileExtensions)
                    val alg = taskName.substring(9)
                    if (alg != "") {
                        algorithm.set(alg)
                    } else {
                        algorithm.set(ext.algorithm)
                    }
                }
            }
        }
    }
}
