package org.jetbrains.internship.tasks

import org.gradle.workers.WorkAction
import org.jetbrains.internship.utils.normalizeToAlgorithm
import org.jetbrains.internship.utils.toHexString
import java.security.MessageDigest

abstract class GenerateHash : WorkAction<HashSumWorkParameters> {
    override fun execute() {
        val messageDigest = MessageDigest.getInstance(parameters.alg.get().normalizeToAlgorithm())
        val suitableFiles = parameters.inputDirectory.asFileTree.files.filter { file ->
            parameters.extensions.get().map { ".$it" }.any { extension -> file.name.endsWith(extension) }
        }
        suitableFiles.forEach {
            messageDigest.update(it.readBytes())
        }
        parameters.outputFile.asFile.get().writeText(messageDigest.digest().toHexString())
    }
}