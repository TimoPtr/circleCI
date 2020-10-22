# What is it?
Embeds all Kolibree toothbrush Bluetooth connections, commands and hardware-related functionnality.

# Requirements
_dagger_

# Usage

See root README.md for module usage

## Dagger
It's important that you init the SDK as early in the application initialization as possible.

```java
class BaseKolibreeApplication extends Application {
  public static AppComponent appComponent;
  
  @Override
  public void onCreate() {
    super.onCreate();
    
    initDagger();
  }
  
  private void initDagger() {
    SdkComponent sdkComponent = KolibreeAndroidSDK.init(this);

    appComponent = DaggerAppComponent.builder()
        .context(this)
        .appModule(new AppModule(Clock::systemUTC))
        .sdkComponent(sdkComponent)
        .build();

    appComponent.inject(this);
  }
}
```

If you need a dependency from the module, you need to set up a dagger @Component that depends on SdkComponent

```
@Component(
    dependencies = {SdkComponent.class},
    modules = {[Your modules]}
)
public interface AppComponent {

  Context context();
  
  IBluetoothUtils bluetoothUtils();
  
  void inject(BaseKolibreeApplication app);
  
  @Component.Builder
  interface Builder {
    
    @BindsInstance
    Builder context(Context context);
    
    Builder sdkComponent(SdkComponent sdkComponent);
    
    AppComponent build();
  }
}
```


## Scanning for toothbrushes

### Get a ToothbrushScanner
There two paths available to scan for toothbrushes

### Classic way
Get a ToothbrushScanner through *ToothbrushScannerImpl*

```java
ToothbrushScannerImpl.getXXXScanner
```

### Dagger way
You can inject a ToothbrushScannerFactory to your instance

```java
@Inject
ToothbrushScannerFactory toothbrushScannerFactory;
```

As long as your component has a dependency to *SdkComponent* as described below, you'll automatically receive the dependency

## Scan for toothbrushes
Once you have a ToothbrushScanner

1. Start scanning by invoking *startScan* 
2. Register yourself as a listener through *addToothbrushScanCallback(ToothbrushScanCallback callback)*
3. Handle found toothbrushes in your callback *onToothbrushFound(ToothbrushScanner scanner, ToothbrushScanResult result)*
4. Stop scanning with *stopScan*. It's important to stop scanning before you attempt to establish a connection.

*KolibreeService* can create a *KLTBConnection* from a *ToothbrushScanResult* 

## Start KolibreeService
There are two ways to start the *KolibreeService*

1. If your activity needs to bind to the service, extend KolibreeServiceActivity, provided by BaseUI module
2. If your class needs access to the service, you can @Inject a ServiceProvider instance and get a KolibreeService instance or be notified when the server is up and running

*KolibreeService* will promote itself to foreground service, which means two things
1. It's less likely to be killed by Android OS
2. The user will see a notification icon for as long as the service is alive. 

The service will be alive as long as there's a bounded object to it, so it's important that for each *bindService* we invoke *unbindService* when we no longer need the *KolibreeService*. Otherwise, we risk staying in the user's toolbar forever, which is not a good user experience.


## Establishing a connection to a toothbrush
Once connected to a KolibreeService, you can use its public methods to create a connection to a Toothbrush

```java
KLTBConnection connection = service.createConnection(toothbrushScanResult, false);
```

For an already known connection, the *KLTBConnectionProvider* class allows to return an active KLTBConnection within the timeout window.

```java
connectionProvider.existingActiveConnection(toothbrushMac);
```

You are also responsible for removing the connections when they are no longer needed, such as when a user logs out or a profile is removed. If you fail to delete the connections, the SDK will attempt to connect to the toothbrush, who will never advertise itself since it already has a connection.

To remove the connection, either invoke *remove(macAddress)* or *removeAll()*

```java
toothbrushRepository.removeAll()
```

We can have multiple connections established at the same time

## Managing a connection to a toothbrush
*KLTBConnection* exposes interactions with a Toothbrush through several handles
 
* Toothbrush

  * Information about the toothbrush (mac address, model, serialNumber, software version, ...)
  * Change part of this information (*setAndCacheName*, *cacheName*)

* Vibrator

  * Information of the vibration state of the toothbrush (*isOn*)
  * Change the vibration state of the toothbrush (*on*, *off*)
  
    ```java
      connection.vibrator().on().sync();
    ```
   * Register as observer of vibration state changes (*register(VibratorListener)*)

* ConnectionState

  * Information on the connection state of the toothbrush (*KLTBConnectionState*: Active, Terminated, etc.)
  * Register as observer of the current *KLTBConnectionState* (*register(ConnectionStateListener)*)
  * A *KLTBConnectionState.TERMINATED* is non-recoverable

* DetectorsManager (see the dedicated section)

  * Provides detectors interfaces you can subscribe to (*OSMDetector*, *SVMDetector*, *RNNDetector*, *RawDetector*)
  * Information on the calibration data of the toothbrush
  * Set if user is left or right handed

* Brushing

  * Support to count and fetch brushings stored in the toothbrush
  * Get and set the default duration
  * Register as an observer of the current brushing

* Parameters

  * Information on some parameters of the toothbrush, such as the owner or the toothbrush time

* User

  * Information on the user that owns the toothbrush
  * Modify the toothbrush owner
  * Read and set multi user mode

* disconnect

  * Disconnect from this toothbrush

