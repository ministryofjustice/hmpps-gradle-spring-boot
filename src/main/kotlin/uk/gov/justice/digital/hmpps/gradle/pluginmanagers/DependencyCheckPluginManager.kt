package uk.gov.justice.digital.hmpps.gradle.pluginmanagers

import org.gradle.api.Project
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.owasp.dependencycheck.reporting.ReportGenerator
import uk.gov.justice.digital.hmpps.gradle.PluginManager
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

internal const val DEPENDENCY_SUPPRESSION_FILENAME = "dps-gradle-spring-boot-suppressions.xml"

class DependencyCheckPluginManager(override val project: Project) : PluginManager {

  override val pluginProject = DependencyCheckPlugin::class.java

  override fun configure() {
    setDependencyCheckConfig()
    addDependencyCheckSuppressionFile()
  }

  override fun afterEvaluate() {
    checkOverriddenSuppressionFile()
  }

  private fun setDependencyCheckConfig() {
    val extension = project.extensions.getByName("dependencyCheck") as DependencyCheckExtension
    extension.failBuildOnCVSS.set(5f)
    extension.suppressionFiles.set(mutableListOf(DEPENDENCY_SUPPRESSION_FILENAME))
    extension.format.set(ReportGenerator.Format.ALL.name)
    extension.analyzers.assemblyEnabled.set(false)
    if (extension.nvd.datafeedUrl.isPresent.not() && project.hasProperty("datafeed.url")) {
      extension.nvd.datafeedUrl.set(project.property("datafeed.url").toString())
    }
    if (extension.analyzers.ossIndex.username.isPresent.not()) {
      if (project.hasProperty("ossIndex.username") && project.hasProperty("ossIndex.password")) {
        project.logger.info("Setting OSS Index username and password from project properties.")
        extension.analyzers.ossIndex.username.set(project.property("ossIndex.username") as String)
        extension.analyzers.ossIndex.password.set(project.property("ossIndex.password") as String)
      }
    }
  }

  private fun addDependencyCheckSuppressionFile() {
    val inputStream = javaClass.classLoader.getResourceAsStream(DEPENDENCY_SUPPRESSION_FILENAME)!!
    val newFile = Paths.get("${project.rootDir.absolutePath}/$DEPENDENCY_SUPPRESSION_FILENAME")
    Files.copy(inputStream, newFile, StandardCopyOption.REPLACE_EXISTING)
  }

  private fun checkOverriddenSuppressionFile() {
    val extension = project.extensions.getByName("dependencyCheck") as DependencyCheckExtension
    if (extension.suppressionFiles.isPresent && extension.suppressionFiles.get().contains(DEPENDENCY_SUPPRESSION_FILENAME).not()) {
      project.logger.warn(
        """
        
        INFO: The default dependency checker suppression file has not been applied. Did you accidentally set suppressionFiles = listOf("<file>") instead of suppressionFiles.add("<file>") in your Gradle build script?

        """.trimIndent(),
      )
    }
  }
}
