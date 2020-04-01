package org.jetbrains.internship

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class HashSumPluginTest {
    companion object {
        val SHA_1_FILE_PATH: String
            get() = "hash_sum.txt"
        const val CALCULATE_SHA_1_TASK_NAME = "calculateSha1"

        val String.isSHA1Content: Boolean
            get() = matches("^[a-fA-F0-9]{40}$".toRegex())

    }

    private val exampleProjectDir: File =
        File(System.getProperty("user.dir")).parentFile.resolve("example-project")

    private val project: Project = ProjectBuilder.builder().withProjectDir(exampleProjectDir).build()

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
    fun task1Test() {
        val calculateSha1Task = project.tasks.findByPath(CALCULATE_SHA_1_TASK_NAME)
        assertNotNull(calculateSha1Task)
        calculateSha1Task!!.actions.forEach {
            it.execute(calculateSha1Task)
        }
        val hashSumFile = project.buildDir.resolve(SHA_1_FILE_PATH)
        assertTrue(hashSumFile.exists() && hashSumFile.readText().isSHA1Content)
    }

}

