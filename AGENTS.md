# AGENTS.md

Codex instructions for this repository. This file is the default instruction layer for all tasks in `kotlin-logging-extensions/`.

## Essence
This repository provides a Kotlin Symbol Processing (KSP) plugin that generates kotlin-logging extensions.
Changes should optimize for correctness, generated-code safety, and developer ergonomics.

## Purpose
- Remove logger boilerplate by generating `log` extensions at compile time.
- Keep processor behavior deterministic and easy to reason about.
- Maintain compatibility across Kotlin/KSP/kotlin-logging version combinations documented by the project.

## Core Objectives
- Keep processor behavior explicit and test-proven.
- Preserve strict module boundaries between processor implementation and workload examples.
- Prefer clear naming and predictable generated output over clever abstractions.
- Keep user-facing docs and compatibility guidance aligned with actual behavior.

## Module Map
- `processor`: KSP symbol processing logic, code generation, and processor unit tests.
- `workload`: sample consumer module used for integration-style verification of generated extensions.
- `scripts`: release and maintenance scripts.

## Engineering Standard
- Design for readers first: explicit behavior, explicit constraints, explicit failure modes.
- Keep generation rules stable and backward-compatible unless an intentional breaking change is requested.
- Add concise comments only when intent is not obvious from code.
- Avoid unnecessary symbol resolution and processing overhead in KSP flows.

## Execution Rules
- Always use the Gradle wrapper: `./gradlew ...`.
- Preferred validations:
  - full build: `./gradlew clean build`
  - all tests: `./gradlew test`
  - processor tests: `./gradlew :processor:test`
  - workload tests: `./gradlew :workload:test`
- If generation logic changes, prioritize `:processor:test` and then run broader checks.

## Architecture Guardrails
- Keep KSP processing logic in `processor`; do not move processor behavior into `workload`.
- Keep `workload` focused on usage scenarios and regression coverage for generated code behavior.
- Preserve deterministic file naming and package handling in generated files.
- Do not introduce breaking changes to generated API shape without updating tests and compatibility documentation.

## Change Workflow
1. Route the task to the correct module first.
2. Implement minimal, explicit changes.
3. Add or update tests closest to changed behavior.
4. Run module-scoped checks first, then broader checks when needed.
5. Summarize assumptions, tradeoffs, and verification in the final response.

## Skills And Specialization
Codex should use project-local skills from `.agents/skills` when the task matches:
- `ksp-module-router`: route requests to the correct module and protect boundaries.
- `ksp-logger-generator-maintainer`: implement/refactor processor generation logic safely.
- `workload-regression-maintainer`: extend workload examples and regression tests.
- `release-compatibility-maintainer`: keep dependency/version compatibility and release docs coherent.
- `reference-quality-maintainer`: enforce readability and teaching-quality repository standards.

These skills are configured as explicit-invocation specialist profiles (subagent-style behavior) via each skill's `agents/openai.yaml`.

## Scoped AGENTS
Additional `AGENTS.md` files exist in subdirectories and override/extend this file for their subtree:
- `processor/AGENTS.md`
- `workload/AGENTS.md`
