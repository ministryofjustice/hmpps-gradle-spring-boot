package uk.gov.justice.digital.hmpps.gradle.functional.configmanagers

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.gradle.functional.GradleBuildTest
import uk.gov.justice.digital.hmpps.gradle.functional.ProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.buildProject
import uk.gov.justice.digital.hmpps.gradle.functional.findFile
import uk.gov.justice.digital.hmpps.gradle.functional.makeProject
import java.io.File
import java.nio.file.Files

class BaseConfigManagerSnykTest : GradleBuildTest() {
  @ParameterizedTest
  @MethodSource("defaultProjectDetails")
  fun `The snyk ignore file is copied into the project`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    val result = buildProject(projectDir, "tasks")
    assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val snykFile = findFile(projectDir, ".snyk")
    assertThat(snykFile).exists()
  }

  @ParameterizedTest
  @MethodSource("defaultProjectDetails")
  fun `The snyk suppressions file is not copied into the project`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    val snykScript =
      """
version: v1.25.1
ignore: {}
      """.trimIndent()
    makeSnykFile(snykScript)

    val result = buildProject(projectDir, "tasks")
    assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val snykFile = findFile(projectDir, ".snyk")
    assertThat(snykFile).exists()
    val firstLine = snykFile.useLines { it.firstOrNull() }
    assertThat(firstLine).startsWith("version: v1.25.1")
  }

  @ParameterizedTest
  @MethodSource("defaultProjectDetails")
  fun `The snyk ignore file is overwritten in the project if WARNING exists`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)
    val snykScript =
      """
# WARNING - contents will be overwritten
version: v1.25.1
      """.trimIndent()
    makeSnykFile(snykScript)

    val result = buildProject(projectDir, "tasks")
    assertThat(result.task(":tasks")?.outcome).isEqualTo(TaskOutcome.SUCCESS)

    val snykFile = findFile(projectDir, ".snyk")
    assertThat(snykFile).exists()
    val firstLine = snykFile.useLines { it.firstOrNull() }
    assertThat(firstLine).startsWith("# WARNING - THIS FILE WAS GENERATED")
  }

  private fun makeSnykFile(snykScript: String) {
    val snykFile = File(projectDir, ".snyk")
    Files.writeString(snykFile.toPath(), snykScript)
  }
}
