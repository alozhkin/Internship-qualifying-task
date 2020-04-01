package org.jetbrains.internship.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.jetbrains.internship.utils.normalizeToAlgorithm
import org.jetbrains.internship.utils.toHexString
import java.security.MessageDigest

open class HashTask : DefaultTask() {

    @InputFiles
    val inputDirectories: ListProperty<Directory> = project.objects.listProperty(Directory::class.java)

    @Input
    val fileExtensions: ListProperty<String> = project.objects.listProperty(String::class.java)

    @Input
    @Option(option = "algorithm", description = "Chooses algorithm from available in MessageDigest.java")
    val algorithm: Property<String> = project.objects.property(String::class.java)

    @OutputFiles
    val outputFiles: ListProperty<RegularFile> = project.objects.listProperty(RegularFile::class.java)

    @TaskAction
    fun countHash() {
        val messageDigest = MessageDigest.getInstance(algorithm.get().normalizeToAlgorithm())
        val outFiles = outputFiles.get().map { it.asFile }
        val extensions = fileExtensions.get().map { ".$it" }
        val inputDirs = inputDirectories.get().map { it.asFileTree }
        for ((index, inputDir) in inputDirs.withIndex()) {
            val suitableFiles = inputDir.filter { file ->
                extensions.any { extension -> file.name.endsWith(extension) }
            }
            suitableFiles.forEach {
                messageDigest.update(it.readBytes())
            }
            outFiles[index].writeText(messageDigest.digest().toHexString())
        }
    }
}