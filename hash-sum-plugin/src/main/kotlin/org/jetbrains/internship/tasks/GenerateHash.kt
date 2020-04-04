package org.jetbrains.internship.tasks

import org.gradle.workers.WorkAction
import org.jetbrains.internship.utils.normalizeToAlgorithm
import org.jetbrains.internship.utils.toHexString
import java.security.MessageDigest
import javax.inject.Inject

abstract class GenerateHash @Inject constructor() : WorkAction<HashSumWorkParameters> {
    override fun execute() {
        with(parameters) {
            val messageDigest = MessageDigest.getInstance(alg.get().normalizeToAlgorithm())
            val suitableFiles = inputDirectory.asFileTree.files.filter { file ->
                fileExt.get().any { extension -> file.name.endsWith(extension) }
            }
            suitableFiles.forEach {
                messageDigest.update(it.readBytes())
            }
            outputFile.asFile.get().writeText(messageDigest.digest().toHexString())
        }
    }
}