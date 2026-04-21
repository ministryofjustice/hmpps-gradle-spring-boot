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

class DependencyManagementPluginManagerTomcatTest : GradleBuildTest() {

  companion object {
    @JvmStatic
    fun wrongTransitiveTomcatVersion() = listOf(
      arguments(javaProjectDetails(projectDir).copy(buildScript = wrongTransitiveLogbackVersionBuildFile)),
      arguments(kotlinProjectDetails(projectDir).copy(buildScript = wrongTransitiveLogbackVersionBuildFile)),
    )

    private val wrongTransitiveLogbackVersionBuildFile = """
      plugins {
        id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.1.0"
      }
      dependencies {
        implementation("org.springframework.boot:spring-boot-starter-tomcat")
      }
    """.trimIndent()
  }

  @ParameterizedTest
  @MethodSource("wrongTransitiveTomcatVersion")
  fun `Wrong transitive version of tomcat should be overridden by the plugin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails.copy())

    val result = buildProject(projectDir, "bootJar")
    assertThat(result.task(":bootJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, projectDetails.projectName)
    val jarContents = JarFile(file).versionedStream().map { it.name }.toList()
    assertThat(jarContents)
      .doesNotContain("BOOT-INF/lib/tomcat-embed-core-10.1.53.jar")
      .doesNotContain("BOOT-INF/lib/tomcat-embed-el-10.1.53.jar")
      .doesNotContain("BOOT-INF/lib/tomcat-embed-websocket-10.1.53.jar")
      .contains("BOOT-INF/lib/tomcat-embed-core-10.1.54.jar")
      .contains("BOOT-INF/lib/tomcat-embed-el-10.1.54.jar")
      .contains("BOOT-INF/lib/tomcat-embed-websocket-10.1.54.jar")
  }
}
