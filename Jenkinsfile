#!/usr/bin/env groovy

library 'kolibree-lib'

final ANDROID_BUILD_STATUS_SLACK = 'CCCUZ1VR9'
final ANDROID_DEV_SLACK = '#android-dev'
final MAGIK_ANDROID_SLACK = '#magik_android'

final IS_PR_BRANCH = env.BRANCH_NAME.startsWith('PR-')
final IS_RELEASE_BRANCH = env.BRANCH_NAME.startsWith('release/')
final IS_MAIN_BRANCH = env.BRANCH_NAME == 'master'

final CRON_STRING = IS_MAIN_BRANCH ? 'H H(0-6) * * *' : ''

final CALCULATE_DYNAMIC_VERSION_CODE = IS_MAIN_BRANCH || IS_RELEASE_BRANCH
final CAN_DEPLOY = IS_MAIN_BRANCH || IS_RELEASE_BRANCH
final BUILD_ON_BRANCH_PUSH = IS_MAIN_BRANCH || IS_PR_BRANCH || IS_RELEASE_BRANCH
final BUILD_ON_NIGTLY_BUILDS = IS_MAIN_BRANCH
final PUBLISH_PACT_TESTS = IS_MAIN_BRANCH

final SHOULD_RUN_BUILD_TRACK_1 = true
final SHOULD_RUN_BUILD_TRACK_2 = true
final SHOULD_RUN_FUNCTIONAL_TESTS = !IS_RELEASE_BRANCH

GRADLE_PROPERTY_FLAGS = "--no-daemon -PcalculateDynamicVersionCode=$CALCULATE_DYNAMIC_VERSION_CODE"

SDK_VERSION_CMD = """ cat SDK/dependencies.gradle | grep name | awk -F"'" '{ print \$2}' """
def SDK_VERSION

COLGATE_MAIN_APP_VERSION_CMD = """ cd MainApp && ./gradlew $GRADLE_PROPERTY_FLAGS colgateProdVersion | grep version= | cut -d "=" -f 2 """
def COLGATE_MAIN_APP_VERSION
def COLGATE_MAIN_APP_IN_APP_PRIORITY
HUM_MAIN_APP_VERSION_CMD = """ cd MainApp && ./gradlew $GRADLE_PROPERTY_FLAGS humProdVersion | grep version= | cut -d "=" -f 2 """
def HUM_MAIN_APP_VERSION
def HUM_MAIN_APP_IN_APP_PRIORITY

SDK_GRADLE = "cd SDK && ./gradlew $GRADLE_PROPERTY_FLAGS"
BT_TESTER_GRADLE = "cd BtTesterApp && ./gradlew $GRADLE_PROPERTY_FLAGS"
GLIMMER_GRADLE = "cd GlimmerCustomizationApp && ./gradlew $GRADLE_PROPERTY_FLAGS"
SDK_DEMO_GRADLE = "cd SdkDemoApp && ./gradlew $GRADLE_PROPERTY_FLAGS"
MAIN_APP_GRADLE = "cd MainApp && ./gradlew $GRADLE_PROPERTY_FLAGS"

def deploySdk = false
def deployApp = false

def updateHumMasterScreenshotSet = false
def updateColgateMasterScreenshotSet = false

def generateChangelog() {
  LAST_COMMIT = sh(script: """git log -n 1 --pretty=format:'%h - %an : %s'""", returnStdout: true).trim()
  return "Build number " + env.BUILD_NUMBER + " (" + env.BUILD_URL + ")\nLast change\n" + LAST_COMMIT
}

