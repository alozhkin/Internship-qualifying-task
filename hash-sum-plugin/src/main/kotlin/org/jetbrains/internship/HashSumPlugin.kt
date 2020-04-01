package org.jetbrains.internship

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.jetbrains.internship.tasks.HashTask

class HashSumPlugin : Plugin<Project> {
    override fun apply(p0: Project) {
        val inputDirs = mutableListOf<Directory>()
        val outFiles = mutableListOf<RegularFile>()
        for (project in p0.allprojects) {
            val projectDir = project.layout.projectDirectory;
            inputDirs.add(projectDir)
            outFiles.add((projectDir.file("build/hash_sum.txt")))
        }
        p0.tasks.create("calculateSha1", HashTask::class.java) {
            inputDirectories.set(inputDirs)
            outputFiles.set(outFiles)
            fileExtensions.set(listOf("kt", "java"))
        }
    }
}