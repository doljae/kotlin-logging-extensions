# Codex Agent Setup

This repository uses Codex-native agent configuration:
- Root `AGENTS.md` for global instructions.
- Scoped `AGENTS.md` files for subtree-specific rules.
- `.agents/skills/*` for reusable, explicit specialist skills.

## Skills
- `ksp-module-router`
- `ksp-logger-generator-maintainer`
- `workload-regression-maintainer`
- `release-compatibility-maintainer`
- `reference-quality-maintainer`

Each skill includes `agents/openai.yaml` with:
- `allow_implicit_invocation: false`

This makes skills behave as explicit specialist profiles (subagent-style): invoke when needed, not automatically.
