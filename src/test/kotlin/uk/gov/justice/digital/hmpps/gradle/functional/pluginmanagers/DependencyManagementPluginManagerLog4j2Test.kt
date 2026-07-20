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

class DependencyManagementPluginManagerLog4j2Test : GradleBuildTest() {
  companion object {
    @Suppress("unused")
    @JvmStatic
    fun wrongLog4j2Version() = listOf(
      Arguments.of(javaProjectDetails(projectDir).copy(buildScript = wrongLog4j2VersionBuildFile())),
      Arguments.of(kotlinProjectDetails(projectDir).copy(buildScript = wrongLog4j2VersionBuildFile())),
    )
  }
  private fun jarContainsLog4j2ToSlf4j(jar: JarFile, version: String): Boolean = jar.getJarEntry("BOOT-INF/lib/log4j-to-slf4j-$version.jar") != null

  private fun jarContainsLog4j2Api(jar: JarFile, version: String): Boolean = jar.getJarEntry("BOOT-INF/lib/log4j-api-$version.jar") != null

  @ParameterizedTest
  @MethodSource("wrongLog4j2Version")
  fun `Wrong version of log4j2 should be overridden by the plugin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails.copy())

    val result = buildProject(projectDir, "bootJar")
    assertThat(result.task(":bootJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, projectDetails.projectName)
    val jarFile = JarFile(file)
    assertThat(jarContainsLog4j2ToSlf4j(jarFile, "2.25.4")).isFalse
    assertThat(jarContainsLog4j2ToSlf4j(jarFile, "2.25.5")).isTrue
    assertThat(jarContainsLog4j2Api(jarFile, "2.25.4")).isFalse
    assertThat(jarContainsLog4j2Api(jarFile, "2.25.5")).isTrue
  }
}

private fun wrongLog4j2VersionBuildFile() = """
    plugins {
      id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.1.0"
    }
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-security");
        implementation("org.springframework.boot:spring-boot-starter-oauth2-client");
    }
""".trimIndent()
