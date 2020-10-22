#!usr/bin/env groovy


/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

def sdkSnapshots() {
  stage('deploy: sdk snapshots') {
    try {
      sh "$SDK_GRADLE artifactoryPublish -PpublishSnapshot=true"
      githubNotify context: 'SDK snapshot', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'SDK snapshot', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

def humBetaAppCenter(String changelog) {
  stage('hum beta deploy: AppCenter') {
    try {
      unstash 'binaries-hum'
      def buildType = 'beta'
      def buildVariant = "hum"
      appCenter apiToken: "$env.APP_CENTER_KEY",
        ownerName: 'CP-Mobile-Dev',
        appName: 'hum',
        pathToApp: "MainApp/app/build/outputs/apk/${buildVariant}/${buildType}/app-${buildVariant}-${buildType}.apk",
        distributionGroups: 'Core PM - CP-KQL, QA, Android Team',
        releaseNotes: "${changelog}"
      githubNotify context: 'Hum beta AppCenter', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Hum beta AppCenter', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

def humReleaseAppCenter(String changelog) {
  stage('hum release deploy: AppCenter') {
    try {
      unstash 'binaries-hum-release'
      def buildType = 'release'
      def buildVariant = "hum"
      appCenter apiToken: "$env.APP_CENTER_KEY",
        ownerName: 'CP-Mobile-Dev',
        appName: 'hum',
        pathToApp: "MainApp/app/build/outputs/apk/${buildVariant}/${buildType}/app-${buildVariant}-${buildType}.apk",
        distributionGroups: 'Core PM - CP-KQL, QA, Android Team',
        releaseNotes: "${changelog}"
      githubNotify context: 'Hum release AppCenter', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Hum release AppCenter', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

def colgateBetaAppCenter(String changelog) {
  stage('colgate beta deploy: AppCenter') {
    try {
      unstash 'binaries-colgate'
      def buildType = 'beta'
      def buildVariant = "colgate"
      appCenter apiToken: "$env.APP_CENTER_KEY",
        ownerName: 'CP-Mobile-Dev',
        appName: 'Colgate-Connect',
        pathToApp: "MainApp/app/build/outputs/apk/${buildVariant}/${buildType}/app-${buildVariant}-${buildType}.apk",
        distributionGroups: 'Core PM - CP-KQL, QA, Android Team',
        releaseNotes: "${changelog}"
      githubNotify context: 'Colgate beta AppCenter', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Colgate beta AppCenter', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

def colgateReleaseAppCenter(String changelog) {
  stage('colgate release deploy: AppCenter') {
    try {
      unstash 'binaries-colgate-release'
      def buildType = 'release'
      def buildVariant = "colgate"
      appCenter apiToken: "$env.APP_CENTER_KEY",
        ownerName: 'CP-Mobile-Dev',
        appName: 'Colgate-Connect',
        pathToApp: "MainApp/app/build/outputs/apk/${buildVariant}/${buildType}/app-${buildVariant}-${buildType}.apk",
        distributionGroups: 'Core PM - CP-KQL, QA, Android Team',
        releaseNotes: "${changelog}"
      githubNotify context: 'Colgate release AppCenter', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Colgate release AppCenter', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

def careOSProdAppCenter(String changelog) {
  stage('careOS prod deploy: AppCenter') {
    try {
      unstash 'binaries-careos'
      def buildType = 'release'
      def buildVariant = "careOS"
      appCenter apiToken: "$env.APP_CENTER_KEY",
        ownerName: 'CP-Mobile-Dev',
        appName: 'CareOS-Colgate-Connect',
        pathToApp: "MainApp/app/build/outputs/apk/${buildVariant}/${buildType}/app-${buildVariant}-${buildType}.apk",
        distributionGroups: 'PM Core - CP-KQL',
        releaseNotes: "${changelog}"
      githubNotify context: 'CareOs Prod AppCenter', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'CareOs Prod AppCenter', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

def sdkDemoAppCenter(String changelog) {
  stage('sdk demo deploy: AppCenter') {
    try {
      unstash 'binaries-sdk-demo-app'
      def appNameSuffix = 'Production'
      def buildType = 'release'
      def buildVariant = "colgate"
      appCenter apiToken: "$env.APP_CENTER_KEY",
        ownerName: 'CP-Mobile-Dev',
        appName: 'SDK-Demo-App',
        pathToApp: "SdkDemoApp/app/build/outputs/apk/${buildVariant}${appNameSuffix}/${buildType}/app-${buildVariant}-${appNameSuffix.toLowerCase()}-${buildType}.apk",
        distributionGroups: 'collaborators, Core PM - KQL',
        releaseNotes: "${changelog}"
      githubNotify context: 'SDK Demo App AppCenter', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'SDK Demo App AppCenter', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

def btTesterAppCenter(String changelog) {
  stage('bt tester deploy: AppCenter') {
    try {
      unstash 'binaries-bt-tester'
      def buildType = 'release'
      appCenter apiToken: "$env.APP_CENTER_KEY",
        ownerName: 'CP-Mobile-Dev',
        appName: 'BT-Testing-App',
        pathToApp: "BtTesterApp/app/build/outputs/apk/${buildType}/app-${buildType}.apk",
        distributionGroups: 'collaborators',
        releaseNotes: "${changelog}"
      githubNotify context: 'BtTester App AppCenter', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'BtTester App AppCenter', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

def glimmerAppCenter(String changelog) {
  stage('glimmer app deploy: AppCenter') {
    try {
      unstash 'binaries-glimmer-app'
      def buildType = 'release'
      appCenter apiToken: "$env.APP_CENTER_KEY",
        ownerName: 'CP-Mobile-Dev',
        appName: 'Glimmer-Customization-App',
        pathToApp: "GlimmerCustomizationApp/app/build/outputs/apk/${buildType}/app-${buildType}.apk",
        distributionGroups: 'collaborators',
        releaseNotes: "${changelog}"
      githubNotify context: 'Glimmer App AppCenter', description: 'deployed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Glimmer App AppCenter', description: 'not deployed', status: 'ERROR'
      throw e
    }
  }
}

return this;
