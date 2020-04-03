package org.jetbrains.internship.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject


open class HashTask @Inject constructor(private val workerExecutor: WorkerExecutor) : DefaultTask() {

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
        val workQueue = workerExecutor.noIsolation()
        val outFiles = outputFiles.get()
        val ext = fileExtensions
        val inputDirs = inputDirectories.get()
        for ((index, inputDir) in inputDirs.withIndex()) {
            workQueue.submit(GenerateHash::class) {
                alg.set(algorithm.get())
                fileExt.set(fileExtensions.get())
                inputDirectory.set(inputDir)
                outputFile.set(outFiles[index])
            }
        }
    }
}