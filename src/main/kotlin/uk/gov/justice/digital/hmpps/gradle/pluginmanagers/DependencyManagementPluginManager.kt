package uk.gov.justice.digital.hmpps.gradle.pluginmanagers

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementConfigurer
import org.gradle.api.Project
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import uk.gov.justice.digital.hmpps.gradle.PluginManager
import uk.gov.justice.digital.hmpps.gradle.configmanagers.OPENTELEMETRY_VERSION

class DependencyManagementPluginManager(override val project: Project) : PluginManager {

  override val pluginProject = DependencyManagementPlugin::class.java

  override fun configure() {
    applyDependencyManagementBom(project)
    project.extensions.extraProperties["opentelemetry.version"] = OPENTELEMETRY_VERSION
    project.extensions.extraProperties["tomcat.version"] = "11.0.23"
    // TODO Pinned to override 1.5.34 which has a security vulnerability CVE-2026-13006 - remove this when Spring pulls in at least this version
    project.extensions.extraProperties["logback.version"] = "1.5.37"

    // temporarily pinning the version to address CVE-2026-54512 and CVE-2026-54513 until other dependencies are updated accordingly
    project.extensions.extraProperties["jackson-2-bom.version"] = JACKSON2_VERSION

    // temporarily pinning the version to address CVE-2026-54399 and CVE-2026-54428 until other dependencies are updated accordingly
    project.extensions.extraProperties["httpclient5.version"] = "5.6.2"
    project.extensions.extraProperties["httpcore5.version"] = "5.4.3"
  }

  private fun applyDependencyManagementBom(project: Project) {
    val depManConfigurer = project.extensions.getByName("dependencyManagement") as DependencyManagementConfigurer
    depManConfigurer.imports {
      it.mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
  }
}
