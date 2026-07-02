package uk.gov.justice.digital.hmpps.gradle.functional.pluginmanagers

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.gradle.functional.GradleBuildTest
import uk.gov.justice.digital.hmpps.gradle.functional.ProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.buildProject
import uk.gov.justice.digital.hmpps.gradle.functional.findJar
import uk.gov.justice.digital.hmpps.gradle.functional.javaProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.kotlinProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.makeProject
import java.util.jar.JarFile

class DependencyManagementPluginManagerLogbackTest : GradleBuildTest() {
  companion object {
    @Suppress("unused")
    @JvmStatic
    fun wrongLogbackVersion() = listOf(
      Arguments.of(javaProjectDetails(projectDir).copy(buildScript = wrongLogbackVersionBuildFile())),
      Arguments.of(kotlinProjectDetails(projectDir).copy(buildScript = wrongLogbackVersionBuildFile())),
    )
  }
  private fun jarContainsLogbackCore(jar: JarFile, version: String): Boolean = jar.getJarEntry("BOOT-INF/lib/logback-core-$version.jar") != null

  private fun jarContainsLogbackClassic(jar: JarFile, version: String): Boolean = jar.getJarEntry("BOOT-INF/lib/logback-classic-$version.jar") != null

  @ParameterizedTest
  @MethodSource("wrongLogbackVersion")
  fun `Wrong version of logback should be overridden by the plugin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails.copy())

    val result = buildProject(projectDir, "bootJar")
    assertThat(result.task(":bootJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, projectDetails.projectName)
    val jarFile = JarFile(file)
    assertThat(jarContainsLogbackCore(jarFile, "1.5.34")).isFalse
    assertThat(jarContainsLogbackCore(jarFile, "1.5.37")).isTrue
    assertThat(jarContainsLogbackClassic(jarFile, "1.5.34")).isFalse
    assertThat(jarContainsLogbackClassic(jarFile, "1.5.37")).isTrue
  }
}

private fun wrongLogbackVersionBuildFile() = """
    plugins {
      id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.1.0"
    }
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-security");
        implementation("org.springframework.boot:spring-boot-starter-oauth2-client");
    }
""".trimIndent()
