package org.jetbrains.internship

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.jetbrains.internship.utils.Algorithm
import org.jetbrains.internship.utils.toHexString
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class HashSumPluginTest {
    companion object {
        private const val HASH_SUM_COMMAND = "calculate"
        private const val HASH_SUM_FILENAME = "hash_sum.txt"
    }

    private val rootProjectDir = File(System.getProperty("user.dir")).parentFile
    private val hashSumPluginDir = rootProjectDir.resolve("hash-sum-plugin")
    private lateinit var newProjectDir: File
    private lateinit var srcDir: File
    private lateinit var javaDir: File
    private lateinit var kotlinDir: File
    private lateinit var buildDir: File

    @Before
    fun init() {
        newProjectDir = createTempDir()
        srcDir = File("$newProjectDir/src")
        buildDir = File("$newProjectDir/build")
        javaDir = File("$srcDir/java")
        kotlinDir = File("$srcDir/kotlin")
        srcDir.mkdir()
        buildDir.mkdir()
        javaDir.mkdir()
        kotlinDir.mkdir()
    }

    @After
    fun clean() {
        newProjectDir.deleteRecursively()
    }

    private fun run(
        algorithm: Algorithm? = null,
        arguments: MutableList<String> = mutableListOf(),
        fileExtensions: List<String>? = null,
        outputFileName: String? = null
    ): BuildResult {
        val buildCode = createBuildCode(algorithm, fileExtensions, outputFileName)
        return build(arguments, buildCode)
    }

    private fun build(arguments: MutableList<String>, buildCode: String): BuildResult {
        File("$newProjectDir/build.gradle.kts").writeText(buildCode)
        File("$newProjectDir/settings.gradle.kts")
        addSource()
        arguments.add(HASH_SUM_COMMAND)
        return GradleRunner.create()
            .withProjectDir(newProjectDir)
            .withArguments(arguments)
            .build()
    }

    private fun createBuildCode(
        algorithm: Algorithm? = null,
        fileExtensions: List<String>? = null,
        outputFileName: String? = null
    ): String {
        val configureAlgorithm = if (algorithm != null) "algorithm = \"$algorithm\"" else ""
        val configureOutputFileName = if (outputFileName != null) "outputFileName = \"$outputFileName\"" else ""
        val configureFileExtensions =
            if (fileExtensions != null) {
                val fe = fileExtensions.joinToString(prefix = "listOf(", postfix = ")") { "\"$it\"" }
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

    private fun addSource() {
        File("$javaDir/HelloWorld.java").writeText(
            """
            class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("hello world");
                }
            }
        """.trimIndent()
        )
        File("$kotlinDir/HelloWorld.kt").writeText(
            """
            fun main() {
                println("hello world")
            }
        """.trimIndent()
        )
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

    @Test
    fun shouldUseSha1ByDefault() {
        run()
        verifyAlgorithm(Algorithm.SHA1, getHashSumFile())
    }

    @Test
    fun shouldSupportAlgorithmConfiguration() {
        run(Algorithm.SHA512)
        verifyAlgorithm(Algorithm.SHA512, getHashSumFile())
    }

    @Test
    fun shouldSupportFileExtensionsConfiguration() {
        File("$srcDir/file.withStrangeFileExtension").writeText("test")
        run(fileExtensions = listOf("withStrangeFileExtension"))
        val md = MessageDigest.getInstance("SHA-1")
        val expected = md.digest("test".toByteArray()).toHexString()
        assertEquals("File contains wrong hash sum", expected, getHashSumFile().readText())
    }

    @Test
    fun shouldSupportOutputFileNameConfiguration() {
        run(outputFileName = "new_file.txt")
        checkFileExistence("new_file.txt")
    }
}