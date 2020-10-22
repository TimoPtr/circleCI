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
  sh "adb devices" // make sure that the
  device = sh(script: "adb devices | grep -w device || true", returnStdout: true).trim()
  if (device == "") {
    sh "adb devices"
    error('Device not available it might be offline. Please restart your build')
  }
}

private def zipAndArchive(String archiveName, String path) {
  try {
    sh "tar -czf ${archiveName}.tar.gz ${path}"
    archiveArtifacts artifacts: "${archiveName}.tar.gz"
  } catch (e) {
    println("Fail to zip and archive :" + path)
  }
}

def sdkFunctionalTest() {
  stage('test: sdk functional tests') {
    try {
     checkDeviceAvailable()
     sh "$SDK_GRADLE connectedDebugAndroidTest"
      githubNotify context: 'SDK Functional Test', description: 'All functional tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Functional Test', description: 'Functional test failed', status: 'ERROR'
      throw e
   } finally {
     junit 'SDK/**/build/outputs/**/connected/*.xml'
    }
  }
}

def mainAppHumFunctionalTest() {
  stage('test hum: app functional & espresso tests') {
    try {
      checkDeviceAvailable()
      sh "$MAIN_APP_GRADLE -Pdisable_glthread -PenableScreenshotTesting :app:cleanHumDebugAndroidTestScreenshots :app:marathonHumDebugAndroidTest"
      githubNotify context: 'Main App Hum Functional Test', description: 'All functional tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Main App Hum Functional Test', description: 'Functional test failed', status: 'ERROR'
      throw e
    } finally {
      junit 'MainApp/app/build/reports/marathon/**/tests/**/*.xml'
      zipAndArchive("hum-espresso-report", "MainApp/app/build/reports/marathon")
    }
  }
}

def mainAppColgateFunctionalTest() {
  stage('test colgate: app functional & espresso tests') {
    try {
      checkDeviceAvailable()
      sh "$MAIN_APP_GRADLE -Pdisable_glthread -PenableScreenshotTesting :app:cleanColgateDebugAndroidTestScreenshots :app:marathonColgateDebugAndroidTest"
      githubNotify context: 'Main App Colgate Functional Test', description: 'All functional tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Main App Colgate Functional Test', description: 'Functional test failed', status: 'ERROR'
      throw e
    } finally {
      junit 'MainApp/app/build/reports/marathon/**/tests/**/*.xml'
      zipAndArchive("colgate-espresso-report", "MainApp/app/build/reports/marathon")
    }
  }
}

def mainAppScreenshotTestsContainAnyIssues(String flavour, String masterSetPath) {
  try {
    sh "ln -s /usr/bin/python2 /usr/bin/python"
    sh "$MAIN_APP_GRADLE pull${flavour}DebugAndroidTestScreenshots"
    sh "$MAIN_APP_GRADLE verify${flavour}DebugAndroidTestScreenshotTest"
    return false
  } catch (e) {
    zipAndArchive("${flavour}-screenshot-failure-report", "MainApp/app/build/screenshots${flavour}DebugAndroidTest/failures")
    zipAndArchive("${flavour}-screenshot-report", "MainApp/app/build/screenshots${flavour}DebugAndroidTest")
    script {
      sh "$MAIN_APP_GRADLE -Pdisable_glthread -PenableScreenshotTesting record${flavour}DebugAndroidTestScreenshotTest"
      sh "$MAIN_APP_GRADLE verify${flavour}DebugAndroidTestScreenshotTest"
      stash includes: "${masterSetPath}/**", name: "${flavour}-master-set"
    }
    return true
  }
}

def mainAppUpdateMasterScreenshotSet(String flavour, String masterSetPath) {
  sshagent(['jenkins-ci-ssh']) {
    sh "\
        git checkout ${env.CHANGE_BRANCH} && \
        git pull origin ${env.CHANGE_BRANCH}"
    unstash "${flavour}-master-set"
    sh "\
        git config --global user.email \"ci@kolibree.com\" && \
        git config --global user.name \"Jenkins\" && \
        git add ${masterSetPath} && \
        git commit -m \"[skipci] Update ${flavour} screenshot master set\" && \
        git lfs push --all origin ${env.CHANGE_BRANCH} && \
        git push origin ${env.CHANGE_BRANCH}"
  }
}

def sdkDemoAppFunctionalTest() {
  stage('test: sdk demo app functional & espresso tests') {
    try {
      checkDeviceAvailable()
      sh "$SDK_DEMO_GRADLE -Pdisable_glthread :app:marathonColgateProductionDebugAndroidTest"
      githubNotify context: 'SDK Demo App Functional Test', description: 'All functional tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'SDK Demo App Functional Test', description: 'Functional test failed', status: 'ERROR'
      throw e
    } finally {
      junit 'SdkDemoApp/app/build/reports/marathon/**/tests/**/*.xml'
      zipAndArchive("demo-espresso-report", "SdkDemoApp/app/build/reports/marathon/*")
    }
  }
}

def glimmerAppFunctionalTest() {
  stage('test: glimmer app functional & espresso tests') {
    try {
      checkDeviceAvailable()
      sh "$GLIMMER_GRADLE -Pdisable_glthread :app:marathonDebugAndroidTest"
      githubNotify context: 'Glimmer App Functional Test', description: 'All functional tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Glimmer App Functional Test', description: 'Functional test failed', status: 'ERROR'
      throw e
    } finally {
      junit 'GlimmerCustomizationApp/app/build/reports/marathon/**/tests/**/*.xml'
      zipAndArchive("glimmer-espresso-report", "GlimmerCustomizationApp/app/build/reports/marathon")
    }
  }
}

def legacyAppFunctionalTest() {
  stage('test: legacy functional & espresso tests') {
    try {
      checkDeviceAvailable()
      sh "$MAIN_APP_GRADLE -Pdisable_glthread :app:marathonColgateDebugAndroidTest"
      githubNotify context: '[Legacy] Main App Functional Test', description: 'All functional tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: '[Legacy] Main App Functional Test', description: 'Functional test failed', status: 'ERROR'
      throw e
    } finally {
      junit 'MainApp/app/build/reports/marathon/**/tests/**/*.xml'
      zipAndArchive("legacy-espresso-report", "MainApp/app/build/reports/marathon")
    }
  }
}

return this;
