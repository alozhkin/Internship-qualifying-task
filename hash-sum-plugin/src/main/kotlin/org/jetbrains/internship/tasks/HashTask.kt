package org.jetbrains.internship.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.internship.utils.toHexString
import java.io.File
import java.security.MessageDigest

open class HashTask : DefaultTask() {

    val fileExtensions: ListProperty<String> = project.objects.listProperty(String::class.java)

    val outputFile: RegularFileProperty = project.objects.fileProperty()

    @TaskAction
    fun countHash() {
        val messageDigest = MessageDigest.getInstance("SHA-1")
        val outFile = outputFile.get().asFile
        val extensions = fileExtensions.get().map { ".$it" }
        val files = project.layout.files ({
            project.projectDir.walkTopDown().toList()
        })
        val suitableFiles = files.filter { file: File ->
            extensions.any { extension -> file.name.endsWith(extension) }
        }
        suitableFiles.forEach {
            messageDigest.update(it.readBytes())
        }
        outFile.writeText(messageDigest.digest().toHexString())
    }
}