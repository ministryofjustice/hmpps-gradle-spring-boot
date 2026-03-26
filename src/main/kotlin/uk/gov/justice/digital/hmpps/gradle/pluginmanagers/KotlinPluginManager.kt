package uk.gov.justice.digital.hmpps.gradle.pluginmanagers

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import uk.gov.justice.digital.hmpps.gradle.PluginManager

const val JACKSON_VERSION = "3.1.0"
const val JACKSON2_VERSION = "2.21.2"

class KotlinPluginManager(override val project: Project) : PluginManager {
  override val pluginProject = KotlinPluginWrapper::class.java

  override fun configure() {
    addDependencies()
    setKotlinCompileJvmVersion()
  }

  private fun setKotlinCompileJvmVersion() {
    project.tasks.withType(KotlinCompile::class.java).forEach {
      it.compilerOptions { JvmTarget.JVM_21 }
    }
  }

  private fun addDependencies() {
    project.dependencies.add("implementation", "tools.jackson.module:jackson-module-kotlin:$JACKSON_VERSION")
    project.dependencies.add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin:$JACKSON2_VERSION")
    project.dependencies.add("implementation", "org.jetbrains.kotlin:kotlin-reflect")

    // Temporarily bump swagger-ui to 5.32.1 for CVE-2026-0540 and CVE-2025-15599
    project.dependencies.constraints.add("implementation", "org.webjars:swagger-ui:5.32.1")

    project.dependencies.add("testImplementation", "org.mockito.kotlin:mockito-kotlin:6.2.3")
  }
}
