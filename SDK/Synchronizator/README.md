# What is it?
Hides the synchronization complexity from the rest of the modules by dealing with it internally.

## Usage
Modules must register a Bundle and provide the needed dependencies 

Supported bundles for v0.3 are 
- SynchronizableItemBundle
- SynchronizableItemBundle
- SynchronizableCatalogBundle

```kotlin
SynchronizationBundles.register(SynchronizableItemBundle())
```

See https://confluence.kolibree.com/display/SOF/Synchronization for extended documentation
