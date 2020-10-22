# What is it?
This module provides the logic to update a Toothbrush's Firmware and Gru data

# Requirements
* _dagger_ and _dagger-android_

# Main components
* _OtaChecker_

# Integration

See root README.md for module usage

## Dagger
- Set up a @Component that at least
-- provides an _Application_, _ServiceProvider_ and _NetworkChecker_
-- Includes ToothbrushUpdateModule

## OtaChecker

Exposes _otaForConnectionObservable()_, which will emit [0-N] _OtaForConnection_, where N is the number of KLTBConnections.

From the javadoc

```text
 Checks if any KLTBConnection needs an Over The Air (OTA) update

 If it's a mandatory update and we can't fetch the data to update the toothbrush, the
 Observable will emit a OtaForConnection with type MANDATORY_NEEDS_INTERNET.

 Other possible types include MANDATORY and STANDARD.

 MANDATORY and MANDATORY_NEEDS_INTERNET need immediate action from the user. Since the
 toothbrush is in an unusable state, they are not safe to ignore.

 STANDARD updates will flag that a connection needs to be updated. This can be checked on any
 connection by subscribing to hasOtaObservable()

 If there's no mandatory update AND there's no internet, the observable won't emit an item for
 that KLTBConnection
```

An example of how to use it

```java
public class OtaCheckerViewModel extends ViewModel
    implements DefaultLifecycleObserver {
  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    if (checkOtaDisposable == null || checkOtaDisposable.isDisposed()) {
      checkOtaDisposable = otaChecker.otaForConnectionObservable()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
              this::onOtaForConnection,
              Throwable::printStackTrace
          );

      disposables.add(checkOtaDisposable);
    }
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    disposables.clear();
  }

  @VisibleForTesting
  void onOtaForConnection(OtaForConnection otaUpdateForConnection) {
    switch (otaUpdateForConnection.getOtaUpdateType()) {
      case STANDARD:
        //This KLTBConnection now will emit true for hasOTAObservable()
        break;
      case MANDATORY:
        onMandatoryUpdateNeeded();
        break;
      case MANDATORY_NEEDS_INTERNET:
        requestEnableInternet();
      default:
        //do nothing
    }
  }
  }
```
