package org.jetbrains.internship

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.internship.tasks.HashTask
import java.io.File

class HashSumPlugin : Plugin<Project> {
    override fun apply(p0: Project) {
        p0.tasks.create("calculateSha1", HashTask::class.java) {
            outputFile.set(File("build/hash_sum.txt"))
            fileExtensions.set(listOf("kt", "java"))
        }
    }
}