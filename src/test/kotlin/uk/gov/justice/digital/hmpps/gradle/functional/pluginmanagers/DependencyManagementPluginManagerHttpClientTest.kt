package uk.gov.justice.digital.hmpps.gradle.functional.pluginmanagers

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

class DependencyManagementPluginManagerHttpClientTest : GradleBuildTest() {

  companion object {
    @JvmStatic
    fun wrongTransitiveHttpClientVersion() = listOf(
      arguments(javaProjectDetails(projectDir).copy(buildScript = wrongTransitiveHttpClientVersionBuildFile)),
      arguments(kotlinProjectDetails(projectDir).copy(buildScript = wrongTransitiveHttpClientVersionBuildFile)),
    )

    private val wrongTransitiveHttpClientVersionBuildFile = """
      plugins {
        id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.1.0"
      }
      dependencies {
        implementation("org.apache.httpcomponents.client5:httpclient5")
        implementation("org.apache.httpcomponents.core5:httpcore5") 
      }
    """.trimIndent()
  }

  @ParameterizedTest
  @MethodSource("wrongTransitiveHttpClientVersion")
  fun `Wrong transitive version of HttpClient 2 should be overridden by the plugin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails.copy())

    val result = buildProject(projectDir, "bootJar")
    assertThat(result.task(":bootJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, projectDetails.projectName)
    val jarContents = JarFile(file).versionedStream().map { it.name }.toList()
    assertThat(jarContents)
      .doesNotContain("BOOT-INF/lib/httpclient5-5.6.1.jar")
      .doesNotContain("BOOT-INF/lib/httpcore5-5.4.2.jar")
      .contains("BOOT-INF/lib/httpclient5-5.6.4.jar")
      .contains("BOOT-INF/lib/httpcore5-5.4.3.jar")
  }
}