def injectFlagsAndCredentials() {
  dir('SDK') {
    kolibree android.appendArtifactoryAuthToGradle()
  }
  dir('SdkDemoApp') {
    kolibree android.appendArtifactoryAuthToGradle()
  }
  dir('BtTesterApp') {
    kolibree android.appendArtifactoryAuthToGradle()
  }
  dir('GlimmerCustomizationApp') {
    kolibree android.appendArtifactoryAuthToGradle()
  }
  dir('MainApp') {
    kolibree android.appendArtifactoryAuthToGradle()
  }
  kolibree android.injectJvmArgsToGradleProperties([
    'SDK/gradle.properties',
    'SdkDemoApp/gradle.properties',
    'BtTesterApp/gradle.properties',
    'GlimmerCustomizationApp/gradle.properties',
    'MainApp/gradle.properties'
  ])
  kolibree android.injectArtifactoryOssJcenterToGradle([
    'SDK/build.gradle',
    'SDK/TestingPact/build.gradle',
    'SdkDemoApp/build.gradle',
    'BtTesterApp/build.gradle',
    'GlimmerCustomizationApp/gradle.properties',
    'MainApp/build.gradle'
  ])
  kolibree android.prependBuildCacheToGradleSettings([
    'SDK/settings.gradle',
    'SdkDemoApp/settings.gradle',
    'BtTesterApp/settings.gradle',
    'GlimmerCustomizationApp/gradle.properties',
    'MainApp/settings.gradle'
  ])
}

def CHANGELOG = ""
def ACTIVE_TIMEOUT_MINUTES = 10
def SKIP_BUILD = false


