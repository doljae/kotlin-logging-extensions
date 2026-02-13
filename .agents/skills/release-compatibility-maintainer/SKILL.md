---
name: release-compatibility-maintainer
description: Maintain version compatibility, release metadata, and dependency coherence across Kotlin, KSP, and kotlin-logging.
---

# Release Compatibility Maintainer

Use this skill when tasks involve dependency bumps, compatibility matrix updates, or release preparation.

## Workflow
1. Identify impacted versions (Kotlin, KSP, kotlin-logging, Gradle plugins).
2. Update build files and compatibility documentation together.
3. Validate impacted modules first, then run broader checks.
4. Ensure release messaging reflects actual tested compatibility.

## Hard Rules
- Do not bump versions blindly; confirm compatibility assumptions.
- Do not update README compatibility tables without corresponding build/test validation.
- Keep release script and documentation behavior consistent.

## Validation
- Minimum for dependency changes: `./gradlew test`
- Preferred before release: `./gradlew clean build`
