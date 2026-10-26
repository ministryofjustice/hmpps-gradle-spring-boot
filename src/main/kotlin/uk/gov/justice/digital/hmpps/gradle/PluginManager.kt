package uk.gov.justice.digital.hmpps.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

interface PluginManager : ConfigManager {
  val pluginProject: Class<out Plugin<in Project>>
}
