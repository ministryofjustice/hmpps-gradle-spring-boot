package uk.gov.justice.digital.hmpps.gradle.functional.pluginmanagers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.gradle.functional.GradleBuildTest
import uk.gov.justice.digital.hmpps.gradle.functional.ProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.getDependencyVersion
import uk.gov.justice.digital.hmpps.gradle.functional.makeProject

class DependencyManagementPluginManagerTest : GradleBuildTest() {

  @ParameterizedTest
  @MethodSource("defaultProjectDetails")
  fun `Spring dependency versions are defaulted from the dependency management plugin`(projectDetails: ProjectDetails) {
    makeProject(projectDetails)

    val webVersion = getDependencyVersion(projectDir, "org.springframework.boot:spring-boot-starter-web")

    assertThat(webVersion).isNotBlank

    val actuatorVersion = getDependencyVersion(projectDir, "org.springframework.boot:spring-boot-starter-actuator")
    val validatorVersion = getDependencyVersion(projectDir, "org.springframework.boot:spring-boot-starter-validation")

    assertThat(webVersion).isEqualTo(actuatorVersion).isEqualTo(validatorVersion)
  }

  @ParameterizedTest
  @MethodSource("defaultProjectDetails")
  fun `Commons Codec uses the overridden dependency version`(projectDetails: ProjectDetails) {
    makeProject(
      projectDetails.copy(
        buildScript = projectDetails.buildScript + "\ndependencies { implementation(\"commons-codec:commons-codec\") }\n",
      ),
    )

    assertThat(getDependencyVersion(projectDir, "commons-codec:commons-codec")).isEqualTo("1.23.0")
  }
}
