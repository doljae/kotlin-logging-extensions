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
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        print_error "Version must be in format X.Y.Z (e.g., 1.0.0)"
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

# Function to suggest next version
suggest_next_version() {
    local latest_tag=$(git tag --sort=-version:refname | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | head -1)
    
    if [[ -z $latest_tag ]]; then
        echo "1.0.0"
        return
    fi
    
    local version=${latest_tag#v}
    local IFS='.'
    read -ra VERSION_PARTS <<< "$version"
    local major=${VERSION_PARTS[0]}
    local minor=${VERSION_PARTS[1]}
    local patch=${VERSION_PARTS[2]}
    
    echo
    print_info "Current latest version: $version"
    print_info "Suggestions:"
    print_info "  Patch (bug fixes): $major.$minor.$((patch + 1))"
    print_info "  Minor (new features): $major.$((minor + 1)).0"
    print_info "  Major (breaking changes): $((major + 1)).0.0"
    echo
}

# Function to show what will happen
show_release_plan() {
    local version=$1
    local tag="v$version"
    
    echo
    print_info "🚀 Release Plan:"
    print_info "  1. Create git tag: $tag"
    print_info "  2. Push tag to GitHub"
    print_info "  3. GitHub Actions will automatically:"
    print_info "     - Run tests"
    print_info "     - Build artifacts"
    print_info "     - Publish to Maven Central"
    print_info "     - Create GitHub Release with notes"
    echo
}

# Main function
main() {
    print_info "🏷️  kotlin-logging-extensions Release Helper"
    echo
    
    # Prerequisites checks
    check_working_directory
    check_branch
    
    # Show current state
    local current_branch=$(git rev-parse --abbrev-ref HEAD)
    local latest_commit=$(git rev-parse --short HEAD)
    
    print_info "Current branch: $current_branch"
    print_info "Latest commit: $latest_commit"
    
    # Show version suggestions
    suggest_next_version
    
    # Get version from user
    read -p "Enter version to release (e.g., 1.0.0): " version
    
    if [[ -z $version ]]; then
        print_error "Version is required."
        exit 1
    fi
    
    validate_version "$version"
    
    local tag="v$version"
    
    if tag_exists "$tag"; then
        print_error "Tag $tag already exists."
        print_info "Existing tags:"
        git tag --sort=-version:refname | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | head -5
        exit 1
    fi
    
    # Show what will happen
    show_release_plan "$version"
    
    # Final confirmation
    print_warning "⚠️  This will create a new release and trigger automatic publishing."
    print_warning "   Make sure all your changes are committed and tested!"
    echo
    
    read -p "Are you sure you want to create release $tag? [y/N]: " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Release cancelled."
        exit 0
    fi
    
    # Create and push tag
    print_info "Creating tag $tag..."
    git tag -a "$tag" -m "Release $tag"
    
    print_info "Pushing tag to GitHub..."
    git push origin "$tag"
    
    print_success "✅ Tag $tag created and pushed!"
    echo
    print_info "🔗 GitHub Actions workflow: https://github.com/doljae/kotlin-logging-extensions/actions"
    print_info "📦 Track publishing: https://central.sonatype.com/artifact/io.github.doljae/kotlin-logging-extensions"
    print_info "🏷️  Release will be available at: https://github.com/doljae/kotlin-logging-extensions/releases/tag/$tag"
    echo
    print_success "🎉 Release process started! Check GitHub Actions for progress."
}

# Show help if requested
if [[ "$1" == "--help" || "$1" == "-h" ]]; then
    echo "kotlin-logging-extensions Release Helper"
    echo
    echo "Usage: $0"
    echo
    echo "This script helps you create a new release by:"
    echo "  1. Checking prerequisites (clean working directory, main branch)"
    echo "  2. Suggesting next version based on existing tags"
    echo "  3. Creating and pushing a git tag"
    echo "  4. Triggering GitHub Actions for automatic publishing"
    echo
    echo "The GitHub Actions workflow will automatically:"
    echo "  - Run tests and code quality checks"
    echo "  - Publish to Maven Central"
    echo "  - Create GitHub Release with release notes"
    echo
    echo "Prerequisites:"
    echo "  - Clean working directory (no uncommitted changes)"
    echo "  - On main branch (recommended)"
    echo "  - GitHub secrets configured for Maven Central publishing"
    echo
    exit 0
fi

# Run main function
main "$@" 