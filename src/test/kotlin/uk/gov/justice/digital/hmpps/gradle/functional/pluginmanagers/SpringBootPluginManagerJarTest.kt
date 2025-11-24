package uk.gov.justice.digital.hmpps.gradle.functional.pluginmanagers

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import uk.gov.justice.digital.hmpps.gradle.functional.createAndRunJar
import uk.gov.justice.digital.hmpps.gradle.functional.javaProjectDetails
import uk.gov.justice.digital.hmpps.gradle.functional.kotlinProjectDetails
import java.io.File
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class JavaSpringBootPluginManagerJarTest : SpringBootPluginManagerJarTest() {

  companion object {
    @BeforeAll
    @JvmStatic
    fun `Create and run project`() {
      jarProcess = createAndRunJar(javaProjectDetails(projectDir))
    }
  }
}

class KotlinSpringBootPluginManagerJarTest : SpringBootPluginManagerJarTest() {

  companion object {
    @BeforeAll
    @JvmStatic
    fun `Create and run project`() {
      jarProcess = createAndRunJar(kotlinProjectDetails(projectDir))
    }
  }
}

abstract class SpringBootPluginManagerJarTest {

  companion object {

    @TempDir
    @JvmStatic
    lateinit var projectDir: File

    var jarProcess: Process? = null

    @AfterAll
    @JvmStatic
    fun `End running jar`() {
      jarProcess?.destroyForcibly()
    }
  }

  @Test
  fun `Spring Boot jar is up and healthy`() {
    val healthResponse = URI.create("http://localhost:8080/actuator/health").toURL().readText()
    assertThatJson(healthResponse).node("status").isEqualTo("UP")
  }

  @Test
  fun `Spring Boot info endpoint is available`() {
    URI.create("http://localhost:8080/actuator/info").toURL().readText()
  }

  @Test
  fun `Spring Boot info endpoint contains git info`() {
    val infoResponse = URI.create("http://localhost:8080/actuator/info").toURL().readText()
    assertThatJson(infoResponse).node("git.branch").isNotNull
  }

  @Test
  fun `Spring Boot info endpoint contains build info`() {
    val infoResponse = URI.create("http://localhost:8080/actuator/info").toURL().readText()
    assertThatJson(infoResponse).node("build.by").isEqualTo(System.getProperty("user.name"))
    assertThatJson(infoResponse).node("build.operatingSystem").isNotNull
    assertThatJson(infoResponse).node("build.machine").isNotNull
    assertThatJson(infoResponse).node("build.time").asString().startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
    assertThatJson(infoResponse).node("build.version").isEqualTo(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
  }
}
