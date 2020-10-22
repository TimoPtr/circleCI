#!usr/bin/env groovy


/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

private def checkDeviceAvailable() {
  device = sh(script: "adb devices | grep -w device || true", returnStdout: true).trim()
  if (device == "") {
    sh "adb devices"
    error('Device not available it might be offline. Please restart your build')
  }
}

def validatePom() {
  stage('validate: sdk pom check') {
    sh "$SDK_GRADLE publishToMavenLocal checkPom"
  }
}

def patchSDKForDeployment() {
  stage('patch SDK for deployment') {

    // Make sure that we are publishing Colgate asset and not Hum
    sh 'cd SDK/StaticResourcesBranded && sed -i.bak "s/def isHumBuild = .*/def isHumBuild = false/g" build.gradle'
  }
}

def packageSdkRelease(boolean canDeploy) {
  stage('package: sdk release') {
    sshagent(['jenkins-ci-ssh']) {
      sh 'cd SDK && ./scripts/prepare_release.sh'
      sh 'cd SDK && rm -rf */build/intermediates'
    }

    // Since we are checking out again the repo if the fix was apply before it won't be apply anymore
    patchSDKForDeployment()

    if (canDeploy) {
      stash includes: 'SDK/**/*', name: 'sdk-workspace'
    }
    archiveArtifacts artifacts: 'SDK/release*.tar'
  }
}

def detectRoomSchemaChanges() {
  stage('validate: room schemas') {
    try {
      sh "cd toolbox && ./detect_room_schema_changes.sh"
      githubNotify context: 'Room Schema Check', description: 'Room schema check passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Room Schema Check', description: 'Room schema check failed, see log for details', status: 'ERROR'
      throw e
    }
  }
}

def checkHumDependencies() {
  stage('dependencies: hum') {
    try {
      sh "cd toolbox && ./check_hum_app_dependencies.sh"
      githubNotify context: 'HUM Dependencies Check', description: 'HUM dependencies check passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'HUM Dependencies Check', description: 'HUM dependencies check failed', status: 'ERROR'
      throw e
    }
  }
}

def pullLfs() {
  stage('pull lfs') {
    try {
      sshagent(['jenkins-ci-ssh']) {
        checkout scm
        sh "git-lfs install"
        sh "git-lfs pull"
      }
    } catch (e) {
      throw e
    }
  }
}

return this;
