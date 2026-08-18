#!/usr/bin/env bash
#
# Categorize one commit subject into a release-notes section for create-release-pr.yml.
#
#   scripts/categorize-commit.sh "docs(readme): fix the table"   # prints: docs
#   scripts/categorize-commit.sh --self-test                     # runs the table below
#
# Prints exactly one of: features | deps | docs.
#
# The type and scope are parsed once, so a Conventional Commit scope cannot change the section.
# The workflow used to match whole subjects (`feat:*`, `docs:*`, ...), which left every scoped
# subject to the uncategorized default, Features & Fixes, and announced `chore(deps):` Renovate
# updates as features (issue #183).
set -euo pipefail

categorize() {
  local subject="$1" type="" scope=""

  # <type>[(scope)][!]: description. Held in a variable because bash 3.2, which is what macOS
  # ships, does not accept the parentheses inline in the conditional.
  local conventional='^([a-z]+)(\(([^)]*)\))?!?:'
  if [[ "$subject" =~ $conventional ]]; then
    type="${BASH_REMATCH[1]}"
    scope="${BASH_REMATCH[3]}"
  fi

  local lower
  lower="$(printf '%s' "$subject" | tr '[:upper:]' '[:lower:]')"

  if [[ "$scope" == deps || "$scope" == dep ]]; then
    # Renovate opens both `fix(deps):` and `chore(deps):`. The scope is the statement of intent,
    # so it decides on its own, with no keyword check on top of it.
    echo deps
  elif [[ "$type" == chore || "$type" == ci ]] &&
       [[ "$lower" == *update* || "$lower" == *upgrade* || "$lower" == *bump* ]] &&
       [[ "$lower" == *dependenc* || "$lower" == *deps* || "$lower" == *version* ||
          "$lower" == *kotlin* || "$lower" == *ksp* || "$lower" == *gradle* ]]; then
    # A hand-written chore/ci subject that reads like a dependency bump but carries no scope.
    # Matched case-insensitively: these are written by hand, as "Gradle", "Kotlin", "KSP".
    echo deps
  elif [[ "$type" == feat || "$type" == fix || "$type" == perf || "$type" == refactor ]]; then
    echo features
  elif [[ "$type" == docs || "$type" == chore || "$type" == ci || "$type" == style || "$type" == test ]]; then
    echo docs
  else
    # Unparseable subject. Features & Fixes is the section a reader looks at first, so an
    # unrecognized commit is surfaced rather than buried.
    echo features
  fi
}

self_test() {
  local failures=0 expected subject actual
  while IFS='|' read -r expected subject; do
    [ -n "$expected" ] || continue
    actual="$(categorize "$subject")"
    if [ "$actual" = "$expected" ]; then
      printf '  ok    %-8s  %s\n' "$actual" "$subject"
    else
      printf '  FAIL  expected %s, got %s:  %s\n' "$expected" "$actual" "$subject"
      failures=$((failures + 1))
    fi
  done <<'CASES'
features|feat: add a PackageScan generation mode
features|fix: keep generated names unique per source set
features|feat(processor): generate one file per package instead of one file per class
features|fix(release): suggest versions by severity, not from the Kotlin version
features|feat!: drop the AutoLog annotation
features|feat(annotations)!: drop the AutoLog annotation
features|Merge pull request #1 from doljae/topic
deps|fix(deps): update dependency ch.qos.logback:logback-classic to v1.6.2
deps|chore(deps): update gradle to v9.7.0
deps|fix(deps): pin logback to 1.5.x
deps|chore: update the Gradle wrapper to 9.7.0
deps|ci: bump the KSP version used by the consumer matrix
docs|docs: write down the issue-first contribution workflow
docs|docs(readme): correct the compatibility table
docs|ci(release): stop tagging the same commit twice
docs|test(processor): cover the shadowing warning
docs|ci: put the maintainer and the linked issue's labels on every PR
docs|chore: remove the unused workload fixture
CASES

  if [ "$failures" -gt 0 ]; then
    echo "$failures case(s) failed."
    return 1
  fi
  echo "All cases passed."
}

case "${1:-}" in
  --self-test) self_test ;;
  "") echo "usage: $0 <commit subject> | --self-test" >&2; exit 2 ;;
  *) categorize "$1" ;;
esac
