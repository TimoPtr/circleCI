# What is it?
Library contains 3D jaws models and OpenGL renderers.
The memory management is done automatically, but you can preload the models to avoid latencies in your app (the models are heavy)
The library has been designed so you can use the 3D views in ViewPagers or multiple screens with the same memory usage

# Dependencies

This library depends on the following modules:

* Commons, for the mouth zones definitions
* Checkup, for data calculations

# Main components
* _MemoryManager_ class, responsible for loading 3D models
* _JawsView_ class, the view that is drawing the 3D models
* _JawsRenderer_ class, responsible for rendering the data on the view

# Integration
Use the provided Dagger _JawsModule_ class
See root README.md for module usage

# How to use

## Preload a model

You can preload a model at any time using the following method of the _MemoryManager_ class

```java
memoryManager.preloadFromAssets(Kolibree3DModel.CHECKUP)
        .subscribeOn(Schedulers.computation())
```

## Use the model

Make sure Dagger knows your view, the renderer is injected automatically.

Use the following method to provide the processed data (from the brushing session object) to the renderer

```java
jawsView.setData(processedData);
```

## Convert a model to the supported format

The provided 3D models usually come in DAE format (Collada), that is not supported by this module.
You can convert the DAE model using the free Blender software (available for Windows, Mac OS and Linux) that can be downloaded for free.
In Blender, clear the scene, import the DAE model the export it to Wavefront OBJ format.

During the export make sure that the following options (and only them) are checked:
* Apply modifier (if you rotated or scaled the object, if not leave unchecked)
* Include edges
* Write normals
* Triangulate faces (this module only supports triangle faces)
* Objects as OBJ objects
* Keep vertex order

# How to release a new version?
* First of all, you have to configure your account on jfrog.io properly.
To do that you can follow steps from "Setup Artifactory account" section. See root README.md 
* Then use _./gradlew clean assembleRelease artifactoryPublish_ to publish the library.
Note: you can execute these tasks directly in Android Studio in _Execute Gradle Task_ window

For the latest version, please visit : https://confluence.kolibree.com/display/SOF/Jaws+3D+module