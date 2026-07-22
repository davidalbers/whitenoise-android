# Code quality tooling

This document covers the testing, screenshot testing, linting, and formatting tooling added to
this project, and how to run each of them locally and in CI.

## Unit tests

### Setup

Unit tests use JUnit 4 plus a small set of testing libraries, all declared as `testImplementation`
in `app/build.gradle`:

- **JUnit 4** (`junit:junit`) — test runner and assertions.
- **MockK** (`io.mockk:mockk`) — mocking library for Kotlin. It can mock the concrete
  `AudioController` class directly (no interface needed) because MockK mocks final classes via a
  JVM agent. On JDK 17+ that agent needs to self-attach, which is disabled by default, so
  `app/build.gradle` passes `-Djdk.attach.allowAttachSelf=true` to the test JVM
  (`android.testOptions.unitTests.all.jvmArgs`).
- **kotlinx-coroutines-test** — provides `UnconfinedTestDispatcher`, installed via
  `Dispatchers.setMain(...)` in `@Before`, so that `viewModelScope.launch { ... }` blocks (used to
  collect `AudioController.stateFlow`) run synchronously in tests instead of needing a real main
  thread.
- **androidx.arch.core:core-testing** (`InstantTaskExecutorRule`) — makes `LiveData.setValue()`
  run synchronously without a real Android main thread/Looper, which `AudioPlayerViewModel` and
  `PlayerScreenViewModel` both rely on.

Tests for the two ViewModels live at:

- `app/src/test/java/dalbers/com/noise/AudioPlayerViewModelTest.kt`
- `app/src/test/java/dalbers/com/noise/playerscreen/viewmodel/PlayerScreenViewModelTest.kt`

(`AudioPlayerViewModelTest` lives directly under `dalbers.com.noise` — not
`dalbers.com.noise.service.viewmodel` — because that's the package `AudioPlayerViewModel` itself
actually declares, even though the file lives in a `service/viewmodel` source folder.)

Both dependencies of each ViewModel (`AudioController`, `UserPreferences`) are mocked, so these are
true unit tests: no Robolectric, no real Android framework classes, no device/emulator required.

### Running locally

```bash
./gradlew testDebugUnitTest
```

Run a single class or a filtered set of tests:

```bash
./gradlew testDebugUnitTest --tests "dalbers.com.noise.PlayerScreenViewModelTest"
```

HTML report: `app/build/reports/tests/testDebugUnitTest/index.html`

### CI

`.github/workflows/tests.yml` runs `./gradlew testDebugUnitTest` on every push to `master` and on
every pull request, and uploads the HTML/XML test reports as a build artifact (`unit-test-reports`)
whether the run passes or fails.

---

## Screenshot testing (Roborazzi)

### Setup

