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
-- Includes ToothbrushUpdateModule

## OtaUpdateActivity
When we want to start a firmware upgrade on a KLTBConnection, start OtaUpdateActivity

```java
Intent intent = OtaUpdateActivity.createIntent(context, mac, isMandatoryUpdate);
startActivityForResult(intent);
```

If there's no OTA for the KLTBConnection associated to the mac address, when the user clicks
_Upgrade_ an error message will tell the user that there's no update for the toothbrush

The Activity will finish with RESULT_OK or RESULT_CANCELED

# How to release a new version?
* First of all, you have to configure your account on jfrog.io properly.
To do that you can follow steps from "Setup Artifactory account" section. See root README.md
* Then use _./gradlew clean assembleRelease artifactoryPublish_ to publish the library.
Note: you can execute these tasks directly in Android Studio in _Execute Gradle Task_ window

# Providing Translations

If you whish to change any text displayed, you can provide your own TranslationsProvider

```
private void provideTranslations() {
    TranslationsProvider translationsProvider = new TranslationsProvider();

    Map<String, String> map = new HashMap<>();
    map.put(FIRMWARE_UPGRADE_WELCOME_KEY, "New english message");
    Map<String, String> chinaMap = new HashMap<>();
    chinaMap.put(FIRMWARE_UPGRADE_WELCOME_KEY, "Chinese message");
    translationsProvider.addLanguageSupport(Locale.US, map);
    translationsProvider.addLanguageSupport(Locale.CHINA, chinaMap);

    Translations.init(context, translationsProvider);
  }
```

## Supported keys
- FIRMWARE_UPGRADE_WELCOME_KEY: Welcome message when landing on the screen

default english: "Upgrading your toothbrush will take a few minutes, please don't turn off your toothbrush."
default chinese: "牙刷升级需要几分钟，请不要关闭牙刷"

- FIRMWARE_UPGRADE_INSTALLING_KEY: Displayed while installing

default: "Installing..."
default chinese: "正在安装"

- FIRMWARE_UPGRADE_REBOOTING_KEY: Displayed while rebooting

default: "Rebooting..."
default chinese: "正在重启"

- FIRMWARE_UPGRADE_ERROR_KEY: Displayed when there's an unknown error

default: "Something went wrong. Please try again later."
default chinese: "出错了，请稍候再试"

- FIRMWARE_UPGRADE_ERROR_DIALOG_KEY: Displayed in a popup dialogwhen there's an error

default: "Firmware upgrade failed:
         <placeholder>"
default chinese: "固件更新失败：
                 <placeholder>"

- FIRMWARE_UPGRADE_CANCEL_DIALOG_MESSAGE_KEY: confirm upgrade exit

default: "Are you sure you want to cancel the update?"
default chinese: "您确定要取消更新吗？"

- POPUP_TOOTHBRUSH_UNAVAILABLE_MESSAGE_KEY: message when we can't find the toothbrush

default: "Make sure your toothbrush is on and bring it closer to your device, it will connect automatically."
default chinese: "确保您的牙刷已打开，并且靠近设备，连接将自动建立"

- FIRMWARE_UPGRADE_OTA_NOT_AVAILABLE_KEY: no update available for the toothbrush

default: "No update available"
default chinese: "还没有更新"

- FIRMWARE_UPGRADE_SUCCESS_KEY: Update succeeded

default: "Your toothbrush is up to date."
default chinese: "您的牙刷已更新"

- FIRMWARE_UPGRADE_CANCEL_KEY: Cancel firmware upgrade. Only shown in non-mandatory updates.

default: "Cancel"
default chinese: "取消"

- FIRMWARE_UPGRADE_KEY: Start firmware upgrade

default: "Upgrade"
default chinese: "更新"

