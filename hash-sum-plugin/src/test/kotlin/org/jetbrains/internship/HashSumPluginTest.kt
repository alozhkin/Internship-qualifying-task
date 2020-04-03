package org.jetbrains.internship

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.jetbrains.internship.utils.Algorithm
import org.jetbrains.internship.utils.toHexString
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class HashSumPluginTest {
    companion object {
        private const val HASH_SUM_COMMAND = "calculate"
        private const val HASH_SUM_FILENAME = "hash_sum.txt"
    }

    private val rootProjectDir = File(System.getProperty("user.dir")).parentFile
    private val exampleProjectDir = rootProjectDir.resolve("example-project")
    private val hashSumPluginDir = rootProjectDir.resolve("hash-sum-plugin")
    private val buildDir = exampleProjectDir.resolve("build")

    private fun run(algorithm: Algorithm? = null,
                    arguments: MutableList<String> = mutableListOf(),
                    fileExtensions: List<String>? = null,
                    outputFileName: String? = null
    ) : BuildResult {
        val buildStr = createBuildStr(algorithm, fileExtensions, outputFileName)
        return build(arguments, buildStr)
    }

    private fun build(arguments: MutableList<String>, buildStr: String): BuildResult {
        File("${exampleProjectDir}/build.gradle.kts").writeText(buildStr)
        arguments.add(HASH_SUM_COMMAND)
        return GradleRunner.create()
            .withProjectDir(exampleProjectDir)
            .withArguments(arguments)
            .build()
    }

    private fun createBuildStr(algorithm: Algorithm? = null,
                               fileExtensions: List<String>? = null,
                               outputFileName: String? = null
    ): String {
        val configureAlgorithm = if (algorithm != null) "algorithm = \"$algorithm\"" else ""
        val configureOutputFileName = if (outputFileName != null) "outputFileName = \"$outputFileName\"" else ""
        val configureFileExtensions =
            if (fileExtensions != null) {
                val fe = fileExtensions.joinToString(prefix = "listOf(", postfix = ")") { "\"$it\""}
                "fileExtensions = $fe"
            } else {
                ""
            }
        return """
        buildscript {
            repositories {
                flatDir {
                    dirs("$hashSumPluginDir/build/libs")
                }
            }
            dependencies {
                classpath("org.jetbrains.internship:hash-sum-plugin:1.0.0")
            }
        }

        apply(plugin = "org.jetbrains.internship")

        configure<org.jetbrains.internship.HashSumPluginExtension> {
            $configureAlgorithm
            $configureFileExtensions
            $configureOutputFileName
        }
    """.trimIndent()
    }

    private fun getHashSumFile(outputFileName: String = HASH_SUM_FILENAME): File {
        return buildDir.resolve(outputFileName)
    }

    private fun verifyAlgorithm(algorithm: Algorithm, file: File) {
        assertEquals("Unknown hash function", algorithm, Algorithm.getAlgorithmWithCode(file.readText()))
    }

    private fun checkFileExistence(filename: String) {
        assertTrue("Hash sum file was not created", File("$buildDir/$filename").exists())
    }

    private fun checkOutcome(buildRes: BuildResult, taskOutcome: TaskOutcome) {
        assertEquals(taskOutcome, buildRes.task(":calculate")?.outcome)
    }

    @Test
    fun shouldUseSha1ByDefault() {
        run()
        verifyAlgorithm(Algorithm.SHA1, getHashSumFile())
    }

    @Test
    fun shouldSupportExtensionClassAlgorithm() {
        run(Algorithm.SHA512)
        verifyAlgorithm(Algorithm.SHA512, getHashSumFile())
    }

    @Test
    fun shouldSupportExtensionClassFileExtensions() {
        run(fileExtensions = listOf("withStrangeFileExtension"))
        val md = MessageDigest.getInstance("SHA-1")
        val expected = md.digest("test".toByteArray()).toHexString()
        assertEquals(expected, getHashSumFile().readText())
    }

    @Test
    fun shouldSupportExtensionClassOutputFileName() {
        run(outputFileName = "new_file.txt")
        checkFileExistence("new_file.txt")
    }

    @Test
    fun shouldSupportIncremental() {
        checkOutcome(run(Algorithm.SHA224), TaskOutcome.SUCCESS)
        checkOutcome(run(Algorithm.SHA224), TaskOutcome.UP_TO_DATE)
        checkOutcome(run(Algorithm.MD5), TaskOutcome.SUCCESS)
    }
}
