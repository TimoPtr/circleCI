Kolibree Android App
=======

## Requirements

Make sure you have the latest android build tools available on SDK Manager

Please read https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755451/Android+onboarding

## Project structure

```
|--- MainApp
|--- SDK
|------- WebServicesSDK
|------- ToothbrushSDK
|------- ...
```

The Kolibree Android app uses the modules to compose the application

## Configure Android Studio

Check https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755037/Android+Studio+Setup

## Installing dependencies

Some of our internal libraries are published to an internal jfrog instance

Please check instructions on how to set it up here https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755451/Android+onboarding#Androidonboarding-KolibreeAndroidApp

### Build Kolibree Android application

#### From command line

##### Debug version 

```bash
./gradlew clean assemble<Flavor>Debug
```

This'll create an .apk file at _$ROOT/app/build/outputs/apk/<flavor>/debug/_

#### Release version

See https://confluence.kolibree.com/display/SOF/Releasing+Versions for details

### Test Kolibree Android application

Check Jenkinsfile for the latest commands to run tests

### Running modules tests

Unfortunately, there's an Android Studio bug that prevents all modules tests from running automatically. Thus, whenever we want to run tests
we must either do it from command line or create a Gradle task configuration to run the tests. For example

```bash
cd AndroidModulesSDK
./gradlew :rewards:testColgateDebugUnitTest
```

will run reward's tests

Within Android Studio, go to Edit Configurations, add a Gradle configuration, select rewards module and write testColgateDebugUnitTest

To run a specific test, append

```bash
./gradlew :rewards:testColgateDebugUnitTest --tests=com.kolibree.android.my.path.to.test.MyTest
```

For functional tests

```bash
./gradlew :rewards:connectedColgateDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kolibree.android.my.path.to.test.MyTest
```

#### Unit tests

You can run them from Android Studio (right click, run test) or running the following command

Windows User : If you encounter the error `Error running '***': Command line is too long. Shorten command line for *** or also for Android JUnit default configuration.`, you should go to the `Edit Configurations` menu and change the `Shorten command line` option to `classpath file`

```bash
./gradlew clean testDebugUnitTest testKolibreeDebugUnitTest testColgateDebugUnitTest

```

A report is generated in _$ROOT/app/build/reports/tests/testDebugUnitTest/_

There are three folders containing unit tests

```
src/
    test/
    testKolibree/
    testColgate/
```
As you can guess, there's a common suite of tests, and then specific suites for each flavor.

#### Functional tests

Functional tests should be run on emulators with animations disabled. See https://developer.android.com/training/testing/espresso/setup.html#set-up-environment

You can run them from Android Studio (right click, run test) or using scripts in `test_script` folder. We recommend 
to use those, as Jenkins is executing Espresso using the exact same configuration, through Spoon library.

First, you need to create AVD that will have the same setup as the one Jenkins uses. Simply run from console:
```bash
cd test_scripts && ./create_new_spoon_emulator.sh NAME_OF_THE_AVD
```

After that, you can execute Espresso tests using Spoon wrapper, by calling:
```bash
cd test_scripts && ./execute_spoon.sh NAME_OF_THE_AVD [path.to.specific.test] [specificMethod]
```

For example
```bash
cd test_scripts && ./create_new_spoon_emulator.sh Jenkins_Nexus_5_API_26
./execute_spoon.sh Jenkins_Nexus_5_API_26 com.kolibree.android.app.ui.dashboard.DashboardEspressoTest testFrequency_accountCreatedToday_1Brushing_shows1
```

If you don't specify a test, the whole test suite is executed
If you don't specify a method, the whole class is executed

Script will create emulator, execute tests and kill the emulator at the end.

Spoon report (with tests results, screen shots etc.) is generated in _$ROOT/app/build/spoon-output/[NAME OF CHOSEN FLAVOUR]_

There are three folders containing unit tests

```
src/
    androidTest/
    androidTestKolibree/
    androidTestColgate/
```
As you can guess, there's a common suite of tests, and then specific suites for each flavor.

Further research must be done regarding functional tests, since we first need to launch an emulator.

Ideally, we should launch multiple emulators and test on different android versions

### Wording

While developing, we need to add the terms manually to POEditor. Please don't use the gradle task exposed from the plugin, we've often run into conflicts and duplicate keys.

See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755140/Android+Best+Practices#AndroidBestPractices-Stringsandtranslations

You can then use gradle to pull the translations. Make sure the current directory is the application's root, then run:

```bash
./gradlew poeditorPull
```

This is not a comfortable workflow, but it's the one we use.
Potential issue for Windows user as Kornel pointed out -> we have to go to 'Users/<your account name>/.gradle/deamon', there will be SDK directory, localization will be stored there

### API credentials

The credentials of the Kolibree's API platform can be found in the _remote_services.xml_ files in the various source roots.
The API secrets are symmetrically encrypted to make harder the reverse engineering.
The steps described below are processed by the _KolibreeGuard_ utility class:

* A xor encrypted byte array of 128 bits (AES key, highest value allowed by the laws) is hardcoded in an Proguard-obfuscated class
* A special non obfuscated class name has been chosen to be the xor password of this AES key.
* The key to decrypt the API secrets is xor-decrypted at runtime

There is no easy way to get the encrypted version of a new API secret. I tried to write a gradle task to do so but it was too verbose and introduced temporary files so I gave up.
The easiest solution would be to use the _encrypt()_ method of the _KolibreeGuard_ class at the beginning of the application and put a breakpoint on it.
