package org.jetbrains.internship

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jetbrains.internship.utils.Algorithm
import org.jetbrains.internship.utils.toHexString
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class HashSumPluginTest {
    companion object {
        val HASH_SUM_FILE_PATH: String
            get() = "hash_sum.txt"
        const val TASK_NAME = "calculate"
    }

    private val exampleProjectDir: File =
        File(System.getProperty("user.dir")).parentFile.resolve("example-project")

    private val project: Project = ProjectBuilder.builder().withProjectDir(exampleProjectDir).build()

    private fun verify(expectedAlg: Algorithm) {
        val hashSumFile = run(HASH_SUM_FILE_PATH)
        assertEquals("Unknown hash function", expectedAlg, Algorithm.getAlgorithmWithCode(hashSumFile.readText()))
    }

    private fun run(outputFileName: String = HASH_SUM_FILE_PATH): File {
        val calculateTask = project.tasks.findByPath(TASK_NAME)
        assertNotNull("Task with name $TASK_NAME does not exists", calculateTask)
        calculateTask!!.actions.forEach {
            it.execute(calculateTask)
        }
        val hashSumFile = project.buildDir.resolve(outputFileName)
        assertTrue("Hash sum file was not created", hashSumFile.exists())
        return hashSumFile
    }

    private fun extend(algorithm: Algorithm? = null,
                       fileExtensions: List<String>? = null,
                       outputFileName: String? = null
    ) {
        val ext = project.extensions.findByType(HashSumPluginExtension::class.java)
        assertNotNull("Extension was not found", ext)
        if (algorithm != null) {
            ext!!.algorithm = algorithm.name
        }
        if (fileExtensions != null) {
            ext!!.fileExtensions = fileExtensions
        }
        if (outputFileName != null) {
            ext!!.outputFileName = outputFileName
        }
    }

    @Before
    fun setUp() {
        project.plugins.apply(HashSumPlugin::class.java)
    }

    @Test
    fun basicTest() {
        with(project.plugins) {
            assertTrue(hasPlugin(HashSumPlugin::class.java))
        }
    }

    @Test
    fun shouldUseSha1ByDefault() {
        verify(Algorithm.SHA1)
    }

    @Test
    fun shouldSupportExtensionClassAlgorithm() {
        extend(Algorithm.SHA256)
        verify(Algorithm.SHA256)
    }

    @Test
    fun shouldSupportExtensionClassFileExtensions() {
        extend(fileExtensions = listOf("withStrangeFileExtension"))
        val hashSum = run().readText()
        val md = MessageDigest.getInstance("SHA-1")
        val expected = md.digest("test".toByteArray()).toHexString()
        assertEquals(expected, hashSum)
    }

    @Test
    fun shouldSupportExtensionClassOutputFileName() {
        extend(outputFileName = "new_file.txt")
        run("new_file.txt")
    }
}
