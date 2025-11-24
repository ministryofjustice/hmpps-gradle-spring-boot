# HMPPS Spring Boot Gradle Plugin

## Overview

This Gradle plugin is used to orchestrate HMPPS Spring Boot projects such that:
* All projects use a common set of plugins at versions that have been tested together
* All projects use a common set of dependencies at versions that have been tested together
* Any common build configuration shared by projects is performed in this plugin and not the project itself (removing duplication and subsequent drift)
* CVEs causing `dependencyCheckAnalyze` failures are mitigated in a single place rather than in each and every project

This plugin targets Spring Boot 3.  See the `spring-boot-2` branch for the Spring Boot 2 version.

## Release Notes
##### [10.x](release-notes/10.x.md)
##### [9.x](release-notes/9.x.md)
##### [8.x](release-notes/8.x.md)
##### [7.x](release-notes/7.x.md)
##### [6.x](release-notes/6.x.md)
##### [5.x](release-notes/5.x.md)
##### [4.x](release-notes/4.x.md)
##### [3.x](release-notes/3.x.md)
##### [2.x](release-notes/2.x.md)
##### [1.x](release-notes/1.x.md)
##### [0.x](release-notes/0.x.md)

## How to use this plugin
In your `build.gradle.kts` (or `build.gradle` for Java) add the following line to the plugin section:
```
plugins {
  ...
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "10.0.0"
  ...
}
```
Where the `plugin-version` can be [found here](https://plugins.gradle.org/plugin/uk.gov.justice.hmpps.gradle-spring-boot)

### Duplicated build logic

This plugin provides various build logic that may already exist in your project, and using this plugin may:
* Have no effect - maybe your project's build logic overrides the plugin
* Cause an error - maybe something in the plugin recognises the duplication and complains

Either way you will need to remove some of this duplication from your `build.gradle.kts` file

## How do I find out what this plugin is doing?

Read the code!

In the main you will probably need to know the following so you don't duplicate in your own build script:

#### The plugins and versions automatically applied by this plugin
The class HmppsSpringBootPlugin contains all 3rd party plugins that are applied by the HMPPS plugin - check there first.  Then look in the file `build.gradle.kts` in the `dependencies/implementation` section.  There you can find each plugin and its version.

E.g. `org.springframework.boot:spring-boot-gradle-plugin` is the Spring Boot plugin and its version is part of the configuration

#### Any configuration relating to those plugins
Look in the package `uk.gov.justice.digital.hmpps.gradle.pluginmanagers` and you should find the class that orchestrates each plugin.

E.g. Class `SpringBootPluginManager` has a method called `setSpringBootInfo` which controls the contents of the `/info` endpoint

#### Any dependencies applied by this plugin automatically
Search the package `uk.gov.justice.digital.hmpps.gradle` for `project.dependencies.add`.  This should list all dependencies this plugin automatically applies.

E.g. Class `SpringBootPluginManager` applies `spring-boot-starter-actuator` (amongst others) so you do not need to declare that dependency in your project's `build.gradle.kts` file.

## OWASP Dependency Check and Suppression Files

The plugin `org.owasp.dependencycheck` is applied by this plugin.  It has a task `dependencyCheckAnalyze` which is used to check for vulnerabilities in dependencies.

Any vulnerabilities introduced by this plugin that have been mitigated can be suppressed in file `src/main/resources/dps-gradle-spring-boot-suppressions.xml`.  See the OWASP dependency check plugin's homepage for further details.

### Additional Suppression Files

If you are using this plugin and need additional suppressions for vulnerabilities introduced by your dependencies then you can specify additional suppression xmls in your Gradle build file:
```
dependencyCheck {
  suppressionFiles.add("<your-suppresion-file>.xml")
}
```

***WARNING***

It is possible to overwrite the `suppressionFiles` list like this:
```
// This removes the plugin's default suppression file
dependencyCheck {
  suppressionFiles = listOf("<your-suppresion-file>.xml")
}
```
 This will remove the suppression file supplied by this plugin which is probably not the intention.

## Dependency Update Checks

The plugin `com.github.ben-manes.versions` has task `dependencyUpdates` which checks the main Gradle build file for out of date versions.

However, we also need to know if dependencies applied by the HMPPS plugin are up to date.

### How to check if dependencies applied by the HMPPS plugin are up to date

There is a test in class `VersionsPluginManagerTest` which:
* builds a sample project
* applies the HMPPS plugin
* runs the `dependencyUpdates` task against the sample project
* copies the reports generated into directory `build/dependencyUpdates/projectsUsingPlugin`

You can run the test with command:
```
./gradlew clean test --tests uk.gov.justice.digital.hmpps.gradle.functional.pluginmanagers.VersionsPluginManagerTest
```
and check the report generated at `build/dependencyUpdates/projectsUsingPlugin/report.txt`

## Testing the plugin locally on other projects

* Firstly bump the version of the plugin.
* Then publish the plugin to local maven
```
./gradlew publishToMavenLocal
```
* Then change the settings of the dependent project to read plugins from local storage.  In settings.gradle.kts
```
pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
  }
}
```

## Releasing the plugin

Semantic versioning is used for the plugin i.e. `MAJOR.MINOR.PATCH`. A `MAJOR` change is a breaking change and will
require changes to any existing projects that wish to adopt the `MAJOR` change.

At present the version number increment is a manual step in `build.gradle.kts`.  Increment that at the same time
as making changes.

When the plugin is ready to be released and PR approved, merge into main.  Then go into github at
https://github.com/ministryofjustice/hmpps-gradle-spring-boot/actions and approve the publish step.  This
will publish the plugin to the gradle plugins repository.

### Code style & formatting
```bash
./gradlew ktlintApplyToIdea addKtlintFormatGitPreCommitHook
```
will apply ktlint styles to intellij and also add a pre-commit hook to format all changed kotlin files.

Note that the .editorconfig in the root of this project is for this project only, the one in src/main/resources will be copied to other projects to enforce style.

To setup dependent projects with ktlint:
1. (Optional) run the above gradle command to apply to intellij and add the pre commit hook.
2. If you don't plan on making changes to .editorconfig (created on first run) then add to .gitignore.
3. Ensure that the continuous integration tool runs the `check` task instead of `test`.

## TODO

* Automate incremental version numbers (we could manage this manually but safer if it is impossible overwrite previous versions)

## Runbook

This runbook details how to upgrade the dependencies of this plugin and publish it for others to use.

A new version may be needed for a few different reasons:
- Dependencies have been flagged in #dps_alerts_security as having security vulnerabilities
  - See [Slack message](https://mojdt.slack.com/archives/C69NWE339/p1639384200271500) for guidance on vulnerability patching
  - These could also be false positives and can be suppressed. Typically these are raised as issues against the [DependencyCheck](https://github.com/jeremylong/DependencyCheck/issues) project
- There is a new major releases of Java, Kotlin or Spring and we want to provide the option for teams to bump to these versions

### Upgrading dependencies

1. Create a Jira ticket
2. git clone this repo
3. Create a git branch including the Jira ticket in the name
4. Open the project in your favourite code editor
5. Bump the library version to the next version
   1. See [Releasing the plugin](#releasing-the-plugin) for help on which version to bump
6. If the dependencies have security vulnerabilities then it can be useful to check that locally so you can see the state of them before and after your changes 
   1. Run the following Gradle task: `./gradlew dependencyCheckAnalyze`
   2. If you are using IntelliJ it can be found by going to _Gradle -> hmpps-gradle-spring-boot -> Tasks -> owasp dependency-check -> dependencyCheckAnalyze_ 
   3. Additional testing can be done with [Dependency Update Checks](#dependency-update-checks)
7. Upgrade all the dependencies in `build.gradle.kts` by running the following Gradle task: `./gradlew useLatestVersions`
   1. If you are using IntelliJ it can be found by going to _Gradle -> hmpps-gradle-spring-boot -> Tasks -> help -> useLatestVersions_
8. Review the pinned versions in `KotlinPluginManager`, `AppInsightsConfigManager`, and any others
   1. You may need to manually bump, or remove them if they are no longer required

### Testing locally

1. Follow [Testing the plugin locally on other projects](#testing-the-plugin-locally-on-other-projects)
2. In the other project you should run all tests including integration and start it locally
   1. At this stage you may run into issues such as transitive dependency resolution, unexpected test errors, or a new version of a dependency may not play nice with the project
   2. General advice is to do a bit of Googling to see if other people had the same issue. If it isn't critical to bump that 
   dependency then you can revert that dependency to the previous version and add a note in the build.gradle.kts
   as to why you didn't upgrade it so the next person knows. Hopefully the issue will be fixed and we'll be able to bump it next time

### Releasing a new version without a beta version

1. In the [release-notes](/release-notes) directory create a new Markdown file for your version 
   1. Look at previous examples - we list the version upgrades which you can retrieve from the report generated by `useLatestVersions`
2. Add a link to the file you just created to [Release Notes](#release-notes)
3. You're now ready to commit these changes and raise a PR 🎉
4. Post the PR link on #hmpps_dev for folks to review
5. After merging to main it will automatically be published to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/uk.gov.justice.hmpps.gradle-spring-boot)

## Smoketesting risky changes

If the changes carry a high level of risk, you can add `-beta` to the end of the version.

We initially release a beta version to indicate to other teams that this version hasn't yet been tested in the wild, i.e. it hasn't
been included in a service deployed an environment. We do this to ensure that the new dependencies haven't caused any unexpected issues.

1. Pick a non-critical project which is not released to prod very frequently
   1. The DPS Tech Team used [court-register](https://github.com/ministryofjustice/court-register) and [prison-to-nhs-update](https://github.com/ministryofjustice/prison-to-nhs-update)
2. Update the `uk.gov.justice.hmpps.gradle-spring-boot` version to the beta one which was just published
3. Raise a PR and once approved deploy it to dev
4. Validate that the service has started up and is working as expected
5. Leave it running for a day and monitor that no unexpected alerts have been raised

### Releasing the new version

After smoketesting did not surface any issues the plugin is now ready for other teams to use.

1. Create a new git branch
2. Drop the `-beta` suffix from the version number in `build.gradle.kts`
3. Commit and raise a PR
4. Post the PR link on #hmpps_dev for folks to review
5. After the new version has been published you can upgrade your remaining projects to the new non-beta version 
   1. A script to do this across a number of projects can be found in [HMPPS Tech scripts](https://github.com/ministryofjustice/hmpps-tech-docs/tree/main/scripts/hmpps-spring-boot-gradle-plugin)
   2. Please also upgrade [hmpps-template-kotlin](https://github.com/ministryofjustice/hmpps-template-kotlin)
