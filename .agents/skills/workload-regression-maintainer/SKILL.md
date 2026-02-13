---
name: workload-regression-maintainer
description: Maintain realistic workload examples and regression tests that verify generated log extension behavior across edge cases.
---

# Workload Regression Maintainer

Use this skill when tasks involve usage examples or integration-style behavior checks.

## Workflow
1. Identify missing or failing usage scenario.
2. Add/update example class under `workload/src/main/kotlin/examples`.
3. Add/update regression tests under `workload/src/test/kotlin/examples`.
4. Keep examples concise but representative.
5. Verify with `./gradlew :workload:test`.

## Guardrails
- Keep workload free of processor implementation logic.
- Favor edge cases that mirror real user projects (nested types, visibility, reserved words, package depth).
- Maintain readable test naming and assertions.
