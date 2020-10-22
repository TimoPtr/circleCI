# android-sdk-demo-app

## Configuring Artifactory Authentication

To build the SDK Demo app, it may be necessary to append Artifcatory authentication
to the `gradle.properties` files.  Provide valid values for the following properties
listed below, inside `project_root/gradle.properties`.

```java
kolibree_artifactory_password=
kolibree_artifactory_url=
kolibree_artifactory_username=
```
## Updating the version of the demo app

To update the version of the demo app, update the `module_version` in the `gradle.properties`.

## Verifications

Before sending new version of SDK, please perform smoke tests on both environments: Kolibree Staging and Chinese 
production.

Chinese production backend allows sign-ins & sign-ups from Chinese phone numbers only. Our test Chinese SIM card is
located in the Paris office. Its number is `18816933792`. If you need to login to this account, ask someone from Paris
office to send you the 6-digit one-time password from SMS.

## WeChat Registration/Login

In order to test WeChat integration, you need to sign it with Kolibree's certificate. To do that, you need

1. Kolibree's keystore, alias and password

2. Invoke gradle with the keystore parameters `storeFilePath`, `keyPass` and `aliasPass`

```
./gradlew assembleColgateProductionDebug -PstoreFilePath=$KL_KEYSTORE_PATH -PkeyPass=$KL_STORE_PASSWORD -PaliasPass=$KL_STORE_PASSWORD
```

You need to uninstall any previous debug application

1. The package name must be cn.colgate.colgateconnect
2. WeChatSDK will call the activity named WXEntryActivity
3. Using WXApiManager.requestWeChatCode to get WeChat code
4. AccountManager.attemptLoginWithWechat help to detect whether the account in use or not. WeChatAccountNotRecognizedException carry the token if account not in use. 








