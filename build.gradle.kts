import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.2.20"
  id("com.gradle.plugin-publish") version "2.0.0"
  id("java-gradle-plugin")
  id("maven-publish")
  id("com.github.ben-manes.versions") version "0.52.0"
  id("se.patrikerdes.use-latest-versions") version "0.2.19"
  id("org.owasp.dependencycheck") version "12.1.5"
  id("com.adarshr.test-logger") version "4.0.0"
  id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

repositories {
  mavenLocal()
  mavenCentral()
  maven {
    url = uri("https://plugins.gradle.org/m2/")
  }
}

fun isNonStable(version: String): Boolean {
  val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
  val regex = "^[0-9,.v-]+(-r)?$".toRegex()
  val isStable = stableKeyword || regex.matches(version)
  return isStable.not()
}

group = "uk.gov.justice.hmpps.gradle"
version = "9.1.1"

gradlePlugin {
  website.set("https://github.com/ministryofjustice/hmpps-gradle-spring-boot")
  vcsUrl.set("https://github.com/ministryofjustice/hmpps-gradle-spring-boot")
  plugins {
    create("hmppsSpringBootPlugin") {
      id = "uk.gov.justice.hmpps.gradle-spring-boot"
      implementationClass = "uk.gov.justice.digital.hmpps.gradle.HmppsSpringBootPlugin"

      displayName = "HMPPS Spring Boot Plugin"
      description = "Plugin for HMPPS Spring Boot microservice configuration"
      tags.set(listOf("hmpps", "spring-boot"))
    }
  }
}

dependencies {
  implementation(kotlin("reflect"))

  implementation("org.springframework.boot:spring-boot-gradle-plugin:3.5.6")
  implementation(kotlin("gradle-plugin"))
  implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.7")
  implementation("org.owasp:dependency-check-core:12.1.5")
  implementation("org.owasp:dependency-check-gradle:12.1.5")
  implementation("com.github.ben-manes:gradle-versions-plugin:0.52.0")
  implementation("com.gorylenko.gradle-git-properties:com.gorylenko.gradle-git-properties.gradle.plugin:2.5.3")
  implementation("com.adarshr.test-logger:com.adarshr.test-logger.gradle.plugin:4.0.0")
  implementation("se.patrikerdes.use-latest-versions:se.patrikerdes.use-latest-versions.gradle.plugin:0.2.19")
  implementation("org.jlleitschuh.gradle.ktlint:org.jlleitschuh.gradle.ktlint.gradle.plugin:13.1.0")

  testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
  testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
  testImplementation("org.assertj:assertj-core:3.27.6")
  testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.1")
  testImplementation("com.google.code.gson:gson:2.13.2")
  testImplementation("org.eclipse.jgit:org.eclipse.jgit:7.3.0.202506031305-r")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
}

tasks {
  test {
    useJUnitPlatform()
  }

  withType<KotlinCompile> {
    compilerOptions.jvmTarget = JvmTarget.JVM_21
  }

  withType<DependencyUpdatesTask> {
    rejectVersionIf {
      isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
  }
}

tasks.named("check") {
  dependsOn(":ktlintCheck")
}
