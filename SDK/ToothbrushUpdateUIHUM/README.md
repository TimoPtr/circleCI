# What is it?
This module provides the screens to update a Toothbrush's Firmware and Gru data

# Requirements
* _dagger_ and _dagger-android_

# Main components
* _OtaUpdateActivity_

# Integration

See root README.md for module usage

## Dagger
- Set up a @Component that at least 
-- provides an _Application_, _ServiceProvider_ and _NetworkChecker_
-- Includes OtaUpdateBindingModule

## OtaUpdateActivity
When we want to start a firmware upgrade on a KLTBConnection, start OtaUpdateActivity with

```java
startOtaUpdateScreen(context, true, "00:00:00:00:00", ToothbrushModel.ARA)
```

# Providing Translations

If you wish to change any text displayed, you can provide your own TranslationsProvider

```
private void provideTranslations() {
    TranslationsProvider translationsProvider = new TranslationsProvider();

    Map<String, String> map = new HashMap<>();
    map.put(OtaUpdateTranslationKey.START_OTA_TITLE, "Title");
    Map<String, String> chinaMap = new HashMap<>();
    chinaMap.put(OtaUpdateTranslationKey.START_OTA_TITLE, "Chinese title");
    translationsProvider.addLanguageSupport(Locale.US, map);
    translationsProvider.addLanguageSupport(Locale.CHINA, chinaMap);

    Translations.init(context, translationsProvider);
  }
```

## Supported keys


Keys are in `com.kolibree.android.app.ui.otaOtaUpdateTranslationKey`

- START_OTA_TITLE: Title of the Start OTA screen it needs to contain START_OTA_TITLE_HIGHLIGHT

default english: "Start the update."
default chinese: None

- START_OTA_TITLE_HIGHLIGHT: Highlight screen of the title it should be included in START_OTA_TITLE

default: "."
default chinese: None

- START_OTA_CONTENT: Content of the Start OTA screen

default: "To prevent toothbrush downtime, during the update process:"
default chinese: None

- START_OTA_SUB_CONTENT1: Sub content of the Start OTA screen

default: "1. Please keep the firmware upgrade interface open and do not exist"
default chinese: None

- START_OTA_SUB_CONTENT2: Sub content of the Start OTA screen

default: "2. Make sure the mobile phone and the toothbrush are within 3 meters"
default chinese: None

- START_OTA_SUB_CONTENT3: Sub content of the Start OTA screen, this sub content is only display when the brush is rechargeable

default: "3. Please put your toothbrush on the charger to continue"
default chinese: None

- START_OTA_UPGRADE: Text on the bottom button to go in in progress OTA

default: "Upgrade"
default chinese: None

- START_OTA_PUT_BRUSH_ON_CHARGER: On E2 with an old firmware we need to be sure the brush is plugged 

default: "Please put your toothbrush on the charger to continue"
default chinese: None

- START_OTA_CANCEL: Text on the cancel button when it's not a mandatory update

default: "Cancel"
default chinese: None

- START_OTA_CANCEL_MANDATORY: Text on the cancel button when it's a mandatory update

default: "Close the app and update later"
default chinese: None

- IN_PROGRESS_OTA_TITLE: Title of the In Progress screen

default: "Update in progress"
default chinese: None

- IN_PROGRESS_OTA_CONTENT: Content of the In Progress screen

default: "Hang tight! You’ll be able to close this window when the update is complete."
default chinese: None

- IN_PROGRESS_OTA_PROGRESS: Progress with placeholder of the In Progress screen

default: "updating firmware: %d%%"
default chinese: None

- OTA_DONE_TITLE: Title of the Done screen

default: "update done!"
default chinese: None

- OTA_DONE_CONTENT: Content of the Done screen

default: "You’re good to go now! After closing this window you can start using your  toothbrush."
default chinese: None

- OTA_DONE: Text on the done button for failure and success

default: "Done"
default chinese: None

- OTA_FAILURE_TITLE: Title of the Failure screen

default: "update failed"
default chinese: None

- OTA_FAILURE_CONTENT: Content of the Failure screen

default: "The toothbrush cannot be updated at the moment."
default chinese: None

- OTA_BLOCKER_NO_ACTIVE_CONNECTION: Error message in start screen when connection is not active

default: "Verify the connection with your brush"
default chinese: None

- OTA_BLOCKER_NOT_CHARGING: Error message in start screen when E2 brush with old FW is not charging

default: "Please put your toothbrush on the charger to continue"
default chinese: None

- OTA_BLOCKER_NOT_ENOUGH_BATTERY: Error message in start screen when a brush does not have enough battery to proceed

default: "Please put your toothbrush on the charger to continue"
default chinese: None

- OTA_BLOCKER_NO_INTERNET: Error message in start screen when there is no internet connection or backend is not reachable

default: "The internet connection appears to be offline."
default chinese: None

