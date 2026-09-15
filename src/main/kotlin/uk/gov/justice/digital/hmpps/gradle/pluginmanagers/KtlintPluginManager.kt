package uk.gov.justice.digital.hmpps.gradle.pluginmanagers

import org.gradle.api.Project
import org.gradle.api.Task
import org.jlleitschuh.gradle.ktlint.KtlintPlugin
import uk.gov.justice.digital.hmpps.gradle.PluginManager

class KtlintPluginManager(override val project: Project) : PluginManager {
  companion object {
    const val LAST_WORKING_KTLINT_VERSION = "2.4.10"
  }

  override val pluginProject = KtlintPlugin::class.java

  override fun configure() {
    project.getTasksByName("check", false).forEach {
      it.dependsOn("${getProjectPrefix(it)}:ktlintCheck")
    }
    project.configurations.named("ktlint").configure {
      it.resolutionStrategy.eachDependency { dep ->
        if (dep.requested.group == "org.jetbrains.kotlin") {
          dep.useVersion(LAST_WORKING_KTLINT_VERSION)
        }
      }
    }

    copyResourcesFile(".editorconfig")
  }

  private fun getProjectPrefix(it: Task) = if (it.path.contains(":")) {
    it.path.substringBeforeLast(":")
  } else {
    ""
  }
}