* getTag

* setTag

## Kolibree movement detectors

Kolibree provides several movement detectors. They are automatically enabled or disabled on listener registration / unregistration, so make sure to unregister any listener when the detector's output is no more needed to prevent battery draining.


### Probable Mouth Zones detector

This detector provides a list of *MouthZone" objects that represent the zones that the user is probably brushing.
Available on all Kolibree devices, for the Ara and Connect M1 models the output zones will be the same as the *Most Probable Mouth Zones detector*'s ones.
Also known as SVM detector.

```java
final SVMDetectorListener svmDetectorListener = new SVMDetectorListener() {
  @Override
  public void onSVMData(@NonNull KLTBConnection source, @NonNull ArrayList<MouthZone> probableZones) {
    // probableZones contains at least one MouthZone
  }
};
    
connection.detectors().probableMouthZones().register(svmDetectorListener);
// ...
connection.detectors().probableMouthZones().unregister(svmDetectorListener);
```

### Most Probable Mouth Zones detector

This is the most accurate mouth zones detector. It outputs a sorted *WeightedMouthZone* list with 6 items. The zone at index 0 is the most probable one. Each zone has a trust factor (weight).
This detector is only available on Ara and Connect M1 devices. Also known as RNN detector or MPMZ detector.

```java
final RNNDetectorListener rnnDetectorListener = new RNNDetectorListener() {
  @Override
  public void onRNNData(@NonNull KLTBConnection source, @NonNull ArrayList<WeightedMouthZone> zones) {
    // Index 0 is the most probable zone
  }
};
    
// This method will return null with a Kolibree V1 toothbrush
connection.detectors().mostProbableMouthZones().register(rnnDetectorListener);
// ..
connection.detectors().mostProbableMouthZones().unregister(rnnDetectorListener);
```

### Raw detector

This detector outputs raw sensor values (magnetometer, accelerometer and gyroscope vectors). It does not provide human readable zoning information.
Available on all Kolibree devices.

```java
final RawDetectorListener rawDetectorListener = new RawDetectorListener() {
  @Override
  public void onRawData(@NonNull KLTBConnection source, @NonNull RawSensorState sensorState) {
    // sensorState.getAcceleration().get(Axis.X);
  }
};

connection.detectors().rawData().register(rawDetectorListener);
// ...
connection.detectors().rawData().unregister(rawDetectorListener);
```

# Dependencies
If you wish to specify Android-SDK dependencies' version, you can do it in your root *build.gradle*, inside an *ext* block. 

```
ext {
    supportLibs = "27.1.1"
    
    mockitoVersion = "2.17.0"
}
```

If you don't specify any, the default values as of 7-sept-2018 are

```
ext {    
    supportLibVersion = "27.1.1"
    
    daggerVersion = "2.17"
    
    rxJavaVersion = "2.1.15"
    rxRelayVersion = "2.0.0"
    replayShareVersion = "2.0.1"
    
    mockitoVersion = "2.17.0"
}
```

# Dagger

Android-SDK uses *dagger* and *dagger-android* dependencies. At the time of this writing, version is *2.11* 

## If your project uses dagger
You'll need to define your dagger version in root build.gradle. Add to your ext block

```
ext {
    ...
    
    daggerVersion = "2.17"
    ...
}
```
You can make your app component depend on SdkComponent like this

```java
@Component(
    dependencies = SdkComponent.class,
    modules = {...}
)
public interface AppComponent {

  @Component.Builder
  interface Builder {
    ...
    
    Builder sdkComponent(SdkComponent sdkComponent);
    
    AppComponent build();
  }
}
```

It's important that you build SdkComponent and your graph as early in the application initialization as possible, 
otherwise your application and the sdk might not share the dependencies. *You'll need to manually pass the created SdkComponent* to your AppComponent. 
To do so, I recommend doing it in the Application class

```java
class BaseKolibreeApplication extends Application {
  public static AppComponent appComponent;
  
  @Override
  public void onCreate() {
    super.onCreate();
    
    initDagger();
  }
  
  private void initDagger() {
    SdkComponent sdkComponent = KolibreeAndroidSDK.init(this);
    
    appComponent = DaggerAppComponent.builder()
        ...
        .sdkComponent(sdkComponent)
        .build();
  }
```

## If your project doesn't use Dagger

You don't need to do anything

# Providing Translations

If you whish to change the text displayed in the status bar when KolibreeService is running,
you can provide your own TranslationsProvider when initializing the component

```
private void initDagger() {
    TranslationsProvider translationsProvider = new TranslationsProvider();
    
    Map<String, String> map = new HashMap<>();
    map.put(RUNNING_IN_BACKGROUND_KEY, "New english message");
    Map<String, String> chinaMap = new HashMap<>();
    chinaMap.put(RUNNING_IN_BACKGROUND_KEY, "Chinese message");
    translationsProvider.addLanguageSupport(Locale.US, map);
    translationsProvider.addLanguageSupport(Locale.CHINA, chinaMap);
    
    SdkComponent sdkComponent = KolibreeAndroidSDK.init(this, translationsProvider);
    
    appComponent = DaggerAppComponent.builder()
        ...
        .sdkComponent(sdkComponent)
        .build();
    
    appComponent.inject(this);
  }
```

## Supported keys
- RUNNING_IN_BACKGROUND_KEY: text displayed in a notification in the status bar when we there's a background service running
