# What is it?
This module holds
- The styles to be shared amongst UI modules
- The strings to be used by the kolibree app

We only release the colgateRelease variant of the module (see root build.gradle)

# Dependencies

- Support libraries

## Usage

Add the module as a dependency
```groovy
    implementation project(path: ':static-resources')
```

### Pulling new strings

In the $ROOT folder of the application using this module, invoke 

```
./gradlew poeditorPullcolgate poeditorPullkolibree
```

*WARNING* If you run those tasks in the wrong folder, it'll create a _StaticResources_ folder in $ROOT

For example, in my kolibree app, $ROOT is _/kolibree-android-app/_ and StaticResources is in
_/kolibree-android-app/AndroidModulesSDK/StaticResources_
 
In order to pull the strings, I need to make sure that I'm in _/kolibree-android-app/_