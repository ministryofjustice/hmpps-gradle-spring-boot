# Release instructions

When preparing a release:

1. Inspect recent release commits and the current worktree before making changes.
2. Bump the plugin version in `build.gradle.kts`.
3. Add release notes at the top of the release-notes file matching the release's major version, for example `release-notes/11.x.md` for 11.x releases. If the release major version changes and the matching file does not exist, create `release-notes/<major>.x.md`.
4. Update the plugin version in the usage example in `README.md`.
5. Add or update focused tests for the code changes before releasing.
6. Run the smallest relevant test suite. Do not run `ktlintCheck` or any other ktlint Gradle task.

Keep release changes limited to the version, release notes, README example, and tests or implementation required by the release. Do not manually edit generated files such as `src/main/resources/gradle.properties`.
