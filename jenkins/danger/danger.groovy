#!usr/bin/env groovy


/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

def install() {
  stage('install: danger') {

    sh "apt-get update"
    sh "apt-get -y install build-essential=12.8ubuntu1"

    // Danger is Ruby based and need Gem, the Ruby package manager, to be installed
    sh "apt-get -y install ruby-dev=1:2.7+1"
    sh "gem install danger --version 8.0.4 --no-document"
    sh "gem install danger-android_lint --version 0.0.8"
    sh "gem install danger-kotlin_detekt --version 0.0.3"
  }
}

/**
 * Display the commons warnings which can be shown before the analysis
 * `--remove-previous-comments` Removes all previous comment with the same `--danger_id`
 * and create a new one in the end of the PR
 */
def runCommonWarning() {
  stage('danger: common warnings') {
    sh 'danger --remove-previous-comments --danger_id=common_warnings --dangerfile="jenkins/danger/common-warning/Dangerfile"'
  }
}

/**
 * Display the warnings from the analysis reports
 * `--remove-previous-comments` Removes all previous comment with the same `--danger_id`
 * and create a new one in the end of the PR
 */
def runAnalysisWarning() {
  stage('danger: read reports') {
    sh 'danger --remove-previous-comments --danger_id=read_reports --dangerfile="jenkins/danger/analysis/Dangerfile"'
  }
}

/**
 * Display the warnings from the screenshot tests verification
 * `--remove-previous-comments` Removes all previous comment with the same `--danger_id`
 * and create a new one in the end of the PR
 */
def runScreenshotWarning(String flavour, Boolean verificationFailed) {
  stage('danger: screenshot message') {
    sh "export DANGER_SCREENSHOT_TESTING_FLAVOUR=${flavour} && export DANGER_SCREENSHOT_VERIFICATION_FAILED=${verificationFailed} && danger --remove-previous-comments --danger_id=${flavour}_screenshot_message --dangerfile=\"jenkins/danger/screenshot-testing/Dangerfile\""
  }
}

/**
 * Display the warnings from the analysis reports
 * `--remove-previous-comments` Removes all previous comment with the same `--danger_id`
 * and create a new one in the end of the PR
 */
def runDeployMessage() {
  stage('danger: deploy message') {
    sh 'danger --remove-previous-comments --danger_id=deploy_message --dangerfile="jenkins/danger/deploy-apk/Dangerfile"'
  }
}

return this;
