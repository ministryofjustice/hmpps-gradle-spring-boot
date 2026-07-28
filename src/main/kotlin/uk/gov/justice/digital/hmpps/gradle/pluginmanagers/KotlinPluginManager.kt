package uk.gov.justice.digital.hmpps.gradle.pluginmanagers

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import uk.gov.justice.digital.hmpps.gradle.PluginManager

const val JACKSON_VERSION = "3.1.5"
const val JACKSON2_VERSION = "2.21.5"

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

    // Temporarily bump swagger-ui to 5.32.11 for CVE-2026-65898, CVE-2026-65902, CVE-2026-65903, CVE-2026-65899, CVE-2026-65900, CVE-2026-65901, CVE-2026-66010
    project.dependencies.constraints.add("implementation", "org.webjars:swagger-ui:5.32.11")

    project.dependencies.add("testImplementation", "org.mockito.kotlin:mockito-kotlin:6.3.0")
  }
}
