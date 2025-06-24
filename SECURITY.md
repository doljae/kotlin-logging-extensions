# Security Policy

## Supported Versions

We release patches for security vulnerabilities. Which versions are eligible for receiving such patches depends on the CVSS v3.0 Rating:

| Version | Supported          |
| ------- | ------------------ |
| 0.0.x   | :white_check_mark: |

## Reporting a Vulnerability

If you discover a security vulnerability, we would like to know about it so we can take steps to address it as quickly as possible.

**Please do NOT report security vulnerabilities through public GitHub issues.**

### How to Report

1. **Email**: Send an email to [seok9211@naver.com](mailto:seok9211@naver.com) with the subject line "Security Vulnerability Report"
2. **Include**: 
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

- **No runtime dependencies**: The processor only runs at compile time
- **Minimal code generation**: Only generates simple logger property extensions
- **No network access**: The processor doesn't make external connections
- **No file system access**: Beyond standard KSP operations for code generation

## Dependencies

This project uses minimal dependencies to reduce the attack surface:

- Kotlin Symbol Processing (KSP) API - for compile-time code generation
- kotlin-logging - for runtime logging functionality

We regularly monitor our dependencies for known vulnerabilities and update them promptly when security patches are available. 