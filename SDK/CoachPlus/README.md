# What is it?
This library contains the Coach+ brushing activity
The memory management is done automatically, but you can preload the models to avoid latencies in your app (the models are heavy)

# Main components
* _JawsModule_ class, if you want to preload the 3D model
* _MemoryManager_ class, responsible for loading 3D models, only if you want to preload the 3D model
* _CoachPlusActivity_ class, the activity you will have to start
* _CoachPlusActivityModule_ class, that will bind the dagger dependencies. See the Integration section for more details.

# Integration

In order to use the CoachPlus brushing activity, you will need to add the following line to your application's Dagger component:

```java
@Component(
    modules = {CoachPlusModule.class, ...})
```

Now that the Dagger graph is bound, you may want to preload the 3D models of the 3D jaws, in order to make the CoachPlusActivity load faster.
You can do so by using the _MemoryManager_ class, that is provided by the _JawsModule_ class.

```java
memoryManager.preloadFromAssets(Kolibree3DModel.COACH)
        .subscribeOn(Schedulers.computation())
```

# How to use

Once the previous section's steps are done, you can start the CoachPlusActivity creating an Intent using the following lines:

```java
CoachPlusActivity.createIntent(context, toothbrushMacAddress, optionalColorSet);
```

* context is a valid Android context
* toothbrushMacAddress is the MAC address of the toothbrush you want to use for this session
* colorSet, you can provide a color set that will be used by the _CoachPlusActivity_ class. If you pass null to this parameter, the default color set will be used.

The activity will return RESULT_OK if a brushing session has been recorded, RESULT_CANCELED otherwise

You can create a color set by using the default constructor.
The 4 arguments are explained below:

* backgroundColor: the color of the activity's background
* titleColor: the color of the "Coach+" labeled title
* neglectedColor: the neglected teeth color (not brushed color)
* cleanColor: the clean teeth color (fully brushed color)

# How to release a new version?
* First of all, you have to configure your account on jfrog.io properly.
To do that you can follow steps from "Setup Artifactory account" section. See root README.md 
* Then use _./gradlew clean assembleRelease artifactoryPublish_ to publish the library.
Note: you can execute these tasks directly in Android Studio in _Execute Gradle Task_ window

For the latest version, please visit : https://confluence.kolibree.com/display/SOF/Jaws+3D+module
