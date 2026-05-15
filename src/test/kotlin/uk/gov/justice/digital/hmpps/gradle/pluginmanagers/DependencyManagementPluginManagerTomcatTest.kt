package uk.gov.justice.digital.hmpps.gradle.pluginmanagers

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

class DependencyManagementPluginManagerTomcatTest : GradleBuildTest() {

  companion object {
    @Suppress("unused")
    @JvmStatic
    fun wrongTransitiveTomcatVersion() = listOf(
      Arguments.of(javaProjectDetails(projectDir).copy(buildScript = wrongTransitiveTomcatVersionBuildFile())),
      Arguments.of(kotlinProjectDetails(projectDir).copy(buildScript = wrongTransitiveTomcatVersionBuildFile())),
    )
  }
  private fun jarContainsTomcatEmbedCore(jar: JarFile, version: String): Boolean = jar.getJarEntry("BOOT-INF/lib/tomcat-embed-core-$version.jar") != null

  private fun jarContainsTomcatEmbedWebsocket(jar: JarFile, version: String): Boolean = jar.getJarEntry("BOOT-INF/lib/tomcat-embed-websocket-$version.jar") != null

  @ParameterizedTest
  @MethodSource("wrongTransitiveTomcatVersion")
  fun `Wrong transitive version of tomcat should be overridden by the plugin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails.copy())

    val result = buildProject(projectDir, "bootJar")
    assertThat(result.task(":bootJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, projectDetails.projectName)
    val jarFile = JarFile(file)
    assertThat(jarContainsTomcatEmbedCore(jarFile, "11.0.21")).isFalse
    assertThat(jarContainsTomcatEmbedCore(jarFile, "11.0.22")).isTrue
    assertThat(jarContainsTomcatEmbedWebsocket(jarFile, "11.0.21")).isFalse
    assertThat(jarContainsTomcatEmbedWebsocket(jarFile, "11.0.22")).isTrue
  }
}

private fun wrongTransitiveTomcatVersionBuildFile() = """
    plugins {
      id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.1.0"
    }
    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-security");
        implementation("org.springframework.boot:spring-boot-starter-oauth2-client");
        
    }
""".trimIndent()
