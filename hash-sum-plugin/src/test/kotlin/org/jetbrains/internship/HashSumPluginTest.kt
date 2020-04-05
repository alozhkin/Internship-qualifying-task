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
        val settingsCode = createSettingsCode()
        return build(arguments, buildCode, settingsCode)
    }

    private fun build(arguments: MutableList<String>, buildCode: String, settingsCode: String): BuildResult {
        File("$newProjectDir/build.gradle.kts").writeText(buildCode)
        File("$newProjectDir/settings.gradle.kts").writeText(settingsCode)
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
        plugins {
            id("org.jetbrains.internship") version("1.0.0")
        }
        configure<org.jetbrains.internship.HashSumPluginExtension> {
            $configureAlgorithm
            $configureFileExtensions
            $configureOutputFileName
        }
    """.trimIndent()
    }

    private fun createSettingsCode(): String {
        return """
            pluginManagement {
                repositories {
                    flatDir {
                        dirs("${hashSumPluginDir.resolve("repo").absolutePath.replace("\\","/")}")
                    }
                }
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

    private fun checkFileExistence(path: String) {
        assertTrue("Hash sum file was not created", File(path).exists())
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
        checkFileExistence("$buildDir/new_file.txt")
    }

    @Test
    fun shouldCalculateHashSumInSubprojects() {
        val settingsCode = """
            pluginManagement {
                repositories {
                    flatDir {
                        dirs("${hashSumPluginDir.resolve("repo").absolutePath.replace("\\","/")}")
                    }
                }
            }
            rootProject.name = "temp"
            include("subproject")
        """.trimIndent()
        val subDir = File("$newProjectDir/subproject")
        subDir.mkdir()
        File("$subDir/build.gradle.kts")
        File("$subDir/settings.gradle.kts").writeText(
            """
            rootProject.name = "subproject"
        """.trimIndent()
        )
        build(mutableListOf(), createBuildCode(), settingsCode)
        checkFileExistence("$subDir/build/$HASH_SUM_FILENAME")
    }
}