Screenshot tests use [Roborazzi](https://github.com/takahirom/roborazzi), which renders
Composables with Robolectric (on the JVM, no emulator needed) and compares the rendered pixels
against a committed baseline PNG.

Rather than hand-writing a screenshot test per Composable, this project uses Roborazzi's
**Compose Preview scanning** feature: the Roborazzi Gradle plugin scans the classpath for every
`@Preview`-annotated Composable and generates one parameterized Robolectric test class per module
at build time (`generateDebugComposePreviewRobolectricTests` task) — one test case per preview,
automatically. **Adding a new `@Preview` to any Composable automatically gets a screenshot test for
free — no test code to write.**

Configuration (in `app/build.gradle`):

```groovy
roborazzi {
    outputDir.set(file("src/test/screenshots"))
    generateComposePreviewRobolectricTests.enable.set(true)
    generateComposePreviewRobolectricTests.packages.set(["dalbers.com.noise"])
    generateComposePreviewRobolectricTests.includePrivatePreviews.set(true)
}
```

`includePrivatePreviews` is required because nearly every `@Preview` composable in this codebase is
declared `private fun` (the normal convention, since preview functions aren't part of any public
API) — Roborazzi ignores private previews by default.

Baseline images are committed to the repo under `app/src/test/screenshots/` (not the Gradle
default of `build/outputs/roborazzi`, which is a build output and would be lost/regenerated). That
means reviewers can see UI changes as an image diff in the PR, and CI can verify against them
without needing to download artifacts from a previous run.

**Known version constraint:** Roborazzi is pinned to `1.60.0` (see the comment next to
`ext.roborazziVersion` in the root `build.gradle`) because 1.61.0+ is compiled with a newer Kotlin
compiler whose metadata this project's Kotlin 2.0.20 can't read. Bump it once the project's Kotlin
version is upgraded. Similarly, `composeVersion` was bumped from `1.6.8` to `1.7.8` — Roborazzi's
Compose integration calls a `BoxKt` method that only exists from Compose Foundation 1.7+.

### Running locally

Record/update the baseline images (do this after intentionally changing a Composable's UI):

```bash
./gradlew recordRoborazziDebug
```

Verify the current UI against the committed baselines (fails if anything changed):

```bash
./gradlew verifyRoborazziDebug
```

Generate a diff report without failing the build:

```bash
./gradlew compareRoborazziDebug
```

Reports: `app/build/reports/roborazzi/index.html`. When `verifyRoborazziDebug` fails, look for
`*_compare.png` files under `app/build/outputs/roborazzi` — they highlight exactly what changed.

If a UI change is intentional, run `recordRoborazziDebug` locally and commit the updated PNGs
under `app/src/test/screenshots/`.

### CI

`.github/workflows/screenshots.yml` runs `./gradlew verifyRoborazziDebug` on every push to
`master` and every pull request. On failure, it uploads the diff images/reports
(`app/build/outputs/roborazzi`, `app/build/reports/roborazzi`) as the `screenshot-diffs` artifact
so you can inspect what changed without re-running locally.

---

## Lint and formatting

### Setup

Two complementary tools are used:

- **[ktlint](https://github.com/pinterest/ktlint)**, via the
  [`org.jlleitschuh.gradle.ktlint`](https://github.com/JLLeitschuh/ktlint-gradle) Gradle plugin —
  Kotlin code style/formatting.
- **Android Lint** — already built into the Android Gradle plugin (`./gradlew lint`), no extra
  setup required; it checks for Android-specific issues (resource usage, API level problems,
  performance, etc.) rather than code style.

`.editorconfig` at the repo root tunes one ktlint rule for this codebase:

```ini
[*.{kt,kts}]
ktlint_function_naming_ignore_when_annotated_with = Composable
```

Without this, ktlint's standard `function-naming` rule would flag every `@Composable fun
SomeScreen(...)` for using PascalCase — which is the normal Jetpack Compose naming convention
(composables act like widgets), not a style violation.

One file (`NoiseSelector.kt`) keeps a wildcard import
(`@file:Suppress("ktlint:standard:no-wildcard-imports")`) because it uses 25+ symbols (color
constants, theme, `NoiseType`) from the same `dalbers.com.noise.shared` package — spelling all of
those out individually hurts readability more than the wildcard does.

### Running locally

Check formatting (fails the build if violations are found):

```bash
./gradlew ktlintCheck
```

Auto-fix what can be auto-fixed:

```bash
./gradlew ktlintFormat
```

Run Android Lint:

```bash
./gradlew lintDebug
```

HTML report: `app/build/reports/lint-results-debug.html`. As of this writing it reports 24
warnings (0 errors) — pre-existing issues not addressed as part of this change, since fixing lint
warnings was out of scope; `lintDebug` only fails the build on errors, not warnings.

### CI

`.github/workflows/lint.yml` runs `./gradlew ktlintCheck` and `./gradlew lintDebug` on every push
to `master` and every pull request, uploading the ktlint and Android Lint reports as artifacts.

---

## Other changes made to support this tooling

- `android.enableJetifier` was turned off in `gradle.properties` (the project is 100% AndroidX
  already, so it was doing nothing) — it was otherwise crashing on a transitive dependency
  (`byte-buddy`, pulled in by MockK) compiled with a newer bytecode version than AGP's bundled
  Jetifier can parse.
- Two `const val`s (`tickPeriod`, `minVolumePercent` in `AudioController.kt`) and two
  (`notificationChannel`, `mediaSessionTag` in `AudioPlayerService.kt`) were renamed to
  `SCREAMING_SNAKE_CASE` to satisfy ktlint's `property-naming` rule.
  `TimerToggle.kt` (an empty, unreferenced file) was deleted.
  All other wildcard imports were replaced with explicit ones.
- `ktlintFormat` was run once across the whole existing codebase to bring it into compliance
  (trailing commas, EOF newlines, spacing). That diff is purely cosmetic.

## A note on `PlayerScreenViewModel` found while writing its tests

`PlayerScreenViewModel.loadPastPreferences()` sets `_playerScreenState.value =
_playerScreenState.value?.copy(...)` — but at that point in `init {}`, `_playerScreenState.value`
is still `null` (it's a `MutableLiveData<PlayerScreenState>()` with no initial value), so the
`?.copy(...)` safe call short-circuits and this assignment is a no-op. In practice this doesn't
lose the noise type/volume/fade/waves preferences, because those are also applied to
`audioController` directly and flow back into UI state via `AudioController`'s own `StateFlow`.
However, `selectedTimerPreset` (which highlights the active timer preset button) has no such path
back and is therefore never restored from disk on app launch, even when a saved timer preset
exists — only `millisLeft` (the countdown) is correct. This is a pre-existing behavior, not
something introduced by this change; flagging it here since the ViewModel tests
(`init resumes a saved timer when timer was enabled`, etc.) exercise this code path directly.
