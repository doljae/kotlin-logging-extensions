# Subagent-Style Specialization

Codex does not require a separate global subagent registry for this repo.
Instead, specialist behavior is implemented with explicit-invocation skills.

## Specialist Mapping
- Routing specialist: `.agents/skills/ksp-module-router`
- Processor specialist: `.agents/skills/ksp-logger-generator-maintainer`
- Workload specialist: `.agents/skills/workload-regression-maintainer`
- Release compatibility specialist: `.agents/skills/release-compatibility-maintainer`
- Quality specialist: `.agents/skills/reference-quality-maintainer`

## Why this structure
- Keeps behavior local to repository context.
- Avoids accidental automatic invocation for broad tasks.
- Makes specialist intent visible and auditable in source control.
