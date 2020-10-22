# What is it?
Links the application to the Kolibree API backend, and is responsible for Brushings persistence 

Historically, it handled local and remote databases synchronization and all API-related actions. As of recently, we've
started moving backend requests and persistence to feature modules.

# Requirements
_dagger_ and _dagger-android_

# Main components
* _IKolibreeConnector_ class

# Integration

See root README.md for module usage

## Dagger
- Set up a @Component that at least 
-- provides a _Context_ 
-- includes _ApiSDKModule.class_

- Project needs to use a custom Application that implements _HasAndroidInjector_. See https://google.github.io/dagger/android.html for details

# How to release a new version?
* First of all, you have to configure your account on jfrog.io properly.
To do that you can follow steps from "Setup Artifactory account" section. See root README.md 
* Then use _./gradlew clean assembleRelease artifactoryPublish_ to publish the library.
Note: you can execute these tasks directly in Android Studio in _Execute Gradle Task_ window
