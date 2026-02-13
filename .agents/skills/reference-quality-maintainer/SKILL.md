---
name: reference-quality-maintainer
description: Enforce clear naming, structure, and verification so this repository remains a high-quality KSP reference implementation.
---

# Reference Quality Maintainer

Use this skill when implementing/refactoring behavior that should remain easy to learn from.

## Quality Bar
- Prefer explicit, descriptive names for classes, functions, and tests.
- Keep module/package structure aligned with responsibility.
- Reduce ambiguity before adding abstraction.
- Keep examples small but complete enough to teach behavior.

## Mandatory Checks
1. Naming clarity check: can intent be inferred without external context?
2. Structure check: is behavior in the correct module/layer?
3. Test check: is changed behavior covered by readable tests?
4. Validation check: run relevant Gradle checks for touched modules.

## Anti-Patterns
- Mixing processor internals into workload examples.
- Hidden behavior changes without test updates.
- Clever but opaque generation logic.
- Documentation that drifts from implemented behavior.
