package me.rerere.rikkahub.brainypal

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalAppStructureTest {
    @Test
    fun `brainypal code is split into shared child and parent packages`() {
        val mainRoot = File("src/main/java/me/rerere/rikkahub/brainypal")
            .takeIf { it.exists() }
            ?: File("app/src/main/java/me/rerere/rikkahub/brainypal")
        val testRoot = File("src/test/java/me/rerere/rikkahub/brainypal")
            .takeIf { it.exists() }
            ?: File("app/src/test/java/me/rerere/rikkahub/brainypal")

        assertTrue("shared package must exist", mainRoot.resolve("shared").isDirectory)
        assertTrue("child package must exist", mainRoot.resolve("child").isDirectory)
        assertTrue("parent package must exist", mainRoot.resolve("parent").isDirectory)

        val misplacedMainFiles = mainRoot.listFiles()
            .orEmpty()
            .filter { it.isFile && it.extension == "kt" }
            .map { it.name }
        assertTrue(
            "BrainyPal main files must live under shared, child, or parent: $misplacedMainFiles",
            misplacedMainFiles.isEmpty(),
        )

        val misplacedTestFiles = testRoot.listFiles()
            .orEmpty()
            .filter { it.isFile && it.extension == "kt" && it.name != "BrainyPalAppStructureTest.kt" }
            .map { it.name }
        assertTrue(
            "BrainyPal test files must live under shared, child, or parent: $misplacedTestFiles",
            misplacedTestFiles.isEmpty(),
        )
    }
}
