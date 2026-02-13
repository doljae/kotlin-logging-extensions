# AGENTS.md

Scoped instructions for `workload`.

## Goal
Keep workload examples representative of real consumer usage and effective for regression detection.

## Scope
- Example classes that consume generated `log` extensions
- Regression tests covering edge cases (nested classes, reserved package segments, visibility)

## Guardrails
- Use workload to prove behavior, not to host processor logic.
- Prefer realistic usage examples over synthetic micro-cases when possible.
- Keep examples readable and aligned with documented project behavior.

## Tests
- Add or update tests in `workload/src/test` when usage behavior changes.
- Run `./gradlew :workload:test` for workload-only changes.
