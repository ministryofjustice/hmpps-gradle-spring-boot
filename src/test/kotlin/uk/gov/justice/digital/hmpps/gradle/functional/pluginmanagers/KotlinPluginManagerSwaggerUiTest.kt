package uk.gov.justice.digital.hmpps.gradle.pluginmanagers

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.gradle.functional.GradleBuildTest
import uk.gov.justice.digital.hmpps.gradle.functional.ProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.buildProject
import uk.gov.justice.digital.hmpps.gradle.functional.findJar
import uk.gov.justice.digital.hmpps.gradle.functional.javaProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.kotlinProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.makeProject
import java.util.jar.JarFile

class KotlinPluginManagerSwaggerUiTest : GradleBuildTest() {

  companion object {
    @JvmStatic
    fun wrongTransitiveSwaggerUiVersion() = listOf(
      arguments(javaProjectDetails(projectDir).copy(buildScript = wrongTransitiveSwaggerUiVersionBuildFile)),
      arguments(kotlinProjectDetails(projectDir).copy(buildScript = wrongTransitiveSwaggerUiVersionBuildFile)),
    )

    private val wrongTransitiveSwaggerUiVersionBuildFile = """
      plugins {
        id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.1.0"
      }
      dependencies {
        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.2")
      }
    """.trimIndent()
  }

  @ParameterizedTest
  @MethodSource("wrongTransitiveSwaggerUiVersion")
  fun `Wrong transitive version of swagger ui should be overridden by the plugin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails.copy())

    val result = buildProject(projectDir, "bootJar")
    assertThat(result.task(":bootJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, projectDetails.projectName)
    val jarContents = JarFile(file).versionedStream().map { it.name }.toList()
    assertThat(jarContents)
      .doesNotContain("BOOT-INF/lib/swagger-ui-5.32.0.jar")
      .contains("BOOT-INF/lib/swagger-ui-5.32.11.jar")
  }
}