pipeline {

  agent none

  triggers { cron CRON_STRING }

  options {
    buildDiscarder(logRotator(
      artifactDaysToKeepStr: '90',
      artifactNumToKeepStr: '5',
      daysToKeepStr: '180',
      numToKeepStr: '25'
    ))
    timestamps()
    skipDefaultCheckout()
  }

  stages {
    stage('cancel older/unneeded PR builds') {
      when {
        beforeAgent true
        expression { return IS_PR_BRANCH }
      }
      agent {
        label 'debian'
      }
      steps {
        kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
        script {
          SKIP_BUILD = sh(script: 'git log -1 --pretty=%B | grep -w "\\[skipci\\]" || true', returnStdout: true)
          println "result " + SKIP_BUILD
          if (SKIP_BUILD) {
            println "Build skip because of commit message asking to skip ci"
            currentBuild.description = "Build skip because of commit message asking to skip ci"
          } else {
            kolibree buildCancel.cancelOlderRunningBuilds()
          }
        }
      }
    }
    stage('build and func tests') {
      failFast true
      when {
        beforeAgent true
        allOf {
          expression { return !SKIP_BUILD }
          anyOf {
            expression { return buildCause.isScm() && BUILD_ON_BRANCH_PUSH }
            expression { return buildCause.isUser() }
            expression { return buildCause.isTimer() && BUILD_ON_NIGTLY_BUILDS }
            expression { return !buildCause.isScm() && !buildCause.isUser() && !buildCause.isTimer() }
            //safety fallback
          }
        }
      }
      parallel {
        stage('track1: wait for build agent') {
          when {
            beforeAgent true
            expression { return SHOULD_RUN_BUILD_TRACK_1 }
          }
          agent {
            kubernetes {
              label agentUtilities.getDynamicAgentLabel('android')
              defaultContainer 'android-sdk'
              yamlFile 'KubernetesPod.yaml'
            }
          }
          stages {
            stage('build track 1') {
              options {
                // This timeout ensure that we don't wait infinitely if pod crash or gradle hangs forever
                // active flag tell that it will restart the timeout each time an activity is spotted like a log
                timeout(time: ACTIVE_TIMEOUT_MINUTES, unit: 'MINUTES', activity: true)
              }
              environment {
                CP_KEYSTORE_PATH = credentials('colgate-android-keystore-file-prod')
                CP_KEY = credentials('colgate-connect-android-signing-credentials')
                CP_STORE_PASSWORD = credentials('cp-android-keystore-credentials-prod')
                MODULE_SIGNING = credentials('kolibree-modules-android-signing-credentials')
                KL_KEYSTORE_PATH = credentials('kolibree-android-keystore-file-prod')
                KL_KEY = credentials('kolibree-app-android-signing-credentials')
                KL_STORE_PASSWORD = credentials('kl-android-keystore-credentials-prod')
                KL_BETA_KEYSTORE_PATH = credentials('kolibree-android-keystore-file-beta')
                KL_BETA_KEY = credentials('kolibree-android-beta-signing-credentials')
                KL_BETA_STORE_PASSWORD = credentials('kolibree-android-keystore-credentials-beta')
              }
              stages {
                stage('init') {
                  steps {
                    kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                    script { injectFlagsAndCredentials() }
                  }
                }

                stage('configure: versions & changelog') {
                  steps {
                    script {
                      SDK_VERSION = sh(script: SDK_VERSION_CMD, returnStdout: true).trim()
                      COLGATE_MAIN_APP_VERSION = sh(script: COLGATE_MAIN_APP_VERSION_CMD, returnStdout: true).trim()
                      HUM_MAIN_APP_VERSION = sh(script: HUM_MAIN_APP_VERSION_CMD, returnStdout: true).trim()
                      CHANGELOG = generateChangelog()
                      stash includes: 'jenkins/**', name: 'groovy-scripts'
                    }
                  }
                }

                stage('run') {
                  steps {
                    script {
                      def tools = load "jenkins/tools.groovy"
                      def build = load "jenkins/build.groovy"
                      def deploy = load "jenkins/deploy.groovy"

                      build.humRelease(CAN_DEPLOY)
                      if (IS_RELEASE_BRANCH) {
                        build.humReleaseApk(true)
                      }

                      build.colgateRelease(CAN_DEPLOY)
                      if (IS_RELEASE_BRANCH) {
                        build.colgateReleaseApk(true)
                      }

                      build.glimmerCustomizationAppRelease(CAN_DEPLOY)

                      tools.patchSDKForDeployment()

                      build.sdkRelease()

                      tools.validatePom()
                      tools.packageSdkRelease(CAN_DEPLOY)

                      build.sdkDemoAppRelease(CAN_DEPLOY)

                      if (CAN_DEPLOY && currentBuild.resultIsBetterOrEqualTo('SUCCESS')) {
                        deploy.sdkSnapshots()
                      }
                    }
                  }
                }
              }
            }
          }
        }

        stage('track2: wait for build agent') {
          when {
            beforeAgent true
            expression { return SHOULD_RUN_BUILD_TRACK_2 }
          }
          agent {
            kubernetes {
              label agentUtilities.getDynamicAgentLabel('android')
              defaultContainer 'android-sdk'
              yamlFile 'KubernetesPod.yaml'
            }
          }
          stages {
            stage('build track 2') {
              options {
                // This timeout ensure that we don't wait infinitely if pod crash or gradle hangs forever
                // active flag tell that it will restart the timeout each time an activity is spotted like a log
                timeout(time: ACTIVE_TIMEOUT_MINUTES, unit: 'MINUTES', activity: true)
              }
              environment {
                PACT_BROKER_CREDENTIALS = credentials('pact-broker-credentials')
                CP_KEYSTORE_PATH = credentials('colgate-android-keystore-file-prod')
                CP_KEY = credentials('colgate-connect-android-signing-credentials')
                CP_STORE_PASSWORD = credentials('cp-android-keystore-credentials-prod')
              }
              stages {
                stage('init') {
                  steps {
                    kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                    script { injectFlagsAndCredentials() }
                  }
                }
                stage('run') {

                  environment {
                    DANGER_GITHUB_API_TOKEN = credentials('kolibree-ci-access-token')
                  }

                  steps {
                    script {
                      def analysis = load "jenkins/analysis.groovy"
                      def tools = load "jenkins/tools.groovy"
                      def unitTest = load "jenkins/unitTest.groovy"
                      def build = load "jenkins/build.groovy"
                      def danger = load "jenkins/danger/danger.groovy"

                      danger.install()

                      danger.runCommonWarning()

                      // In order to display an exhaustive list of the issues,
                      // the analysis tools will all run at once before throwing
                      // an exception if there is one.
                      List<Exception> exceptions = []

                      if (IS_PR_BRANCH) {
                        runAndRecordException(exceptions) { tools.detectRoomSchemaChanges() }
                      }

                      runAndRecordException(exceptions) { analysis.sdkStaticChecks() }

                      runAndRecordException(exceptions) { analysis.mainAppStaticChecks() }

                      runAndRecordException(exceptions) { analysis.glimmerAppStaticChecks() }

                      runAndRecordException(exceptions) { tools.checkHumDependencies() }

                      runAndRecordException(exceptions) { analysis.lint() }

                      danger.runAnalysisWarning()

                      //Propagate the previous thrown exceptions
                      if (!exceptions.isEmpty()) {
                        throw exceptions.remove(0).with(true) { firstException ->
                          exceptions.each { firstException.addSuppressed(it) }
                        }
                      }

                      unitTest.sdkUnitTest(PUBLISH_PACT_TESTS)
                      unitTest.mainAppUnitTest()
                      unitTest.glimmerUnitTest()

                      build.bTTesterRelease(CAN_DEPLOY)

                      danger.runDeployMessage()
                    }
                  }
                }
              }
            }
          }
        }

        stage('wait for UI agent') {
          when {
            beforeAgent true
            expression { return SHOULD_RUN_FUNCTIONAL_TESTS }
          }

          agent {
            kubernetes {
              label agentUtilities.getDynamicAgentLabel('android-ui')
              defaultContainer 'anbox-with-sdk'
              yamlFile 'KubernetesPodAind.yaml'
            }
          }

          options { retry(2) }

          stages {
            stage('init') {
              steps {
                kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                script { injectFlagsAndCredentials() }
              }
            }
            stage('run') {
              options {
                // This timeout ensure that we don't wait infinitely if pod crash or gradle hangs forever
                // active flag tell that it will restart the timeout each time an activity is spotted like a log
                timeout(time: ACTIVE_TIMEOUT_MINUTES, unit: 'MINUTES', activity: true)
              }
              environment {
                DANGER_GITHUB_API_TOKEN = credentials('kolibree-ci-access-token')
              }
              steps {
                script {
                  def uiTest = load "jenkins/uiTest.groovy"

                  if (buildCause.isTimer() && BUILD_ON_NIGTLY_BUILDS) {
                    uiTest.sdkFunctionalTest()
                  }

                  uiTest.mainAppHumFunctionalTest()

                  // TODO uncomment after issues with screenshot tests are resolved
                  // stage('test hum: app screenshot tests') {
                  //   updateHumMasterScreenshotSet = uiTest.mainAppScreenshotTestsContainAnyIssues("Hum", "MainApp/screenshot_master_set/hum")
                  // }
                  //
                  // def danger = load "jenkins/danger/danger.groovy"
                  // danger.install()
                  // danger.runScreenshotWarning("Hum", updateHumMasterScreenshotSet)
                }
              }
            }
          }

          post {
            always {
              sh "adb devices"
              sh "dmesg"
              sh "cat /var/lib/anbox/logs/console.log"
              sh "cat /var/lib/anbox/logs/container.log"
              sh "/usr/local/bin/anbox system-info"
              sh "ls /sys/module/fuse/parameters/"
              sh "systemctl -l status anbox-container-manager"
            }
          }
        }

        stage('wait for UI agent 2') {
          when {
            beforeAgent true
            expression { return SHOULD_RUN_FUNCTIONAL_TESTS }
          }

          agent {
            kubernetes {
              label agentUtilities.getDynamicAgentLabel('android-ui')
              defaultContainer 'anbox-with-sdk'
              yamlFile 'KubernetesPodAind.yaml'
            }
          }


          options { retry(2) }


          stages {
            stage('init') {
              steps {
                kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                script { injectFlagsAndCredentials() }
              }
            }
            stage('run') {
              options {
                // This timeout ensure that we don't wait infinitely if pod crash or gradle hangs forever
                // active flag tell that it will restart the timeout each time an activity is spotted like a log
                timeout(time: ACTIVE_TIMEOUT_MINUTES, unit: 'MINUTES', activity: true)
              }
              environment {
                DANGER_GITHUB_API_TOKEN = credentials('kolibree-ci-access-token')
              }
              steps {
                script {
                  def uiTest = load "jenkins/uiTest.groovy"

                  uiTest.mainAppColgateFunctionalTest()

                  // TODO uncomment after Screenshot plugin is fixed
                  // stage('test colgate: app screenshot tests') {
                  //   updateColgateMasterScreenshotSet = uiTest.mainAppScreenshotTestsContainAnyIssues("Colgate", "MainApp/screenshot_master_set/colgateConnect")
                  // }

                  // def danger = load "jenkins/danger/danger.groovy"
                  // danger.install()
                  // danger.runScreenshotWarning("Colgate", updateColgateMasterScreenshotSet)

                  uiTest.sdkDemoAppFunctionalTest()
                }
              }
            }
          }
          post {
            always {
              sh "adb devices"
              sh "dmesg"
              sh "cat /var/lib/anbox/logs/console.log"
              sh "cat /var/lib/anbox/logs/container.log"
              sh "/usr/local/bin/anbox system-info"
              sh "ls /sys/module/fuse/parameters/"
              sh "systemctl -l status anbox-container-manager"
            }
          }
        }
      }
    }

    stage('update: master screenshot sets') {
      when {
        allOf {
          expression { return !SKIP_BUILD }
          expression { return IS_PR_BRANCH }
          expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
        }
      }
      failFast false
      parallel {
        stage('hum: update master screenshot set') {
          when {
            beforeAgent true
            beforeInput true
            allOf {
              expression { updateHumMasterScreenshotSet }
              expression { return !buildCause.isTimer() }
              expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
            }
          }
          options {
            timeout(time: 5, unit: 'DAYS')
          }
          stages {
            stage("input") {
              steps {
                script {
                  try {
                    input(message: '''Hum screenshot test failed!\nIs that expected?''', ok: 'Yes, I expected them to fail, please update the master set')
                  } catch (Exception) {
                    updateHumMasterScreenshotSet = false
                    error("Screenshot master set update aborted")
                  }
                }
              }
            }
            stage("update") {
              when {
                beforeAgent true
                expression { updateHumMasterScreenshotSet }
              }
              agent {
                label 'debian'
              }
              stages {
                stage('test hum: update screenshot master set') {
                  steps {
                    kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                    script {
                      def uiTest = load "jenkins/uiTest.groovy"
                      uiTest.mainAppUpdateMasterScreenshotSet("Hum", "MainApp/screenshot_master_set/hum")
                    }
                  }
                }
              }
            }
          }
        }
        stage('colgate: update master screenshot set') {
          when {
            beforeAgent true
            beforeInput true
            allOf {
              expression { updateColgateMasterScreenshotSet }
              expression { return !buildCause.isTimer() }
              expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
            }
          }
          options {
            timeout(time: 5, unit: 'DAYS')
          }
          stages {
            stage("input") {
              steps {
                script {
                  try {
                    input(message: '''Colgate screenshot test failed!\nIs that expected?''', ok: 'Yes, I expected them to fail, please update the master set')
                  } catch (Exception) {
                    updateColgateMasterScreenshotSet = false
                    error("Screenshot master set update aborted")
                  }
                }
              }
            }
            stage("update") {
              when {
                beforeAgent true
                expression { updateColgateMasterScreenshotSet }
              }
              agent {
                label 'debian'
              }
              stages {
                stage('test colgate: update screenshot master set') {
                  steps {
                    kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                    script {
                      def uiTest = load "jenkins/uiTest.groovy"
                      uiTest.mainAppUpdateMasterScreenshotSet("Colgate", "MainApp/screenshot_master_set/colgateConnect")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    stage('deploy: beta') {
      when {
        beforeAgent true
        allOf {
          expression { return !SKIP_BUILD }
          expression { return IS_MAIN_BRANCH }
          expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
        }
      }
      environment {
        APP_CENTER_KEY = credentials('jenkins-app-center-token')
      }
      agent {
        label 'debian'
      }
      stages {
        stage('run') {
          steps {
            script {
              unstash 'groovy-scripts'
              def deploy = load "jenkins/deploy.groovy"

              deploy.humBetaAppCenter(CHANGELOG)
              deploy.colgateBetaAppCenter(CHANGELOG)

              deploy.sdkDemoAppCenter(CHANGELOG)
              deploy.btTesterAppCenter(CHANGELOG)
              deploy.glimmerAppCenter(CHANGELOG)
            }
          }
        }
      }
    }

    stage('print release versions') {
      when {
        expression { return !SKIP_BUILD }
      }
      steps {
        println "Changelog: " + CHANGELOG
        println "SDK version name: " + SDK_VERSION
        println "Colgate Connect version name:" + COLGATE_MAIN_APP_VERSION
        println "Hum version name:" + HUM_MAIN_APP_VERSION
      }
    }

    stage('deploy: internal release') {
      when {
        beforeAgent true
        allOf {
          expression { return !SKIP_BUILD }
          expression { return IS_RELEASE_BRANCH }
          expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
        }
      }
      environment {
        APP_CENTER_KEY = credentials('jenkins-app-center-token')
      }
      agent {
        label 'debian'
      }
      stages {
        stage('run') {
          steps {
            script {
              unstash 'groovy-scripts'
              def deploy = load "jenkins/deploy.groovy"

              deploy.humReleaseAppCenter(CHANGELOG)
              deploy.colgateReleaseAppCenter(CHANGELOG)
            }
          }
        }
      }
    }

    stage('deploy: live release') {
      when {
        allOf {
          expression { return !SKIP_BUILD }
          expression { return IS_RELEASE_BRANCH }
          expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
        }
      }
      failFast false
      parallel {
        stage('deploy: sdk') {
          when {
            beforeAgent true
            beforeInput true
            allOf {
              expression { return !buildCause.isTimer() }
              expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
            }
          }
          options {
            timeout(time: 5, unit: 'DAYS')
          }
          stages {
            stage("input") {
              steps {
                script {
                  try {
                    input(message: '''Deploy the SDK?\nPlease check print release versions stage for version name''', ok: 'Deploy')
                    deploySdk = true
                  } catch (Exception) {
                    println "Deploy SDK aborted"
                  }
                }
              }
            }
            stage("deploy") {
              when {
                beforeAgent true
                expression { deploySdk }
              }
              agent {
                kubernetes {
                  label agentUtilities.getDynamicAgentLabel('android')
                  defaultContainer 'android-sdk'
                  yamlFile 'KubernetesPod.yaml'
                }
              }
              stages {
                stage('sdk deploy: validate tag') {
                  steps {
                    script {
                      if (!(SDK_VERSION =~ '\\d\\.\\d+\\.\\d+\\w*')) {
                        error("Failed match")
                      }
                    }
                  }
                  post {
                    failure {
                      error("Invalid Tag '${SDK_VERSION}'. Suggestions: 4.4.12")
                    }
                  }
                }
                stage('sdk deploy: maven') {
                  steps {
                    unstash 'sdk-workspace'
                    sh "$SDK_GRADLE artifactoryPublish"
                  }
                }
                stage('sdk deploy: tar bundle') {
                  options {
                    withAWS(credentials: 'aws-s3-bucket-credentials', region: 'us-east-1')
                  }
                  steps {
                    script {
                      sh 'mv SDK/release*.tar .'
                      sh 'tar -xvf release*.tar'
                      sh 'cp release/CHANGELOG.md CHANGELOG.md'

                      // TODO should we also add suffix here ?

                      def RELEASE_VERSION = sh(
                        script: 'cat release/version.txt',
                        returnStdout: true
                      ).trim()

                      def uploadSpec = """{
                        "files": [{
                          "pattern": "release_${RELEASE_VERSION}.tar",
                          "target": "generic-local/jiezhong/android/sdk-release/"
                        }]
                      }"""

                      def artifactory = Artifactory.server 'kolibree-artifactory'
                      artifactory.upload(uploadSpec)

                      def filesToUpload = [
                        [localTarget: 'release/SDK_ANDROID.pdf', bucketTarget: "android/${RELEASE_VERSION}/getting_started/SDK_ANDROID.pdf"],
                        [localTarget: 'SDK/CHANGELOG.md', bucketTarget: 'android/CHANGELOG.md']
                      ]
                      filesToUpload.each { file ->
                        s3Upload acl: 'BucketOwnerFullControl',
                          bucket: 'docs.kolibree.com',
                          file: file.localTarget,
                          path: file.bucketTarget,
                          pathStyleAccessEnabled: true
                      }
                    }
                  }
                }
              }
              post {
                success {
                  sh "mv ./SDK/CHANGELOG.md ."
                  archiveArtifacts artifacts: "CHANGELOG.md"
                  slackSend channel: "${MAGIK_ANDROID_SLACK}", color: 'good', message: ":information_desk_person: Hello! New version of Android SDK *${SDK_VERSION}* has been released!\n\nPlease check the changelog for more details: https://github.com/kolibree-git/android-monorepo/blob/master/SDK/CHANGELOG.md"
                  build job: 'kolibree-git/sdk-documentation-builder/master', wait: false
                  sshagent(['jenkins-ci-ssh']) {
                    sh "rm -rf *"
                    kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                    sh "git tag sdk/${SDK_VERSION}"
                    sh "git push origin sdk/${SDK_VERSION}"
                  }
                }
              }
            }
          }
        }

        stage('deploy: Colgate Connect (Google Play)') {
          when {
            beforeAgent true
            beforeInput true
            allOf {
              expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
              expression { return !buildCause.isTimer() }
            }
          }
          options {
            timeout(time: 5, unit: 'DAYS')
          }
          stages {
            stage("input") {
              steps {
                script {
                  try {
                    def output = input(message: '''[Colgate Connect]\n Deploy Release version of the application on Google Play?\nPlease check print release versions stage for version name''',
                      ok: 'Deploy',
                      parameters: [
                        string(defaultValue: '',
                          description: 'Append a suffix to version tag (git only), leave it empty if you don\'t want a suffix (don\'t need to add dash)',
                          name: 'suffix',
                          trim: true),
                        choice(defaultValue: "0",
                          description: 'In App priority \n0 (default) = nothing prompt,\n [1,4] = flexible update,\n 5 = mandatory update',
                          choices: "0\n1\n2\n3\n4\n5",
                          name: 'inAppPriority')
                      ])
                    if (output.suffix != "") {
                      COLGATE_MAIN_APP_VERSION += "-" + output.suffix
                    }
                    COLGATE_MAIN_APP_IN_APP_PRIORITY = output.inAppPriority
                    println "Release ColgateConnect version " + COLGATE_MAIN_APP_VERSION + " with inAppPriority " + COLGATE_MAIN_APP_IN_APP_PRIORITY
                    deployApp = true
                  } catch (Exception) {
                    println "Deploy App aborted"
                  }
                }
              }
            }
            stage("deploy") {
              when {
                beforeAgent true
                expression { deployApp }
              }
              agent {
                label 'debian'
              }
              stages {
                stage('app deploy: validate tag') {
                  steps {
                    script {
                      if (!(COLGATE_MAIN_APP_VERSION =~ '\\d\\.\\d+\\.\\d+\\w*')) {
                        error("Failed match")
                      }
                    }
                  }
                  post {
                    failure {
                      error("Invalid Tag '${COLGATE_MAIN_APP_VERSION}'. Suggestions: 1.2.0, 1.2.0RC1")
                    }
                  }
                }

                stage('app deploy: GooglePlay') {
                  steps {
                    unstash 'binaries-colgate'
                    androidApkUpload apkFilesPattern: "MainApp/app/build/outputs/bundle/colgateRelease/app-colgate-release.aab",
                      googleCredentialsId: 'Colgate Google Play Publisher', trackName: 'internal', rolloutPercentage: '100',
                      inAppUpdatePriority: COLGATE_MAIN_APP_IN_APP_PRIORITY
                  }
                }
              }
              post {
                success {
                  sshagent(['jenkins-ci-ssh']) {
                    sh "rm -rf *"
                    kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                    sh "git tag ${COLGATE_MAIN_APP_VERSION}"
                    sh "git push origin ${COLGATE_MAIN_APP_VERSION}"
                  }
                  slackSend channel: "${ANDROID_DEV_SLACK}", color: 'good', message: ":information_desk_person: Hello! Colgate Connect *${COLGATE_MAIN_APP_VERSION}* has been released to Google Play! :tada::tada::tada:"
                }
              }
            }
          }
        }
        stage('deploy: Hum (Google Play)') {
          when {
            beforeAgent true
            beforeInput true
            allOf {
              expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
              expression { return !buildCause.isTimer() }
            }
          }
          options {
            timeout(time: 5, unit: 'DAYS')
          }
          stages {
            stage("input") {
              steps {
                script {
                  try {
                    def output = input(message: '''[Hum]\n Deploy Hum Release version on Google Play?\nPlease check print release versions stage for version name''',
                      ok: 'Deploy',
                      parameters: [
                        string(defaultValue: '',
                        description: 'Append a suffix to version tag (git only), leave it empty if you don\'t want a suffix (don\'t need to add dash)',
                        name: 'suffix',
                        trim: true),
                        choice(defaultValue: "0",
                          description: 'In App priority \n0 (default) = nothing prompt,\n [1,4] = flexible update,\n 5 = mandatory update',
                          choices: "0\n1\n2\n3\n4\n5",
                          name: 'inAppPriority')
                      ])
                    if (output.suffix != "") {
                      HUM_MAIN_APP_VERSION += "-" + output.suffix
                    }
                    HUM_MAIN_APP_IN_APP_PRIORITY = output.inAppPriority
                    println "Release HUM version " + HUM_MAIN_APP_VERSION + " with inAppPriority " + HUM_MAIN_APP_IN_APP_PRIORITY
                    deployApp = true
                  } catch (Exception) {
                    println "Deploy App aborted"
                  }
                }
              }
            }
            stage("deploy") {
              when {
                beforeAgent true
                expression { deployApp }
              }
              agent {
                label 'debian'
              }
              stages {
                stage('app deploy: validate tag') {
                  steps {
                    script {
                      if (!(HUM_MAIN_APP_VERSION =~ '\\d\\.\\d+\\.\\d+\\w*')) {
                        error("Failed match")
                      }
                    }
                  }
                  post {
                    failure {
                      error("Invalid Tag '${HUM_MAIN_APP_VERSION}'. Suggestions: 1.2.0, 1.2.0RC1")
                    }
                  }
                }

                stage('app deploy: GooglePlay') {
                  steps {
                    unstash 'binaries-hum'
                    androidApkUpload apkFilesPattern: "MainApp/app/build/outputs/bundle/humRelease/app-hum-release.aab",
                      googleCredentialsId: 'Hum Google Play Publisher', trackName: 'internal', rolloutPercentage: '100',
                      inAppUpdatePriority: HUM_MAIN_APP_IN_APP_PRIORITY
                  }
                }
              }
              post {
                success {
                  sshagent(['jenkins-ci-ssh']) {
                    sh "rm -rf *"
                    kolibree advancedClone(gitLFSPull: true, sshAuth: 'jenkins-ci-ssh')
                    sh "git tag ${HUM_MAIN_APP_VERSION}"
                    sh "git push origin ${HUM_MAIN_APP_VERSION}"
                  }
                  slackSend channel: "${ANDROID_DEV_SLACK}", color: 'good', message: ":information_desk_person: Hello! HUM *${HUM_MAIN_APP_VERSION}* has been released to Google Play! :tada::tada::tada:"
                }
              }
            }
          }
        }
      }
    }
  }
  post {
    failure {
      script {
        slackSend channel: "${ANDROID_BUILD_STATUS_SLACK}", color: 'danger', message: "Build android-monorepo/${env.BRANCH_NAME}/${env.BUILD_NUMBER} failed :disappointed:\n\nPlease check ${env.BUILD_URL}flowGraphTable/ or open Blue Ocean to check what went wrong."
        if (IS_MAIN_BRANCH) {
          slackSend channel: "${ANDROID_DEV_SLACK}", color: 'danger', message: "Master build ${env.BUILD_NUMBER} failed! :disappointed:\nPlease check ${env.BUILD_URL}."
        }
      }
    }
  }
}

private static runAndRecordException(List<Exception> exceptions, closure) {
  try {
    closure()
  } catch (Exception e) {
    exceptions.add(e)
  }
}
