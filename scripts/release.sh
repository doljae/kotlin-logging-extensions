#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to validate version format
validate_version() {
    local version=$1
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+-[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        print_error "Version must be in KSP format: KOTLIN_VERSION-LIB_VERSION"
        print_error "Example: 2.1.21-0.0.1 (Kotlin 2.1.21, Library version 0.0.1)"
        exit 1
    fi
}

# Function to check if tag exists
tag_exists() {
    local tag=$1
    git tag -l | grep -q "^${tag}$"
}

# Function to check if working directory is clean
check_working_directory() {
    if [[ -n $(git status --porcelain) ]]; then
        print_error "Working directory is not clean. Please commit or stash your changes."
        git status --short
        exit 1
    fi
}

# Function to check if on main branch
check_branch() {
    local current_branch=$(git rev-parse --abbrev-ref HEAD)
    if [[ $current_branch != "main" ]]; then
        print_warning "You are not on the main branch (current: $current_branch)"
        print_warning "It's recommended to release from the main branch."
        read -p "Do you want to continue? [y/N]: " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Aborted."
            exit 0
        fi
    fi
}

# Function to check if GitHub CLI is installed
check_github_cli() {
    if ! command -v gh &> /dev/null; then
        print_error "GitHub CLI (gh) is not installed."
        print_error "Please install it from: https://cli.github.com/"
        print_error ""
        print_error "Alternative: Use GitHub UI to trigger release:"
        print_error "  1. Go to: https://github.com/doljae/kotlin-logging-extensions/actions/workflows/release.yml"
        print_error "  2. Click 'Run workflow'"
        print_error "  3. Enter version and click 'Run workflow'"
        exit 1
    fi
    
    # Check if authenticated
    if ! gh auth status &> /dev/null; then
        print_error "GitHub CLI is not authenticated."
        print_error "Please run: gh auth login"
        exit 1
    fi
}

# Function to suggest next version
suggest_next_version() {
    local latest_tag=$(git tag --sort=-version:refname | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+-[0-9]+\.[0-9]+\.[0-9]+$' | head -1)
    
    if [[ -z $latest_tag ]]; then
        echo
        print_info "No previous releases found"
        print_info "Suggested first version: 2.1.21-0.0.1 (current Kotlin version + initial lib version)"
        echo
        return
    fi
    
    local version=${latest_tag#v}
    local kotlin_version=${version%-*}
    local lib_version=${version#*-}
    
    local IFS='.'
    read -ra LIB_PARTS <<< "$lib_version"
    local lib_major=${LIB_PARTS[0]}
    local lib_minor=${LIB_PARTS[1]}
    local lib_patch=${LIB_PARTS[2]}
    
    echo
    print_info "Current latest version: $version"
    print_info "Kotlin version: $kotlin_version, Library version: $lib_version"
    print_info ""
    print_info "Suggestions:"
    print_info "  Patch (bug fixes): $kotlin_version-$lib_major.$lib_minor.$((lib_patch + 1))"
    print_info "  Minor (new features): $kotlin_version-$lib_major.$((lib_minor + 1)).0"
    print_info "  Major (breaking changes): $kotlin_version-$((lib_major + 1)).0.0"
    print_info "  Kotlin upgrade: 2.1.21-$lib_version (if Kotlin version changed)"
    echo
}

# Function to show what will happen
show_release_plan() {
    local version=$1
    local tag="v$version"
    
    echo
    print_info "🚀 Release Plan:"
    print_info "  1. Trigger GitHub Actions workflow with version: $version"
    print_info "  2. GitHub Actions will automatically:"
    print_info "     - Validate version format"
    print_info "     - Check if tag already exists"
    print_info "     - Run tests and code quality checks"
    print_info "     - Build artifacts"
    print_info "     - Create and push git tag: $tag"
    print_info "     - Upload to Maven Central staging"
    print_info "     - Create GitHub Release with auto-generated notes"
    print_info "  3. Manual step: Go to https://oss.sonatype.org/ to publish"
    echo
}

# Main function
main() {
    print_info "🏷️  kotlin-logging-extensions Release Helper (Manual Trigger)"
    echo
    
    # Prerequisites checks
    check_working_directory
    check_branch
    check_github_cli
    
    # Show current state
    local current_branch=$(git rev-parse --abbrev-ref HEAD)
    local latest_commit=$(git rev-parse --short HEAD)
    
    print_info "Current branch: $current_branch"
    print_info "Latest commit: $latest_commit"
    
    # Show version suggestions
    suggest_next_version
    
    # Get version from user
    read -p "Enter version to release (e.g., 2.1.21-0.0.1): " version
    
    if [[ -z $version ]]; then
        print_error "Version is required."
        exit 1
    fi
    
    validate_version "$version"
    
    local tag="v$version"
    
    if tag_exists "$tag"; then
        print_error "Tag $tag already exists."
        print_info "Existing tags:"
        git tag --sort=-version:refname | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+-[0-9]+\.[0-9]+\.[0-9]+$' | head -5
        exit 1
    fi
    
    # Show what will happen
    show_release_plan "$version"
    
    # Final confirmation
    print_warning "⚠️  This will trigger the release workflow with manual validation."
    print_warning "   Make sure all your changes are committed and tested!"
    echo
    
    read -p "Are you sure you want to trigger release workflow for $version? [y/N]: " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Release cancelled."
        exit 0
    fi
    
    # Trigger GitHub Actions workflow
    print_info "Triggering GitHub Actions release workflow..."
    
    if gh workflow run release.yml --field version="$version"; then
        print_success "✅ Release workflow triggered successfully!"
        echo
        print_info "🔗 Monitor progress: https://github.com/doljae/kotlin-logging-extensions/actions/workflows/release.yml"
        print_info "📦 Track publishing: https://central.sonatype.com/artifact/io.github.doljae/kotlin-logging-extensions"
        print_info "🏷️  Release will be available at: https://github.com/doljae/kotlin-logging-extensions/releases/tag/$tag"
        echo
        print_success "🎉 Release workflow started! The workflow will handle tag creation and publishing."
    else
        print_error "Failed to trigger release workflow."
        print_error "You can manually trigger it from GitHub UI:"
        print_error "  https://github.com/doljae/kotlin-logging-extensions/actions/workflows/release.yml"
        exit 1
    fi
}

# Show help if requested
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "kotlin-logging-extensions Release Helper (Manual Trigger)"
    echo
    echo "Usage: $0"
    echo
    echo "This script helps you trigger a release workflow by:"
    echo "  1. Checking prerequisites (clean working directory, main branch)"
    echo "  2. Suggesting next version based on existing tags"
    echo "  3. Triggering GitHub Actions release workflow via GitHub CLI"
    echo
    echo "The GitHub Actions workflow will automatically:"
    echo "  - Validate version format and check for duplicate tags"
    echo "  - Run tests and code quality checks"
    echo "  - Create and push git tag"
    echo "  - Upload to Maven Central staging repository"
    echo "  - Create GitHub Release with auto-generated release notes"
    echo "  - Manual step required: Go to https://oss.sonatype.org/ to publish"
    echo
    echo "Prerequisites:"
    echo "  - Clean working directory (no uncommitted changes)"
    echo "  - On main branch (recommended)"
    echo "  - GitHub CLI installed and authenticated (gh auth login)"
    echo "  - GitHub secrets configured for Maven Central publishing"
    echo
    echo "Alternative (without GitHub CLI):"
    echo "  1. Go to: https://github.com/doljae/kotlin-logging-extensions/actions/workflows/release.yml"
    echo "  2. Click 'Run workflow'"
    echo "  3. Enter version and click 'Run workflow'"
    echo
    exit 0
fi

# Run main function
main "$@" 