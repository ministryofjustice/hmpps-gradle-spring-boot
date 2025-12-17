package uk.gov.justice.digital.hmpps.gradle.functional.pluginmanagers

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.gradle.functional.GradleBuildTest
import uk.gov.justice.digital.hmpps.gradle.functional.ProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.buildProject
import uk.gov.justice.digital.hmpps.gradle.functional.getDependencyVersion
import uk.gov.justice.digital.hmpps.gradle.functional.kotlinProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.makeProject

class KotlinPluginManagerTest : GradleBuildTest() {

  companion object {
    @JvmStatic
    fun kotlinOnlyProject() = listOf(
      Arguments.of(kotlinProjectDetails(projectDir)),
    )

    @JvmStatic
    fun projectDetailsWithMockitoDependency() = listOf(
      Arguments.of(
        kotlinProjectDetails(projectDir).copy(
          testClass = kotlinMockitoTest(),
        ),
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("kotlinOnlyProject")
  fun `Sets JVM target for Kotlin compile tasks`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    // This compile would fail at JVM 1.6 which is the default
    val result = buildProject(projectDir, "compileKotlin")
    assertThat(result.task(":compileKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
  }

  @ParameterizedTest
  @MethodSource("kotlinOnlyProject")
  fun `Adds in the jackson module kotlin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    val webVersion = getDependencyVersion(projectDir, "com.fasterxml.jackson.module:jackson-module-kotlin")
    assertThat(webVersion).isNotBlank
  }

  @ParameterizedTest
  @MethodSource("kotlinOnlyProject")
  fun `Adds in kotlin reflect`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    val webVersion = getDependencyVersion(projectDir, "org.jetbrains.kotlin:kotlin-reflect")
    assertThat(webVersion).isNotBlank
  }

  @ParameterizedTest
  @MethodSource("projectDetailsWithMockitoDependency")
  fun `Adds in mockito kotlin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    val result = buildProject(projectDir, "compileTestKotlin")
    assertThat(result.output)
      .contains("Task :compileTestKotlin")
      .contains("BUILD SUCCESSFUL")
  }
}

private fun kotlinMockitoTest(): String = """
    package uk.gov.justice.digital.hmpps.app

    import org.assertj.core.api.Assertions.assertThat
    import org.junit.jupiter.api.Test
    import org.mockito.kotlin.mock

    class ApplicationTest {
      private val bob: String = mock()

      @Test
      fun `A Test`() {
        assertThat("anything").isEqualTo("anything")
      }
    }
""".trimIndent()
