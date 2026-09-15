package uk.gov.justice.digital.hmpps.gradle.pluginmanagers

import io.spring.gradle.dependencymanagement.DependencyManagementPlugin
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementConfigurer
import org.gradle.api.Project
import org.springframework.boot.gradle.plugin.SpringBootPlugin
import uk.gov.justice.digital.hmpps.gradle.PluginManager
import uk.gov.justice.digital.hmpps.gradle.configmanagers.OPENTELEMETRY_VERSION
import uk.gov.justice.digital.hmpps.gradle.pluginmanagers.KotlinPluginManager.Companion.JACKSON2_VERSION
import uk.gov.justice.digital.hmpps.gradle.pluginmanagers.KotlinPluginManager.Companion.JACKSON_VERSION

class DependencyManagementPluginManager(override val project: Project) : PluginManager {

  override val pluginProject = DependencyManagementPlugin::class.java

  override fun configure() {
    applyDependencyManagementBom(project)
    project.extensions.extraProperties["opentelemetry.version"] = OPENTELEMETRY_VERSION

    project.extensions.extraProperties["tomcat.version"] = "11.0.25"

    // TODO temporarily pinning for SNYK-JAVA-IONETTY-19778369
    project.extensions.extraProperties["netty.version"] = "4.2.18.Final"

    // TODO temporarily pinning the version to address CVE-2026-68497
    project.extensions.extraProperties["jackson-2-bom.version"] = JACKSON2_VERSION

    // TODO temporarily pinning the version to address CVE-2026-68497
    project.extensions.extraProperties["jackson-bom.version"] = JACKSON_VERSION
  }

  private fun applyDependencyManagementBom(project: Project) {
    val depManConfigurer = project.extensions.getByName("dependencyManagement") as DependencyManagementConfigurer
    depManConfigurer.imports {
      it.mavenBom(SpringBootPlugin.BOM_COORDINATES)
    }
  }
}
