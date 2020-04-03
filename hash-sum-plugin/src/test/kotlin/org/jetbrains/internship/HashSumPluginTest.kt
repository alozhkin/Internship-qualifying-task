package org.jetbrains.internship

import org.gradle.testkit.runner.GradleRunner
import org.jetbrains.internship.utils.Algorithm
import org.jetbrains.internship.utils.toHexString
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class HashSumPluginTest {

    private val rootProjectDir = File(System.getProperty("user.dir")).parentFile

    private val exampleProjectDir = rootProjectDir.resolve("example-project")

    private val hashSumPluginDir = rootProjectDir.resolve("hash-sum-plugin")

    private val buildDir = exampleProjectDir.resolve("build")


    private fun verify(file: File, algorithm: Algorithm) {
        assertEquals("Unknown hash function", algorithm, Algorithm.getAlgorithmWithCode(file.readText()))
    }

    private fun run(algorithm: Algorithm? = null,
                    fileExtensions: List<String>? = null,
                    outputFileName: String = "hash_sum.txt"): File {
        createBuildFile(algorithm, fileExtensions, outputFileName)
        val result = GradleRunner.create()
            .withProjectDir(exampleProjectDir)
            .withArguments("calculate")
            .build()
        val hashSumFile = buildDir.resolve(outputFileName)
        assertTrue("Hash sum file was not created", hashSumFile.exists())
        return hashSumFile
    }

    fun createBuildFile(algorithm: Algorithm? = null,
                        fileExtensions: List<String>? = null,
                        outputFileName: String? = null) {
        val configureAlgorithm = if (algorithm != null) "algorithm = \"$algorithm\"" else ""
        val configureOutputFileName = if (outputFileName != null) "outputFileName = \"$outputFileName\"" else ""
        val configureFileExtensions =
            if (fileExtensions != null) {
                val fe = fileExtensions.joinToString(prefix = "listOf(", postfix = ")") { "\"it\""}
                "fileExtensions = $fe"
            } else {
                ""
            }
        File("${exampleProjectDir}/build.gradle.kts").writeText("""
        buildscript {
            repositories {
                flatDir {
                    dirs("${hashSumPluginDir}/build/libs")
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
        
        println("ex")
    """.trimIndent())
    }

    @Test
    fun shouldUseSha1ByDefault() {
        val file = run()
        verify(file, Algorithm.SHA1)
    }

    @Test
    fun shouldSupportExtensionClassAlgorithm() {
        val hashSumFile = run(Algorithm.SHA256)
        verify(hashSumFile, Algorithm.SHA256)
    }

    @Test
    fun shouldSupportExtensionClassFileExtensions() {
        val hashSum = run(fileExtensions = listOf("withStrangeFileExtension")).readText()
        val md = MessageDigest.getInstance("SHA-1")
        val expected = md.digest("test".toByteArray()).toHexString()
        assertEquals(expected, hashSum)
    }

    @Test
    fun shouldSupportExtensionClassOutputFileName() {
        val hashSumFile = run(outputFileName = "new_file.txt")
        assertEquals("new_file.txt", hashSumFile.name)
    }
}
