---
name: ksp-logger-generator-maintainer
description: Implement and refactor logger generation behavior in the KSP processor with deterministic output and compatibility-safe changes.
---

# KSP Logger Generator Maintainer

Use this skill when tasks affect generated logger extension behavior.

## Workflow
1. Confirm current processor behavior and affected scenario.
2. Update traversal/filtering/visibility logic in `processor`.
3. Keep generated package/name/signature deterministic.
4. Add or update processor tests for each behavior change.
5. Verify with `./gradlew :processor:test`.

## Hard Rules
- No nondeterministic generation order or naming.
- No breaking signature changes without explicit request and test updates.
- No processor-side assumptions that depend on workload-only classes.

## Validation
- Minimum: `./gradlew :processor:test`
- Preferred for wider changes: `./gradlew test`
