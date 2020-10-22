## What is it?
Library contains common profile values and set of mouth zones

## Main components
* commons profile values (Gender, Handedness)
* set of mouth zones

## Integration

See root README.md for module usage

## How to release a new version?
* First of all, you have to configure your account on jfrog.io properly.
To do that you can follow steps from "Setup Artifactory account" section. See root README.md 
* Then use _./gradlew clean assembleRelease artifactoryPublish_ to publish the library.
Note: you can execute these tasks directly in Android Studio in _Execute Gradle Task_ window
