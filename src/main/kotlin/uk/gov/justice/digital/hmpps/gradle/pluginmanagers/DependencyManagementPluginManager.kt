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

    // TODO Pinned for CVE-2026-59083 and CVE-2026-59084
    project.extensions.extraProperties["tomcat.version"] = "11.0.24"
    // TODO Pinned for CVE-2026-56819, CVE-2026-59921 and CVE-2026-59901
    project.extensions.extraProperties["netty.version"] = "4.2.16.Final"
    // TODO Pinned to override 1.5.34 which has a security vulnerability CVE-2026-13006 - remove this when Spring pulls in at least this version
    project.extensions.extraProperties["logback.version"] = "1.5.37"
    // TODO Pinned for CVE-2026-49844
    project.extensions.extraProperties["log4j2.version"] = "2.25.5"
    // TODO temporarily pinning the version to address CVE-2026-54512 and CVE-2026-54513 until other dependencies are updated accordingly
    project.extensions.extraProperties["jackson-2-bom.version"] = JACKSON2_VERSION
    project.extensions.extraProperties["jackson-bom.version"] = JACKSON_VERSION
    // TODO temporarily pinning the version to address CVE-2026-54399 and CVE-2026-54428 until other dependencies are updated accordingly
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
