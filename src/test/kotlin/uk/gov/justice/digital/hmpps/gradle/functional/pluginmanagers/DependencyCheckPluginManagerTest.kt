package uk.gov.justice.digital.hmpps.gradle.functional.pluginmanagers

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.gradle.functional.GradleBuildTest
import uk.gov.justice.digital.hmpps.gradle.functional.ProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.buildProject
import uk.gov.justice.digital.hmpps.gradle.functional.findFile
import uk.gov.justice.digital.hmpps.gradle.functional.makeProject
import uk.gov.justice.digital.hmpps.gradle.pluginmanagers.DEPENDENCY_SUPPRESSION_FILENAME

class DependencyCheckPluginManagerTest : GradleBuildTest() {

  @ParameterizedTest
  @MethodSource("defaultProjectDetails")
  fun `The Owasp dependency analyze task is available`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    val result = buildProject(projectDir, "dependencyCheckAnalyze", "-m")
    assertThat(result.output)
      .contains(":dependencyCheckAnalyze SKIPPED")
      .contains("SUCCESSFUL")
  }

  @ParameterizedTest
  @MethodSource("defaultProjectDetails")
  fun `The Owasp dependency check suppression file is copied into the project`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    val result = buildProject(projectDir, "tasks")
    assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val suppressionFile = findFile(projectDir, DEPENDENCY_SUPPRESSION_FILENAME)
    assertThat(suppressionFile).exists()
  }

  @ParameterizedTest
  @MethodSource("defaultProjectDetails")
  fun `Dependency check skips build tool configurations`(projectDetails: ProjectDetails) {
    makeProject(
      projectDetails.copy(
        buildScript = projectDetails.buildScript + skipConfigurationsTask(projectDetails.buildScriptName),
      ),
    )

    val result = buildProject(projectDir, "printDependencyCheckSkipConfigurations")

    assertThat(result.output).contains(
      "[ktlint, ktlintBaselineReporter, ktlintReporter, ktlintRuleset, kotlinAbiValidationCompatClasspath]",
    )
  }

  private fun skipConfigurationsTask(buildScriptName: String): String = if (buildScriptName.endsWith(".kts")) {
    """

    tasks.register("printDependencyCheckSkipConfigurations") {
      doLast {
        val extension = project.extensions.getByName("dependencyCheck") as org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
        println(extension.skipConfigurations.get())
      }
    }
    """.trimIndent()
  } else {
    """

    tasks.register("printDependencyCheckSkipConfigurations") {
      doLast {
        println(project.extensions.getByName("dependencyCheck").skipConfigurations.get())
      }
    }
    """.trimIndent()
  }
}
