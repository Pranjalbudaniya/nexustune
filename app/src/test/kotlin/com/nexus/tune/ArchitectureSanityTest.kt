package com.nexus.tune

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArchitectureSanityTest {

    @Test
    fun `core domain module contains zero android dependencies`() {
        val domainSrcDir = File("../core/domain/src/main/kotlin")
        if (!domainSrcDir.exists()) {
            // Check relative to current project root
            val rootDomain = File("core/domain/src/main/kotlin")
            if (rootDomain.exists()) {
                verifyNoAndroidImports(rootDomain)
            }
            return
        }
        verifyNoAndroidImports(domainSrcDir)
    }

    private fun verifyNoAndroidImports(dir: File) {
        dir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
            val content = file.readText()
            assertFalse(
                "File ${file.name} in :core:domain violates clean architecture by importing android packages",
                content.contains("import android.") || content.contains("import androidx.")
            )
        }
    }

    @Test
    fun `feature modules do not depend on each other`() {
        val settingsGradle = File("settings.gradle.kts")
        val features = listOf("home", "search", "library", "player", "settings")

        features.forEach { feature ->
            val buildFile = File("feature/$feature/build.gradle.kts")
            if (buildFile.exists()) {
                val text = buildFile.readText()
                features.filter { it != feature }.forEach { otherFeature ->
                    assertFalse(
                        ":feature:$feature must not depend on :feature:$otherFeature",
                        text.contains("project(\":feature:$otherFeature\")")
                    )
                }
            }
        }
    }
}

