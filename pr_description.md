## 🚀 Release v2.1.21-0.0.1

This PR prepares the release of version `2.1.21-0.0.1`.

### 🎉 Initial Release

This is the first release of kotlin-logging-extensions!

**Features:**
- Automatic logger generation for Kotlin classes using KSP
- Zero boilerplate - just use `log.info { }` in any class
- Package-aware naming with fully qualified class names
- Seamless integration with kotlin-logging library

### ⚡ Version Compatibility
- **Kotlin**: 2.1.21
- **KSP**: 2.1.21-2.0.2
- **kotlin-logging**: 7.0.7+

### 🔄 After Merge
When this PR is merged, the following will happen automatically:
- ✅ Create git tag: `v2.1.21-0.0.1`
- ✅ Generate GitHub Release with release notes
- ✅ Upload to Maven Central staging repository
- ⚠️ Manual step: Complete publishing at https://oss.sonatype.org/

### ✅ Review Checklist
- [ ] Version number is correct
- [ ] All version references are updated consistently
- [ ] Tests are passing
- [ ] Ready to release
