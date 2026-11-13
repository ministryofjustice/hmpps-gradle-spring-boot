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

class DependencyManagementPluginManagerJacksonTest : GradleBuildTest() {

  companion object {
    @JvmStatic
    fun managedJacksonVersions() = listOf(
      arguments(javaProjectDetails(projectDir).copy(buildScript = jacksonBuildFile, testClass = javaJacksonTest)),
      arguments(kotlinProjectDetails(projectDir).copy(buildScript = jacksonBuildFile, testClass = kotlinJacksonTest)),
    )

    private val jacksonBuildFile = """
      plugins {
        id("uk.gov.justice.hmpps.gradle-spring-boot") version "0.1.0"
      }
      dependencies {
        implementation("com.fasterxml.jackson.core:jackson-core")
        implementation("tools.jackson.core:jackson-core")
      }
    """.trimIndent()

    private val javaJacksonTest = """
      package uk.gov.justice.digital.hmpps.app;

      import org.junit.jupiter.api.Test;
      import static org.assertj.core.api.Assertions.assertThat;

      public class ApplicationTest {
        public record Payload(String name, int count) {}

        @Test
        void bothJacksonVersionsRoundTripRecords() throws Exception {
          var payload = new Payload("example", 7);
          var jackson2 = com.fasterxml.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();
          var jackson3 = tools.jackson.databind.json.JsonMapper.builder().findAndAddModules().build();
          assertThat(jackson2.readValue(jackson2.writeValueAsString(payload), Payload.class)).isEqualTo(payload);
          assertThat(jackson3.readValue(jackson3.writeValueAsString(payload), Payload.class)).isEqualTo(payload);
        }
      }
    """.trimIndent()

    private val kotlinJacksonTest = """
      package uk.gov.justice.digital.hmpps.app

      import org.assertj.core.api.Assertions.assertThat
      import org.junit.jupiter.api.Test

      data class Payload(val name: String, val count: Int = 7, val note: String? = null)

      class ApplicationTest {
        @Test
        fun bothJacksonVersionsRoundTripKotlinDefaultsAndNulls() {
          val payload = Payload("example")
          val jackson2 = com.fasterxml.jackson.databind.json.JsonMapper.builder().findAndAddModules().build()
          val jackson3 = tools.jackson.databind.json.JsonMapper.builder().findAndAddModules().build()
          assertThat(jackson2.readValue(jackson2.writeValueAsString(payload), Payload::class.java)).isEqualTo(payload)
          assertThat(jackson3.readValue(jackson3.writeValueAsString(payload), Payload::class.java)).isEqualTo(payload)
          assertThat(jackson2.readValue("{\"name\":\"example\"}", Payload::class.java)).isEqualTo(payload)
          assertThat(jackson3.readValue("{\"name\":\"example\"}", Payload::class.java)).isEqualTo(payload)
        }
      }
    """.trimIndent()
  }

  @ParameterizedTest
  @MethodSource("managedJacksonVersions")
  fun `Both Jackson lines use patched Boot managed versions and retain consumer behaviour`(projectDetails: ProjectDetails) {
    makeProject(projectDetails.copy())

    val result = buildProject(projectDir, "test", "bootJar")
    assertThat(result.task(":test")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    assertThat(result.task(":bootJar")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val file = findJar(projectDir, projectDetails.projectName)
    val jarContents = JarFile(file).use { it.versionedStream().map { entry -> entry.name }.toList() }
    assertThat(jarContents)
      .contains(
        "BOOT-INF/lib/jackson-core-2.21.7.jar",
        "BOOT-INF/lib/jackson-module-kotlin-2.21.7.jar",
        "BOOT-INF/lib/jackson-databind-2.21.7.jar",
        "BOOT-INF/lib/jackson-core-3.1.7.jar",
        "BOOT-INF/lib/jackson-module-kotlin-3.1.7.jar",
        "BOOT-INF/lib/jackson-databind-3.1.7.jar",
      )
      .doesNotContain(
        "BOOT-INF/lib/jackson-core-2.21.8.jar",
        "BOOT-INF/lib/jackson-module-kotlin-2.21.8.jar",
        "BOOT-INF/lib/jackson-databind-2.21.8.jar",
        "BOOT-INF/lib/jackson-core-3.1.8.jar",
        "BOOT-INF/lib/jackson-module-kotlin-3.1.8.jar",
        "BOOT-INF/lib/jackson-databind-3.1.8.jar",
      )
  }
}
