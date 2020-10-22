#!usr/bin/env groovy


/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

def humRelease(boolean canDeploy) {
  stage('build: hum') {
    try {
      sh "cd SDK/StaticResourcesBranded && sed -i.bak \"s/def isHumBuild = .*/def isHumBuild = true/g\" build.gradle"
      sh "$MAIN_APP_GRADLE -PreleaseStoreFilePath='$CP_KEYSTORE_PATH' -PreleaseAlias='$CP_KEY_USR' -PreleaseKeyPass='$CP_KEY_PSW' -PreleaseAliasPass='$CP_STORE_PASSWORD' -PbetaStoreFilePath='$KL_BETA_KEYSTORE_PATH' -PbetaAlias='$KL_BETA_KEY_USR' -PbetaKeyPass='$KL_BETA_KEY_PSW' -PbetaAliasPass='$KL_BETA_STORE_PASSWORD' :app:assembleHumBeta :app:bundleHumRelease"
      // Only stash if we can deploy otherwise it's a waste of time and space
      if (canDeploy) {
        stash includes: 'MainApp/app/build/outputs/apk/hum/**, MainApp/app/build/outputs/bundle/humRelease/**', name: 'binaries-hum'
      }
      archiveArtifacts artifacts: 'MainApp/app/build/outputs/apk/**'
      archiveArtifacts artifacts: 'MainApp/app/build/outputs/bundle/**'
      sh 'rm -rf MainApp/app/build'
      githubNotify context: 'Hum App build', description: 'build succeed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Hum App build', description: 'build failed', status: 'ERROR'
      throw e
    }
  }
}

def colgateRelease(boolean canDeploy) {
  stage('build: colgate') {
    try {
      sh "cd SDK/StaticResourcesBranded && sed -i.bak \"s/def isHumBuild = .*/def isHumBuild = false/g\" build.gradle"
      sh "$MAIN_APP_GRADLE -PreleaseStoreFilePath='$CP_KEYSTORE_PATH' -PreleaseAlias='$CP_KEY_USR' -PreleaseKeyPass='$CP_KEY_PSW' -PreleaseAliasPass='$CP_STORE_PASSWORD' -PbetaStoreFilePath='$KL_BETA_KEYSTORE_PATH' -PbetaAlias='$KL_BETA_KEY_USR' -PbetaKeyPass='$KL_BETA_KEY_PSW' -PbetaAliasPass='$KL_BETA_STORE_PASSWORD' :app:assembleColgateBeta :app:bundleColgateRelease"
      // Only stash if we can deploy otherwise it's a waste of time and space
      if (canDeploy) {
        stash includes: 'MainApp/app/build/outputs/apk/colgate/**, MainApp/app/build/outputs/bundle/colgateRelease/**', name: 'binaries-colgate'
      }
      archiveArtifacts artifacts: 'MainApp/app/build/outputs/apk/**'
      archiveArtifacts artifacts: 'MainApp/app/build/outputs/bundle/**'
      sh 'rm -rf MainApp/app/build'
      githubNotify context: 'Colgate App build', description: 'build succeed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Colgate App build', description: 'build failed', status: 'ERROR'
      throw e
    }
  }
}

def humReleaseApk(boolean canDeploy) {
  stage('build: hum release apk') {
    try {
      sh "$MAIN_APP_GRADLE -PreleaseStoreFilePath='$CP_KEYSTORE_PATH' -PreleaseAlias='$CP_KEY_USR' -PreleaseKeyPass='$CP_KEY_PSW' -PreleaseAliasPass='$CP_STORE_PASSWORD' :app:assembleHumRelease"
      // Only stash if we can deploy otherwise it's a waste of time and space
      if (canDeploy) {
        stash includes: 'MainApp/app/build/outputs/apk/hum/**', name: 'binaries-hum-release'
      }
      archiveArtifacts artifacts: 'MainApp/app/build/outputs/apk/**'
      sh 'rm -rf MainApp/app/build'
      githubNotify context: 'Hum App build', description: 'build succeed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Hum App build', description: 'build failed', status: 'ERROR'
      throw e
    }
  }
}

def colgateReleaseApk(boolean canDeploy) {
  stage('build: colgate release apk') {
    try {
      sh "$MAIN_APP_GRADLE -PreleaseStoreFilePath='$CP_KEYSTORE_PATH' -PreleaseAlias='$CP_KEY_USR' -PreleaseKeyPass='$CP_KEY_PSW' -PreleaseAliasPass='$CP_STORE_PASSWORD' :app:assembleColgateRelease"
      // Only stash if we can deploy otherwise it's a waste of time and space
      if (canDeploy) {
        stash includes: 'MainApp/app/build/outputs/apk/colgate/**', name: 'binaries-colgate-release'
      }
      archiveArtifacts artifacts: 'MainApp/app/build/outputs/apk/**'
      sh 'rm -rf MainApp/app/build'
      githubNotify context: 'Colgate App build', description: 'build succeed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Colgate App build', description: 'build failed', status: 'ERROR'
      throw e
    }
  }
}

