#!usr/bin/env groovy


/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

def sdkStaticChecks() throws Exception {
  stage('static checks: sdk code style and static analysis') {
    try {
      sh "$SDK_GRADLE spotlessCheck detekt"
      githubNotify context: 'Sdk Code Style Check', description: 'Code style check passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Sdk Code Style Check', description: 'Code style check failed. Run cd SDK && ./gradlew spotlessApply detekt', status: 'ERROR'
      throw e
    }
  }
}

def mainAppStaticChecks() throws Exception {
  stage('static checks: app code style and static analysis') {
    try {
      sh "$MAIN_APP_GRADLE :app:spotlessCheck :app:detekt"
      githubNotify context: 'App Code Style Check', description: 'Code style check passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'App Code Style Check', description: 'Code style check failed. Run ./gradlew spotlessApply detekt', status: 'ERROR'
      throw e
    }
  }
}

def glimmerAppStaticChecks() throws Exception {
  stage('static checks: glimmer app code style and static analysis') {
    try {
      sh "$GLIMMER_GRADLE :app:spotlessCheck :app:detekt"
      githubNotify context: 'Glimmer App Code Style Check', description: 'Code style check passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Glimmer App Code Style Check', description: 'Code style check failed. Run ./gradlew spotlessApply detekt', status: 'ERROR'
      throw e
    }
  }
}

def lint() throws Exception {
  stage('static checks: lint') {
    try {
      sh "$SDK_GRADLE lintDebug"
      sh "$MAIN_APP_GRADLE :app:lintHumDebug"
      sh "$GLIMMER_GRADLE :app:lintDebug"
      collectLintReports()
      if (!currentBuild.resultIsBetterOrEqualTo('SUCCESS')) {
        error('Lint static checks failed.')
      } else {
        githubNotify context: 'Lint check', description: 'Lint static checks passed', status: 'SUCCESS'
      }
    } catch (e) {
      collectLintReports()
      githubNotify context: 'Lint check', description: 'Lint static checks failed. See report in console', status: 'ERROR'
      throw e
    }
  }
}

private def collectLintReports() {
  recordIssues enabledForFailure: true,
    tool: androidLintParser(pattern: '**/lint-results-*.xml'),
    failedTotalHigh: 1, failedTotalNormal: 1, healthy: 0, unhealthy: 50, minimumSeverity: 'NORMAL'
}

return this;
