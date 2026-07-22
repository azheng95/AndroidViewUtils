# Contributing

## Development

Use JDK 17 and the checked-in Gradle wrapper.

```shell
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

Pull requests should keep changes focused, add tests for behavior changes, and update
`CHANGELOG.md` when public APIs or XML resources change.

Optional integrations must remain `compileOnly` in the library module and be documented
in the README with the exact runtime dependencies required by consumers.

## Public API changes

Avoid removing or renaming public Kotlin declarations and Android resources in patch
releases. Breaking changes require a migration note and an appropriate version bump.