def sdkRelease() {
  stage('build: sdk') {
    try {
      sh "$SDK_GRADLE assembleRelease -PstoreFilePath=$KL_KEYSTORE_PATH -PkeyPass=$KL_STORE_PASSWORD -PaliasPass=$KL_STORE_PASSWORD"
      githubNotify context: 'SDK build', description: 'build succeed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'SDK build', description: 'build failed', status: 'ERROR'
      throw e
    }
  }
}

def sdkDemoAppRelease(boolean canDeploy) {
  stage('build: sdk demo') {
    try {
      sh "$SDK_DEMO_GRADLE -PstoreFilePath=$KL_KEYSTORE_PATH -Palias=$KL_KEY_USR -PkeyPass=$KL_KEY_PSW -PaliasPass=$KL_STORE_PASSWORD :app:assembleColgateProductionRelease"
      // Only stash if we can deploy otherwise it's a waste of time and space
      if (canDeploy) {
        stash includes: 'SdkDemoApp/app/build/outputs/apk/**', name: 'binaries-sdk-demo-app'
      }
      archiveArtifacts artifacts: 'SdkDemoApp/app/build/outputs/apk/**'
      sh 'rm -rf SdkDemoApp/app/build'
      githubNotify context: 'SDK Demo App build', description: 'build succeed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'SDK Demo App build', description: 'build failed', status: 'ERROR'
      throw e
    }
  }
}

def glimmerCustomizationAppRelease(boolean canDeploy) {
  stage('build: glimmer customization app') {
    try {
      sh "$GLIMMER_GRADLE -PstoreFilePath=$KL_KEYSTORE_PATH -Palias=$KL_KEY_USR -PkeyPass=$KL_KEY_PSW -PaliasPass=$KL_STORE_PASSWORD assembleRelease"
      // Only stash if we can deploy otherwise it's a waste of time and space
      if (canDeploy) {
        stash includes: 'GlimmerCustomizationApp/app/build/outputs/apk/**', name: 'binaries-glimmer-app'
      }
      archiveArtifacts artifacts: 'GlimmerCustomizationApp/app/build/outputs/apk/**'
      sh 'rm -rf GlimmerCustomizationApp/app/build'
      githubNotify context: 'Glimmer Customization App build', description: 'build succeed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Glimmer Customization App build', description: 'build failed', status: 'ERROR'
      throw e
    }
  }
}

def bTTesterRelease(boolean canDeploy) {
  stage('build: bt tester') {
    try {
      sh "$BT_TESTER_GRADLE -PstoreFilePath=$CP_KEYSTORE_PATH -Palias=$CP_KEY_USR -PkeyPass=$CP_KEY_PSW -PaliasPass=$CP_STORE_PASSWORD :app:assembleRelease"
      if (canDeploy) {
        // Only stash if we can deploy otherwise it's a waste of time and space
        stash includes: 'BtTesterApp/app/build/outputs/apk/**', name: 'binaries-bt-tester'
      }
      archiveArtifacts artifacts: 'BtTesterApp/app/build/outputs/apk/**'
      githubNotify context: 'BTTester build', description: 'build succeed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'BTTester build', description: 'build failed', status: 'ERROR'
      throw e
    }
  }
}


//            stage('build: careos') {
//              when {
//                expression { return SHOULD_BUILD_CAREOS }
//              }
//              environment {
//                KL_KEYSTORE_PATH = credentials('kolibree-android-keystore-file-prod')
//                KL_KEY = credentials('kolibree-app-android-signing-credentials')
//                KL_STORE_PASSWORD = credentials('kl-android-keystore-credentials-prod')
//                KL_BETA_KEYSTORE_PATH = credentials('kolibree-android-keystore-file-beta')
//                KL_BETA_KEY = credentials('kolibree-android-beta-signing-credentials')
//                KL_BETA_STORE_PASSWORD = credentials('kolibree-android-keystore-credentials-beta')
//              }
//              steps {
//                sh "$MAIN_APP_GRADLE -PreleaseStoreFilePath='$KL_KEYSTORE_PATH' -PreleaseAlias='$KL_KEY_USR' -PreleaseKeyPass='$KL_KEY_PSW' -PreleaseAliasPass='$KL_STORE_PASSWORD' -PbetaStoreFilePath='$KL_BETA_KEYSTORE_PATH' -PbetaAlias='$KL_BETA_KEY_USR' -PbetaKeyPass='$KL_BETA_KEY_PSW' -PbetaAliasPass='$KL_BETA_STORE_PASSWORD' :app:assembleCareOSBeta :app:assembleCareOSRelease"
//              }
//              post {
//                success {
//                  script {
//                    // Only stash if we can deploy otherwise it's a waste of time and space
//                    if (CAN_DEPLOY) {
//                      stash includes: 'MainApp/app/build/outputs/apk/careOS/**, jenkins/**', name: 'binaries-careos'
//                    }
//                    archiveArtifacts artifacts: 'MainApp/app/build/outputs/apk/**'
//                  }
//                  sh 'rm -rf MainApp/app/build'
//                }
//              }
//            }

return this;
