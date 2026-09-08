# Contributing to Modkit

Thanks for taking a look. Bug reports, questions, and "why does it work like this?" are all useful, and you don't need to write code to help.

## Ways to pitch in

* Open an issue if something breaks, or if the docs left you guessing.
* Tell us about your setup. Modkit tries to cover Fabric, NeoForge, Quilt, and Forge from one model, and the fastest way to find the gaps is to hear about a build it doesn't handle.
* Send a pull request. Small ones are easiest to review, but we'll take whatever you've got.

If you're planning something big, open an issue first so we can talk it over. We'd rather chat early than have you spend a weekend on something we were already halfway through, or something that doesn't fit the design.

## Getting set up

You'll need JDK 21. Everything else comes from the Gradle wrapper, so you don't need to install Gradle yourself.

```bash
git clone https://github.com/oliveryasuna/modkit.git
cd modkit
./gradlew build
```

The build has the configuration cache turned on. If a change of yours breaks it, Gradle will say so during the build, and it's worth fixing rather than working around.

## How the repo is laid out

* `build-logic` holds the convention plugins (`modkit.library-conventions` and `modkit.plugin-conventions`). They set the toolchain, the bytecode target, JUnit, and coverage, so individual modules (`build.gradle.kts`/`settings.gradle.kts`) stay short.
* `libraries` holds the shared code that the plugins build on, like the core model and the plugin support helpers. These go to Maven Central.
* `plugins` holds the actual Gradle plugins. These go to the Plugin Portal.

Modules compile on Java 21 but emit Java 17 bytecode, so plugins still load for people on older Gradle JVMs. Anything that wraps loader tooling can raise that with `modkit.bytecodeTarget`, since Loom and ModDevGradle need 21.

## Tests

Two suites:

* `./gradlew test` runs the unit tests. Plugin modules get the Gradle API on the test classpath, so `ProjectBuilder` is available.
* `./gradlew functionalTest` runs the TestKit tests, which spin up a real Gradle build against the plugin.

`./gradlew check` runs both plugin `coverageReport`, which merges the JaCoCo data from both suites into one report per module. **Run this before you push.**

New behavior should come with a test. Plugin wiring in particular is easy to get subtly wrong, and a functional test catches things a unit test won't.

## Style

When you open a pull request, the CI will run a style-check (with Spotless). It uses the IntelliJ style defined in `.idea/codeStyles/Project.xml`. A few things we care about:

* Four spaces, no tabs.
* Explicit API mode is on for every module, so public declarations need explicit visibility and return types.
* KDoc on public types and anything a reader will hit cold.
* Comments should say why, not what. The code already says what it does. If something looks strange, leave a note explaining the constraint that put it there.
* Comments should not extend pass column 80.
* Code should not extend pass column 120. If a line is getting long, that's usually a hint the expression wants to be broken up anyway.

There is no linter wired up (yet).

## Commits and pull requests

We write commit messages as plain sentences that say what changed. No prefixes, no strict format. Just make it readable in a log.

For a pull request:

1. Branch off `main`.
2. Keep the changes focused on one thing.
3. Run `./gradlew check`.
4. Say what you changed and why in the description. If it's a bug fix, a short note in the extended message on how to reproduce the bug helps a lot.

If you're not sure something's ready, open it as a draft and say so. Half-finished work with a question attached is fine.

## Licensing

Modkit is licensed under Apache 2.0. By sending a pull request, you're agreeing that your contribution goes under the same license. See the full license [here](./LICENSE).

## Code of conduct

Be decent to people. Assume good faith, keep criticism constructive and about the code, and give folks room to be new at this. We follow version 3.0 of the [Contributor Covenant](https://www.contributor-covenant.org/version/3/0/code_of_conduct/).

Thanks again for being here!
