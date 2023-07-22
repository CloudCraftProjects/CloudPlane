#!/bin/bash

# Exit instantly if any command fails
set -e

# The root directory for the project
ROOT_DIR="$(dirname $0)/.."
echo "Project root directory detected as $ROOT_DIR."

# Extract the github organisation and repository from the gradle buildscript
ORGANISATION=${1:-$(cat build.gradle.kts | grep 'set(github' | awk -F '"' '{ print $2}')}
REPOSITORY=${2:-$(cat build.gradle.kts | grep 'set(github' | awk -F '"' '{ print $4}')}

# Query the default branch from the github api if none has been set
BRANCH=${3:-$(curl -s "https://api.github.com/repos/$ORGANISATION/$REPOSITORY" | jq -r .default_branch)}

# Extract the current gradle property from the buildscript
GRADLE_PROPERTY=$(cat build.gradle.kts | grep gradleProperty | awk -F '\"' '{ print $2}')

# Print out the information which will be used for updating to the runner.
echo "Will be updating property $GRADLE_PROPERTY from repository $ORGANISATION/$REPOSITORY in branch $BRANCH."

# Curl the branch information and parse the commit hash using python
LATEST_COMMIT=$(curl -s "https://api.github.com/repos/$ORGANISATION/$REPOSITORY/branches/$BRANCH" | jq -r .commit.sha)
echo "Curled latest commit hash of $ORGANISATION/$REPOSITORY in $BRANCH: $LATEST_COMMIT."

# Replace in the gradle.properties file the old commit hash with the new one
sed -ri "s/($GRADLE_PROPERTY ?= ?)[0-9a-z]{40}/\1$LATEST_COMMIT/g" "$ROOT_DIR/gradle.properties"
echo "Replaced old commit hash in gradle.properties with $LATEST_COMMIT."
