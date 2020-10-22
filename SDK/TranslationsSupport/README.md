# What is it?
This is module is to be used by other modules, not by final clients.

It Allows a module consumer to provide translations at initialization time

# Requirements
none

# Usage

## In MVI context

You don't have to do anything BaseMVI already sets everything you need, just expose the key that
you want to be changeable.

## Android component

Given a standard Android component (Activity, Fragment, Service, etc.) that uses a string resource,
or has views that use string resources
 
 ```java
class KolibreeService {
 
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
      final NotificationCompat.Builder notificationBuilder = new Builder(this)
          .setContentTitle(getResources().getString(R.string.running_in_background));
  }
}
 ```
 
In order to use the replaced version, override onAttachBaseContext

```java
class KolibreeService {
 
  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(Translations.wrapContext(base));
  }
}
```

## Other classes

Other classes (such as ViewModel) should declare a dependency on a TranslationContext

```java
class OtaUpdateViewModel extends BaseKolibreeServiceViewModel {

  private final Context context;

  OtaUpdateViewModel(TranslationContext translationContext) {
    super(serviceProvider);

    this.context = translationContext; 
  }   
    
  String getString(@StringRes int stringRes) {
    return context.getString(stringRes);
  }
}
```

In the specifig case of a ViewModel, it can be provided in the Factory

```java
static class Factory implements ViewModelProvider.Factory {
   
    private final Application application;

    @Inject
    Factory(Application application) {
      this.application = application;
    }

    @NonNull
    @Override
    public OtaUpdateViewModel create(@NonNull Class ignored) {
      return new OtaUpdateViewModel(new TranslationContext(application));
    }
  }
```

## Supporting translations
 
At the very least, the key should be in values/strings.xml. It'll be the fallback message if a 
translation isn't given for the phone's current language
 ```xml
<resources>
  <string name="running_in_background">Original message</string>
</resources>
```

If the module consumer wanted to replace R.string.running_in_background, at initialization time, 
invoke Translations.init(context, translationsProvider). 

Continuing with the example of KolibreeService

```java
class MyApp extends Application {
  void initTranslations(){
    TranslationsProvider translationsProvider = new TranslationsProvider();

    Map<String, String> map = new HashMap<>();
    map.put(RUNNING_IN_BACKGROUND_KEY, "English message");
    Map<String, String> chinaMap = new HashMap<>();
    chinaMap.put(RUNNING_IN_BACKGROUND_KEY, "chinese message");
    translationsProvider.addLanguageSupport(Locale.US, map); 
    
    Translations.init(this, translationsProvider);
  }
}
```

The module should expose the keys as constants in order to avoid spelling errors. See ToothbrushSDK/src/main/java/com/kolibree/android/translationssupport/ToothbrushSDKTranslationKeys.kt

Multiple modules can invoke Translations.init without harm. If there are duplicated keys, the latter will prevail.

From the client's point of view, it might be easier if we hide the translations initialization and expose a module initialization, such as

```java
public final class KolibreeAndroidSDK {

  @NonNull
  public static SdkComponent init(@NonNull Context context) {
    return init(context, null);
  }

  @NonNull
  public static SdkComponent init(@NonNull Context context, @Nullable TranslationsProvider translationsProvider) {
    Translations.init(this, translationsProvider);  
    
    //other stuff
  }
}
```

Internally, this module uses [Philology](https://github.com/JcMinarro/Philology) and [ViewPump](https://github.com/InflationX/ViewPump)
