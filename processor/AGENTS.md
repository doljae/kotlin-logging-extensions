# AGENTS.md

Scoped instructions for `processor`.

## Goal
Maintain a deterministic and efficient KSP processor that generates correct kotlin-logging extensions.

## Scope
- Symbol discovery (`Resolver`, declarations traversal)
- Visibility and class-kind filtering
- Generated file/package naming
- Generated extension code shape

## Guardrails
- Prefer minimal symbol resolution; avoid expensive or unnecessary traversal.
- Keep generated output deterministic for identical source inputs.
- Never overwrite user-authored source paths.
- Treat changes to generated extension signatures as compatibility-sensitive.

## Tests
- Update processor unit tests whenever generation behavior changes.
- Run `./gradlew :processor:test` as the minimum verification.
