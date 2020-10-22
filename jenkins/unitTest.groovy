#!usr/bin/env groovy


/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */


def sdkUnitTest(boolean publishPactTest) {
  stage('test: sdk unit tests') {
    try {
      sh "$SDK_GRADLE testDebugUnitTestWithCoverage"
      if (publishPactTest) {
        sh "$SDK_GRADLE pactPublish"
      }
      githubNotify context: 'Sdk Unit Test', description: 'All tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Sdk Unit Test', description: 'Unit test failed', status: 'ERROR'
      throw e
    } finally {
      junit 'SDK/**/build/test-results/**/*.xml'
    }
  }
}

def mainAppUnitTest() {
  stage('test: app unit tests') {
    try {
      sh "$MAIN_APP_GRADLE :app:testHumDebugUnitTestWithCoverage :app:testColgateDebugUnitTestWithCoverage"
      githubNotify context: 'App Unit Test', description: 'All tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'App Unit Test', description: 'Unit test failed', status: 'ERROR'
      throw e
    } finally {
      junit 'MainApp/**/build/test-results/**/*.xml'
    }
  }
}

def glimmerUnitTest() {
  stage('test: glimmer unit tests') {
    try {
      sh "$GLIMMER_GRADLE testDebugUnitTest"
      githubNotify context: 'Glimmer App Unit Test', description: 'All tests passed', status: 'SUCCESS'
    } catch (e) {
      githubNotify context: 'Glimmer App Unit Test', description: 'Unit test failed', status: 'ERROR'
      throw e
    } finally {
      junit 'GlimmerCustomizationApp/**/build/test-results/**/*.xml'
    }
  }
}


//            stage('code coverage') {
//              when {
//                allOf {
//                  expression { return false }
//                  //FIXME bring it back once delta thresholds issue is fixed
//                  branch 'master'
//                  expression { return buildCause.isTimer() }
//                }
//              }
//              steps {
//                sh 'echo Starting JaCoCo report generation...'
//              }
//              post {
//                success {
//                  githubNotify context: 'Code Coverage', description: 'Code Coverage passed', status: 'SUCCESS'
//                }
//                failure {
//                  githubNotify context: 'Code Coverage', description: 'Code Coverage failed', status: 'ERROR'
//                }
//                always {
//                  jacoco buildOverBuild: true,               \
//                                    changeBuildStatus: true,               \
//                                    execPattern: 'MainApp/app/build/jacoco/*.exec, SDK/**/build/jacoco/*.exec',               \
//                                    classPattern: 'MainApp/app/build/**/classes, SDK/**/build/**/classes',               \
//                                    sourceInclusionPattern: '**/*.java, **/*.kt',               \
//                                    sourceExclusionPattern: 'generated/**/*',               \
//                                    sourcePattern: 'MainApp/app/src/**/java, MainApp/app/src/**/kotlin, SDK/**/src/**/java, SDK/**/src/**/kotlin',               \
//                                    exclusionPattern: '**/R.class, **/R$*.class, **/BuildConfig.*, **/Manifest*.*, **/*Test*.*, android/**/*.*, **/*Dagger*.*, **/Dagger*Component.class, **/Dagger*Component$Builder.class, **/*MembersInjector*.*, **/*_MembersInjector.class, **/*_Factory.*, **/*Module_*Factory.class, **/*_Provide*Factory*.*, **/*_MembersInjector.class, **/Dagger*Component*.class, **/Dagger*Subcomponent*.class, **/*Subcomponent$Builder.class, **/*Module_*Factory.class,',              \
//                                    deltaInstructionCoverage: '-10', deltaBranchCoverage: '-10', deltaComplexityCoverage: '-10', deltaLineCoverage: '-10', deltaMethodCoverage: '-10', deltaClassCoverage: '-10'
//                }
//              }
//            }

return this;
