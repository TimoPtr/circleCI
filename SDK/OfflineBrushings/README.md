# What is it?
This module has two goals
1. Retrieves Offline Brushings from connected toothbrush
2. Manages Orphan brushings, which are Brushings for which we don't know the owner. This can happen for shared toothbrushes on a multiprofile account

# Requirements
* _dagger_ and _dagger-android_
* A _LifecycleRegistry_. _AppCompatActivity_ and _support Fragment_ implement LifecycleOwner, which is enough

# Main components
* _OfflineBrushingsRetrieverViewModel_
* _OfflineBrushingsRetrieverViewState_
* _OrphanBrushingRepository_
* _OrphanBrushingMapper_

# Integration

See root README.md for module usage

## Dagger
- Set up a @Component that at least 
-- provides a _Context_ 
-- includes _OfflineBrushingsModule.class_

## OfflineBrushingsRetrieverViewModel and OfflineBrushingsRetrieverViewState
1. _OfflineBrushingsRetrieverViewModel.Factory_ needs to be injected.
2. Instantiate an _OfflineBrushingsRetrieverViewModel_
3. Add the instance as a LifecycleObserver
4. Subscribe to _OfflineBrushingsRetrieverViewState_
5. Update the UI when a new _OfflineBrushingsRetrieverViewState_ is emitted

This snippet shows a complete usage of OfflineBrushingsRetrieverViewModel

```java
class MainActivity extends AppCompatActivity{

  //1. Inject a Factory
  @Inject
  OfflineBrushingsRetrieverViewModel.Factory offlineRetrieverViewModelFactory;

  private void initOfflineBrushingsRetrieverViewModel() {
    //2. Instantiate the ViewModel
    OfflineBrushingsRetrieverViewModel offlineBrushingsRetrieverViewModel = ViewModelProviders
        .of(this, offlineRetrieverViewModelFactory)
        .get(OfflineBrushingsRetrieverViewModel.class);

    //3. Add as a LifecycleObserver
    getLifecycle().addObserver(offlineBrushingsRetrieverViewModel);

    //4. Subscribe to OfflineBrushingsRetrieverViewState
    disposables.add(offlineBrushingsRetrieverViewModel.viewStateObservable()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            this::renderOfflineBrushingsRetrieverViewState,
            Throwable::printStackTrace
        ));
  }

  //5. Update the UI
  private void renderOfflineBrushingsRetrieverViewState(
      OfflineBrushingsRetrieverViewState offlineRetrieverViewState) {
    if (offlineRetrieverViewState.haveRecordsBeenRetrievedForCurrentProfile()) {
      showOfflineRecordsSynchedDialog(offlineRetrieverViewState.getRecordsRetrieved());
    }
  }
}
```

In order to inject the Factory, the easiest way is to use _dagger-android_. Refer to https://confluence.kolibree.com/display/SOF/Android+SDK+%3A+How+to+integrate+it+within+your+app for instructions

## OrphanBrushingRepository
_OrphanBrushingRepository_ is reactive CRUD interface 
- Notifies subscribers whenever an _OrphanBrushing_ is added or removed
- Exposes methods to manipulate the data

## OrphanBrushingMapper
Once the user has selected an action on the _OrphanBrushing_, _OrphanBrushingMapper_ should be used to assign a brushing to a profile or to delete it. This will perform the remote addition/deletion of the 
associated brushing 

# How to release a new version?
* First of all, you have to configure your account on jfrog.io properly.
To do that you can follow steps from "Setup Artifactory account" section. See root README.md 
* Then use _./gradlew clean assembleRelease artifactoryPublish_ to publish the library.
Note: you can execute these tasks directly in Android Studio in _Execute Gradle Task_ window
