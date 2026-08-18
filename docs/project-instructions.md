# kotlin-logging-extensions — Project Instructions

KSP plugin that generates `kotlin-logging` `log` extensions at compile time.

## Contribution Workflow

Every change follows this order, and no step is skipped, not even for a one-line documentation fix:

1. **Open an issue** stating the problem and the proposed change.
2. **Branch off `main`** as `<type>/<slug>`, where `<type>` is the Conventional Commit type of the
   work (`docs/quick-start-generation-model`, `fix/inherited-logger-name-173`).
3. **Commit on that branch**, one logical change per commit, subject in Conventional Commit form.
4. **Open a PR** whose body links the issue with `Closes #<number>`.

Never commit to `main` or edit its working tree directly.

Use the forms this repository already configures, and fill them in rather than replacing them with
free-form text:

- Issues: the templates in [`.github/ISSUE_TEMPLATE/`](../.github/ISSUE_TEMPLATE). Blank issues are
  enabled for work that fits none of them, but they get the same level of detail.
- PRs: [`.github/pull_request_template.md`](../.github/pull_request_template.md). Keep its headings
  and answer each one.

Two conventions above are load-bearing, not cosmetic:

- `pr-triage.yml` parses the PR body for GitHub's closing keywords and mirrors the linked issue's
  labels onto the PR. A body without `Closes #<number>` produces an unlabeled PR.
- `create-release-pr.yml` groups release notes by commit subject prefix: `feat:`, `fix:`, `perf:`,
  `refactor:` become "Features & Fixes", and `docs:`, `chore:`, `ci:`, `style:`, `test:` become
  "Documentation & Maintenance". The patterns are unscoped, so anything else, a scoped
  `docs(readme):` included, falls through to a default that files it under "Features & Fixes".

Issue and PR bodies are where the reasoning behind a change is recorded, so state the mechanism and
the evidence, not just what changed.

## Modules

| Module | Purpose |
|--------|---------|
| `annotations` | `@Log` (and deprecated `@AutoLog`), published as `kotlin-logging-extensions-annotations` |
| `processor` | KSP processor, code generation, processor unit tests |
| `workload` | Consumer module for integration verification of generated extensions |
| `scripts` | Release and maintenance scripts (not a Gradle module) |

## Build Commands

```
./gradlew clean build       # full build
./gradlew test              # all tests
./gradlew :processor:test   # processor tests only
./gradlew :workload:test    # workload tests only
./gradlew ktlintCheck       # lint
```

Always use the Gradle wrapper (`./gradlew`), never a system-installed Gradle.

## Key Constraints

- Keep processor logic in `processor`; do not leak it into `workload`.
- Keep `workload` focused on usage examples and regression coverage.
- Generated output must be deterministic for identical source inputs.
- Do not change generated extension signatures without updating tests.
- `annotations` is the only artifact on a consumer's compile classpath: it publishes zero
  dependencies (enforced by a `check` task) and targets Kotlin language/API 2.0. Adding a dependency
  or raising that floor is a breaking change for consumers.
- Every module uses `jvmToolchain(17)`. For the two published ones the toolchain sets
  `org.gradle.jvm.version` in the module metadata, so raising it breaks resolution on JDK 17
  consumers (issue #152).
- The processor matches `@Log`/`@AutoLog` by qualified name and must not depend on `:annotations`
  outside tests. A `typealias` shim is not usable, because KSP reports aliases under their own name.
- Generation is project-wide (`All`) by default; `kotlinloggingextensions.mode` /
  `.targets` narrow it. One file per package per source set:
  `KotlinLoggingExtensions_<module>.kt`.

## General Coding Guidelines

See [karpathy-coding-guidelines.md](karpathy-coding-guidelines.md).
