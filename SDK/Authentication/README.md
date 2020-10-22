# What is it?
Modules contains classes needed for authentication process via SMS or WeChat

## Main components
* _AuthenticationFlowActivity_ class

## Integration
Use the provided Dagger _AuthenticationFlowModule_ class

## Custom translations
If you want to provide custom translations for AuthenticationFlowActivity then you have to use _TranslationProvider_ object
All needed keys can be found in class _AuthTranslationKey_ (or _AuthTranslationKeyKt_ for Java application)
These keys have to be added together with custom translation to Map<String, String> object.
And map object has to be added by _TranslationProvider.addLanguageSupport_ 
```
    TranslationsProvider provider = new TranslationsProvider();
    Map<String, String> authTranslations = new HashMap<>();
    authTranslations.put(AuthTranslationKeyKt.TOOLBAR_TITLE, "Custom title");
    authTranslations.put(AuthTranslationKeyKt.TOOLBAR_SUBMIT, "Custom submit");
    authTranslations.put(AuthTranslationKeyKt.CODE_DESCRIPTION, "Custom code description");
    authTranslations.put(AuthTranslationKeyKt.PHONE_NUMBER_HINT, "Custom phone number hint");
    authTranslations.put(AuthTranslationKeyKt.PHONE_NUMBER_INVALID, "Custom phone number invalid");
    authTranslations.put(AuthTranslationKeyKt.PHONE_NUMBER_SUBMIT, "Custom phone number submit");
    provider.addLanguageSupport(Locale.ENGLISH, authTranslations);

    Translations.init(context, translationsProvider);
```
All keys with related UI views can be found here:
https://confluence.kolibree.com/display/SOF/Authentication+module

Default English translations for these keys:
```
    TOOLBAR_TITLE => "Log in with SMS"
    TOOLBAR_SUBMIT => "OK"
    PHONE_NUMBER_HINT => "What's your phone number?"
    PHONE_NUMBER_SUBMIT => "Get authentication code"
    PHONE_NUMBER_INVALID => "Invalid phone number"
    CODE_DESCRIPTION => "Enter the code you've just received by SMS and press the OK button."
```

Default Chinese translations for these keys:
```
    TOOLBAR_TITLE => "通过短信登录"
    TOOLBAR_SUBMIT => "确定"
    PHONE_NUMBER_HINT => "你的电话号码是什么？"
    PHONE_NUMBER_SUBMIT => "获取验证码"
    PHONE_NUMBER_INVALID => "无效的电话号码"
    CODE_DESCRIPTION => "输入您刚收到的短信代码，然后按OK"
```

## How to release a new version?
* First of all, you have to configure your account on jfrog.io properly.
To do that you can follow steps from "Setup Artifactory account" section. See root README.md
* Then use _./gradlew clean assembleRelease artifactoryPublish_ to publish the library.
Note: you can execute these tasks directly in Android Studio in _Execute Gradle Task_ window
