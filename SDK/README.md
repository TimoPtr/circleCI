# Goal
This folder holds all Android modules

There are different usages to our modules
## Internal
Most of them are used internally by applications living in the same monorepo (MainApp, SdkDemo, BtTester)

## External
We provide libraries for 3rd party organizations.

Modules are published by default unless their build.gradle skips the deployment

```publisher.skipDeployment = true```

We need to document breaking changes and enhancements to the SDK in CHANGELOG.md

## JV
Chinese Joint Venture that develop the China version of Colgate Connect app. We try to hide as much
complexity from them as possible

## Magik
Kolibree project that use our SDK for backend calls

# Modules

## Flavors

While modules are flavorless, they also hold many of the screens and assets in the application. This
is a typical organization of modules for cases where we had to support legacy V1 UI and modern UI

* BaseUI: base code shared by both V1 and modern UI
* BaseUIHUM: holds modern UI. Initially it was only for Hum, hence the name.
* BaseUIV1: legacy UI components

For now, we need to keep V1 modules. SDK still uses it. In the short term, the plan is that SDK
will also consume modern modules and we will be able to remove legacy modules

## List

This is not a exhaustive list

* `WebServicesSDK`: This module links the application to the Kolibree API backend. Previously known as [Android-SDK](https://github.com/kolibree-git/Android-SDK)

Historically, it handled local and remote databases synchronization and all API-related actions. As of recently, we've
started moving backend requests and persistence to feature modules.

* `Network`: Configures and provides Network related classes, such as a Retrofit instance ready to be used to talk to
our backend.

* `Account-internal`: Local persistence of account and profiles 

* `ToothbrushSDK`: This module embeds all Kolibree toothbrush Bluetooth connections, commands and hardware-related functionality. Previously known as [Android-SDK](https://github.com/kolibree-git/Android-SDK)

* `CoachPlus`, `Pirate`, `Smart Brushing Analyzer` and `Game`: modules related to games. Game modules holds shared code for all games

* `Commons` and `Commons-android` Common models classes for modules. The first is a java only module, while the latter provides
android related code

* `BaseUI` and `HomeUI`: Common to modules that implement UI, or Home features

* `Heuristic`: related to algorithms used to detect which zone user is brushing

* `Jaws`: renders 3D Jaw on screen. [Previous repository](https://gitlab.com/kolibree/android/jaws)

* `Offline Brushings`: Users can brush without being connected to our app. This modules contains the business logic 
responsible for fetching stored brushings in the toothbrush. The bluetooth interaction code lives in ToothbrushSDK.

* `Pairing`: helper module to pair to a Toothbrush. It exposes operations such as Scan, pair or unpair, amongst others.

* `Processed Brushings`: Logic to parse and produce processed_data, which is the representation of the zones the user 
has brushed along with the data needed to infer the quality of the brushing (time spent, speed, angles, etc.). In the 
mid term, we will use a shared C++ library shared with iOS. For now, we use our own code. Code inherited from [kolibree-android-stats](https://gitlab.com/kolibree/android/kolibree-android-stats)

* `Question of the day`: Provides question of the day functionality

* `Rewards`: Holds screens and business logic for Rewards feature.

* `Static Resources`: Its main goal is to provide translations needed by the application and the different modules. 
These translations come from POEditor, and we pull them through a gradle task.

* `Stats`: Business logic to calculate Daily and Weekly calculations for brushings for the active profile

* `Synchronizator`: Module to perform the synchronization on behalf of other modules, which can register. It's a work
in progress, for now only used by rewards

* `TestingXXX`: Base modules for testing

* `Toothbrush Update UI`: Screens related to Over The Air (OTA) updates

* `Tracker`: Analytics tracking module. For now, a wrapper around Google Analytics.

* `Translations Support`: Provides a mechanism for SDK consumers to change some string translations on the fly, without
needing to modify POEditor.

## Usage

To create a new module, please follow the naming convention of the rest of the modules

`/SDK/settings.gradle` lists all modules. You'll need to edit that file manually after creating a module

From the application, just declare the dependency to the module

```
dependencies {
    implementation project(path: ':stats')
    implementation project(path: ':commons')
    implementation project(path: ':toothbrush-sdk')
    implementation project(path: ':web-services-sdk')
    
    [.. rest of dependencies ..]
```

# Version naming
Currently, we are using pattern X.Y.Z
* X - major changes
* Y - minor changed
* Z - bugfixes

# Dependencies

All shared dependencies live in _dependencies.gradle_, as well as _minSdk, targetSdk_ and other shared values

Make sure you check _dependencies.gradle_ and respect the naming if you want your dependency versions to be used, otherwise there will be inconsistencies

The idea was copied from https://github.com/artem-zinnatullin/qualitymatters

# Publishing artifacts

Artifacts are published through a release branch in Jenkins. Check https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2754750/Android+Modules+SDK+release+process
