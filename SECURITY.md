# Security Policy

## Supported Versions

Security fixes land on the current minor line. Older lines are not backported — this is a
single-maintainer project, and pretending otherwise would be a promise it cannot keep.

| Version | Supported          |
| ------- | ------------------ |
| `3.x`   | :white_check_mark: |
| `< 3.0` | :x:                |

Releases follow plain SemVer from `3.0.0`; the version no longer tracks your Kotlin version. See the
[compatibility table](README.md#-version-compatibility) for the Kotlin, KSP and JDK range a release
supports.

## Reporting a Vulnerability

**Please do NOT report security vulnerabilities through public GitHub issues.**

### How to Report

**Preferred — GitHub private vulnerability reporting**: open the repository's
[Security tab](https://github.com/doljae/kotlin-logging-extensions/security) and choose *Report a
vulnerability*. The report stays private, the discussion stays attached to the repository, and a fix
can be published as a GitHub Security Advisory with a CVE.

**Alternative — email**: [seok9211@naver.com](mailto:seok9211@naver.com), subject line
"Security Vulnerability Report".

Either way, please include:

- Description of the vulnerability
- Steps to reproduce the issue
- Potential impact
- Any suggested fixes (if available)

### What to Expect

- We will acknowledge receipt of your vulnerability report within 48 hours
- We will send you regular updates about our progress
- If the issue is confirmed as a vulnerability, we will:
  - Work on a fix as soon as possible
  - Release a security update
  - Publicly acknowledge your responsible disclosure (if desired)

### Security Best Practices for Users

When using kotlin-logging-extensions:

1. **Keep dependencies updated**: Regularly update to the latest version
2. **Secure your build environment**: Don't commit sensitive credentials to version control
3. **Use environment variables**: Store GitHub tokens and other credentials securely
4. **Review generated code**: While the processor generates simple logger extensions, always review what's being generated in your project

## Security Features

- **Nothing we publish reaches your application's runtime**: the processor is applied through
  `ksp(...)`, so it and its dependencies live on the KSP classpath and run only during compilation
- **Minimal code generation**: only simple logger property extensions
- **No network access**: the processor doesn't make external connections
- **No file system access**: beyond standard KSP operations for code generation

## Dependencies

Two artifacts are published, and what each one declares is checked rather than asserted:

| Artifact | Declared dependencies |
|---|---|
| `kotlin-logging-extensions` (the processor) | `com.google.devtools.ksp:symbol-processing-api`, `org.jetbrains.kotlin:kotlin-stdlib` |
| `kotlin-logging-extensions-annotations` | none |

The annotations artifact is the only one that lands on your **compile** classpath, and it publishes
zero dependencies — enforced on every build by `verifyPublishedMetadataHasNoDependencies`, which
fails the build if a dependency ever appears in the POM or Gradle module metadata (#156).

kotlin-logging itself is **not** a dependency of either artifact. The processor declares it
`compileOnly` and the generated code references it, so the version on your classpath is the one you
chose.

We regularly monitor our dependencies for known vulnerabilities and update them promptly when
security patches are available.
