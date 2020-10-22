# Changelog

All changes to the Android Modules SDK are documented below.

All new SDK versions adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
- Increment **MAJOR** version when introduced massive incompatible API changes (like large refactors, with impact on several modules),
- Increment **MINOR** version when new functionalities added in a backwards compatible manner, and
- Increment **PATCH** version when made backwards compatible bug fixes.

## [6.6.3]

### Enhancements
- Add httpCode to ApiError unknown state

### Breaking changes

- Converted ApiError to kotlin. Removed all Error codes. Please use com.kolibree.android.network.api.ApiErrorCode

## [6.6.2]

### Enhancements

- [[KLTB002-13131](https://kolibree.atlassian.net/browse/KLTB002-13131)] - Add `sourceApplication` field in IProfile,
which can be used to distinguish from which app the profile is created.
- Bump dagger version to 2.29.1
- [[KLTB002-13202](https://kolibree.atlassian.net/browse/KLTB002-13202)] - Brushing program night mode has been replace by polishing mode,
translation key too
- [[KLTB002-13204](https://kolibree.atlassian.net/browse/KLTB002-13204)] - All reference to Brushing program night mode has been replace by polishing mode
- [[KLTB002-13204](https://kolibree.atlassian.net/browse/KLTB002-13204)] - Add PolishingBrushing to the BrushingModePattern enum
- [[KLTB002-13193](https://kolibree.atlassian.net/browse/KLTB002-13193)] - ActiveConnectionUseCase emits KLTBConnection instead of mac address
- [[KLTB002-13193](https://kolibree.atlassian.net/browse/KLTB002-13193)] - Introduce BrushingSessionMonitor to monitor the status of a brushing
- [[KLTB002-13193](https://kolibree.atlassian.net/browse/KLTB002-13193)] - Add stateStream to ConnectionState
- [[KLTB002-13193](https://kolibree.atlassian.net/browse/KLTB002-13193)] - OfflineBrushing are now retrieved after a brushing session is end

## [6.6.1] 2020 September 25th

### Bug fixes

- [[KLTB002-13145](https://kolibree.atlassian.net/browse/KLTB002-13145)] - Fix Pirate restarting the application on exit

The Activity that launches a Unity game (launcher) must declare `android:launchMode="singleTop"` or `android:launchMode="singleInstance"`.
When the game exits, the host Activity will invoke startActivity specifying launcher as class.
If the class does not declare `singleTop` or `singleInstance`, the Activity stack will host multiple launcher Activity instances.

Invoking startActivityForResult on a Unity game activity does not work because it lives in a separate task

### Demo App

- Add measures to prevent NullPointerExceptions. This is not how you should manage Account or Active profile, but we want
to register all changes we make to SdkDemo
- Remove AssetBundleShowCaseActivity, which is no longer supported

## [6.6.1] 2020 September 25th

### Bug fixes

- [[KLTB002-13145](https://kolibree.atlassian.net/browse/KLTB002-13145)] - Fix Pirate restarting the application on exit

The Activity that launches a Unity game (launcher) must declare `android:launchMode="singleTop"` or `android:launchMode="singleInstance"`.
When the game exits, the host Activity will invoke startActivity specifying launcher as class.
If the class does not declare `singleTop` or `singleInstance`, the Activity stack will host multiple launcher Activity instances.

Invoking startActivityForResult on a Unity game activity does not work because it lives in a separate task

### Demo App

- Add measures to prevent NullPointerExceptions. This is not how you should manage Account or Active profile, but we want
to register all changes we make to SdkDemo
- Remove AssetBundleShowCaseActivity, which is no longer supported

## [6.6.0] 2020 September 25th

### Bug fixes

- [[KLTB002-12586](https://kolibree.atlassian.net/browse/KLTB002-12579)] - ConnectionDoctor now use CheckConnectionPrerequisite to start the scan

### Enhancements

- [[KLTB002-11040](https://kolibree.atlassian.net/browse/KLTB002-11040)] - Refactor Pirate internals. Please test & report any issues.
- [[KLTB002-13011](https://kolibree.atlassian.net/browse/KLTB002-13011)] - Add SwitchOffMode.TRAVEL_MODE to SwitchOffMode 
  and add switchOffDevice to Toothbrush
- [[KLTB002-12923](https://kolibree.atlassian.net/browse/KLTB002-12923)] - Introduce ExceptionLogger to catch apiError's, core module use NoOpExceptionLogger
- [[KLTB002-8998](https://kolibree.atlassian.net/browse/KLTB002-8998)] - Toothbrushes must have a minimum of 30% battery to perform an OTA

### Breaking changes

- PirateCompatActivity is no longer visible. Use `com.kolibree.android.pirate.PirateCompatActivityKt.createPirateIntent` to create the intent
- PirateCompatActivity.ACTIVITY_TO_OPEN is no longer visible
- Pirate will destroy the process after it exits

### Demo App

- PirateCompatActivity will now start LauncherActivity after exit, instead of MainActivity
- BrushingFragment uses com.kolibree.android.pirate.PirateCompatActivityKt.createPirateIntent

## [6.5.0] 2020 August 27th

### Breaking changes

- EventTrackerLifecycleCallbacks: `triggerEvent(event: AnalyticsEvent)` replaced with `setScreenName(activity: Activity, screenName: String)`
- `app_name` string was removed from resource and replaced by `Context.appName()`

### Enhancements

- Improve magic link parser error handling
- Analytics: `setCurrentScreen(activity: Activity, screenName: String)` method added to `EventTracker`
- [[KLTB002-12586](https://kolibree.atlassian.net/browse/KLTB002-12586)] - Update of weight file for B1, E2 and Glint
- Target Android 11
- Update Kotlin to 1.4.0

## [6.4.0] 2020 August 13th

### Enhancements

- [[KLTB002-12663](https://kolibree.atlassian.net/browse/KLTB002-12663)] - Add new translation keys to `CoachPlusTranslationKey`:
  `BRUSHING_PROGRAM_DIALOG_NIGHT` and `BRUSHING_PROGRAM_DIALOG_CUSTOM`

## [6.3.1] 2020 July 30th

### Enhancements

- Jira KLTB002-11652: Kolibree `Optional` implementation was removed and migrated to Google implementation.
- Jira KLTB002-12351: Gender is prepopulated with UNKNOWN value during account creation.
- KML is now enable by default for the checkup
- Jira KLTB002-10786: Raw-data module has been remove since KML is generating the avro file directly

## [6.2.0] 2020 July 16th

### Enhancements

- OTA might ask user for permission to reset bluetooth state

### New features

- Jira KLTB002-11595: Added brushing mode customization capabilities
Usage (kotlin):

```
connection.brushingMode().setCustomBrushingModeSettings(
    BrushingModeSettingsBuilder()
        .addSegmentWithSequence(BrushingModeSequence.GumCare, 3)
        .addSegmentWithSequence(BrushingModeSequence.NightMode, 2)
        .lastSegmentStrategy(BrushingModeLastSegmentStrategy.EndAfterLastSegment)
        .strengthOption(BrushingModeStrengthOption.ThreeLevels)
        .lastSegment(BrushingModeSequence.GumCare, 1)
        .build()
).[...].subscribe()
```

- Jira KLTB002-11595: Added Glint Overpressure sensor and pickup detection controls
Usage (kotlin):

```
connection.detectors().enableOverpressureDetector(enable: Boolean).[...].subscribe()

connection.detectors().isOverpressureDetectorEnabled().[...].subscribe()

connection.detectors().enablePickupDetector(enable: Boolean).[...].subscribe()
```

### Breaking changes

- Jira KLTB002-11713: LedPattern has been moved to .led package


## [6.1.0] 2020 July 6th

### New features

- Question of the day: new module that provides complete functionality

### Enhancements

- Jira KLTB002-11367: Introduce Hum OTA screen to start it just call `startOtaUpdateScreen` with the
right parameters (You will also probably need to custom the theme). For the translation please check
the readme in the ToothbrushUpdateUIHum module
- Jira KLTB002-11440: Don't attempt to upload a brushing if there's no internet
- Jira KLTB002-11373 Birthday in profile is no longer required

- Jira KLTB002-11574: Add overpressure feedback into coach plus a new key has been add into `CoachPlusTranslationKey`
`FEEDBACK_MESSAGE_OVERPRESSURE` 
- Jira KLTB002-11754: Bring back too slow detection for all the toothbrush models
- Jira KLTB002-11469: Migrate to brushings endpoint v2
- ConnectionDoctor won't attempt to connect for third generation toothbrushes if there is update in progress

### Breaking changes

- Jira KLTB002-11469: Brushing.points will always be 0 from now on

## [6.0.0] 2020 June 24th (never shared)

### New features

- New activity: Guided brushing, is available in the activity list. It is a next evolution of Coach+
UI with several additions and upcoming features.

SDK demo app contains sample code that allows to configure & launch the new activity.

Requirements:
1. Make sure your component allows injection into `GuidedBrushingJawsView`
```
void inject(GuidedBrushingJawsView view);
```
and `viewInjector` calls it:
```

  @NotNull
  @Override
  public <T extends View> ViewInjector<T> viewInjector(@NotNull Class<T> clazz) {
    //...
    if (GuidedBrushingJawsView.class.equals(clazz)) {
      return view -> demoAppComponent.inject((GuidedBrushingJawsView) view);
    }
  }
```
2. Start guided brushing activity in pretty much the same way as Coach+:
```
  v.findViewById(R.id.guided_brushing).setOnClickListener(v1 -> displayGuidedBrushing());

  ...

  /**
   * action when clicking the guided brushing button. Will launch the guided brushing
   * if there is a connected toothbrush, otherwise start manual guided brushing
   */
  private void displayGuidedBrushing() {
    if (getContext() != null) {
      PairingSession session = accountInfo.getPairingSession();
      Intent intent;
      if (session != null && getContext() != null) {
          ToothbrushFacade toothbrush = session.toothbrush();
          intent = guidedBrushingFactory.createConnectedGuidedBrushing(
                          getContext(), toothbrush.getMac(), toothbrush.getModel(), null);
        } else {
          intent = guidedBrushingFactory.createManualGuidedBrushing(getContext(), null);
        }
        /*
        you should use startActivityWithResult if you wish to know if the session has been recorded
         */
        startActivity(intent);
      }
    }
```

### Enhancements

- Jira KLTB002-10802: Since the brushing dateTime is stored with OffsetDateTime in the current timezone, we
introduced a new extension function `OffsetDateTime.toCurrentTimeZone()` to convert the dateTime into the current timezone
- Jira KLTB002-11351: Refresh stale data in AccountToothbrush. From now on, it'll contain the most up to date data
coming from the toothbrush
- Jira KLTB002-11223: Added support for Glint's Overpressure Sensor
- Jira KLTB002-11223: Bound overpressure stream to Coach+ and TestBrushing KML contexts
- Jira KLTB002-11434: Added Mode LEDs test pattern command
- Jira KLTB002-11224: Added Glint Brushing Modes

### Breaking changes

- Jira KLTB002-10802: Usage of ZonedDateTime is now deprecated in Brushing. OffsetDateTime is exposed instead.
Also Brushing now exposes `getDateTime()` instead of `getDate()`.
- Jira KLTB002-10802: Quality field has been removed from the brushing. It should be taken from `CheckupCalculator`.
- Upgraded KML to 1.4.1, KML contexts have a new constructor parameter
- JIRA KLTB002-11387: IProfile.pictureUrl is deprecated, use avatar cache url instead.
    - When request profile avatar, use `AvatarCache.getAvatarUrl()` or `IProfile.getAvatarUrl()`
      (`ProfileExtension.getAvatarUrl()` in Java) instead of `IProfile.pictureUrl`.
    - A new `ProfileFacade.getProfilePicture()` API is added for clients to request the avatar url.
      The url will expire shortly.
- JIRA KLTB002-11427: Add new options for Gender and Handedness
  - Added new enum constants.`Gender.PREFER_NOT_TO_ANSWER` and `Handedness.UNKNOWN`
  - `Handedness.findBySerializedName(String)` returns `Handedness.UNKNOWN` for unrecognized input.
  - `CreateProfileData.setGender(Boolean male)` method is removed.

### Bug fixes

- Jira KLTB002-11169: Fixed brushing mode being reset immediately
- Jira KLTB002-11604: Don't notify vibration state until connection state is ACTIVE

### Demo App

- `ZonedDateTime` was replaced with `OffsetDateTime` in `AggregateDataFragment`
- `getDate` was replaced with `getDateTime` and `ZonedDateTime` was replaced with `OffsetDateTime` in `CheckupFragment` 
- To convert an OffsetDateTime into the current timezone you can check `CheckupFragment` :
```
    // Kotlin code sample: val lastSessionDate = getFormattedElapsedTimeSince(brushing.dateTime.toCurrentTimeZone())
    String lastSessionDate = getFormattedElapsedTimeSince(DateExtensionsKt.toCurrentTimeZone(brushing.getDateTime()));
```
- To get user avatar url, you can check `HeaderActivity`:
```
    // With AvatarCache, this can be used where context is not directly accessible
    @Inject AvatarCache avatarCache;
    String avatarUrl = avatarCache.getAvatarUrl(profile);
    
    // With ProfileExtension, this can be used where context is accessible
    // Kotlin code sample: val avatarUrl = profile.getAvatarUrl(getApplicationContext())
    String avatarUrl = ProfileExtension.getAvatarUrl(profile, getApplicationContext());
```
Requirements: Implement AvatarCacheWarmUp and Bind it in Dagger, check `AppModule` and `DemoAvatarCacheWarmUp`:
```
  @Binds
  abstract AvatarCacheWarmUp bindAvatarCacheWarmUp(DemoAvatarCacheWarmUp impl);
```


## [5.9.1] 2020 June 5th

### Bug fixes

- Manual coach plus was throwing an exception when a toothbrush was associated with the current profile but not connected

## [5.9.0] 2020 June 4th

### Breaking changes

- Jira KLTB002-11074: `com.kolibree.android.coachplus.v2.mvi.CoachPlusTranslationKey` has been moved 
to `com.kolibree.android.coachplus.mvi.CoachPlusTranslationKey`
- Jira KLTB002-11243: `com.kolibree.android.processedbrushings.featuretoggles.KMLCheckupFeature` has
been moved to `com.kolibree.android.feature.KMLCheckupFeature`

### Enhancements

- Jira KLTB002-11243: new `features` module, holding all dynamic features in it
- Jira KLTB002-11243: `rewards` module was split into `rewards-logic` and `rewards-ui-v1`
- Jira KLTB002-11243: `toothbrush-update` module was split into `toothbrush-update-logic` and `toothbrush-update-ui-v1`
- Jira KLTB002-10840: `ProfileInternal.getGenderEnum()` now returns `Gender.UNKNOWN`
for any serializedName other than `M` and `F`
- Jira KLTB002-10683: Add a new account endpoint to check if a phone number is already
linked with an account and WeChat

### Enhancements
- `CheckPairingPrerequisitesUseCase` now exposes `checkOnceAndStream`

### Demo App

- Jira KLTB002-11186: Add Ota update analytic events,
and provide sample code to add and consume analytics track events.
See `DemoAnalyticsTracker.java`, and `ToothbrushActivity.java`.
AnalyticsTracker is injected in `AppModule.java`, `DemoAppComponent.java`, and `MainApp.java`.

## [5.8.0] 2020 May 21st

### Breaking changes

- Jira KLTB002-10774: Remove OSM support
- Jira KLTB002-10774: Remove V1 support in Pirate (always use KML)
- `AccountOperations.createEmailAccount()` now returns `Single<AccountInternal>`
- Jira KLTB002-11074: `com.kolibree.android.coachplus.v2.mvi.CoachPlusTranslationKey` has been moved 
to `com.kolibree.android.coachplus.mvi.CoachPlusTranslationKey`
- Jira KLTB002-10996: Use NickName for HILINK toothbrushes
- `Toothbrush.setName()` now renamed to `setAndCacheName()`,
and a new `cacheName()` is added to update the name only in the App

### Enhancements

- Jira KLTB002-10943: Added DSP state to the Toothbrush interface
- Jira KLTB002-11066: Support for Hum Electric and Hum Battery
- Add `addBrushingSingle` method to `KolibreeConnector` and` ProfileWrapper`
- `addBrushingSync` methods in `KolibreeConnector` and` ProfileWrapper` are now deprecated

### Demo App

- Provide sample code to inject `ColorJawsView`,
see `MainApp.java` and `DemoAppComponent.java`:
```
public <T extends View> ViewInjector<T> viewInjector(@NotNull Class<T> clazz) {
    ......
    
    if (ColorJawsView.class.equals(clazz)) {
      return view -> demoAppComponent.inject((ColorJawsView) view);
    }
    ......
}
```
```
public interface DemoAppComponent extends AndroidInjector<MainApp> {
    ......

    void inject(ColorJawsView view);
    ......
}
```

### Bug fixes

- Jira KLTB002-10809: Fixed E2 can be updated without being on the charger
- Added DSP versions to the toothbrush screen

## [5.7.0] 2020 May 7th

### Breaking changes

- `ScreenName` class was removed in favour of `AnalyticsEvent`.
- `EventScreenName` enum was removed - now every module declares its own analytics.
- `EventTracker` now accepts only `AnalyticsEvent` objects.
- `HasScreenName` interface was replaced with `TrackableScreen`. 
Also, now it accepts `AnalyticsEvent` instead of strings.
- `NoTrackableEvent` interface was replaced with `NonTrackableScreen`.
- Jira KLTB002-10590: Use a different OTA upgrade welcome message for non-rechargeable toothbrushes,
`ToothBrushModel` now needs to be provided in `OtaUpdateActivity`'s `createIntent` method.
- `ColorJawInjector` is no more needed and has been removed. Please take a look a the Demo App.

### Enhancements

- `ProfileFacade.changeProfilePicture` inserts to local database before attempting to upload the 
picture. Thus, you will be notified earlier of avatar change, but pictureUrl field will refer to
a file.
- `ProfileFacade.getProfile` emits `NoSuchElementException` when requesting a profileId that's not in the database
- Jira KLTB002-10737: Bump KML to 1.4.0
- Jira KLTB002-10737: Bump GameMiddleware to 0.9.0
- Jira KLTB002-10737: Bump GameLoader to 0.16.0
- Jira KLTB002-10458: Improved Analytics API
- Jira KLTB002-10822: Added support for HiLink devices

### Bug fixes

- Change Scheduler on which Profile database results are emitted to avoid potential deadlocks 
- Jira KLTB002-10223: Fixed can't do manual coach on Huawei P20
- Coach+ crashing at end of session

### Demo App

- `ColorJawInjector` has been removed


## [5.6.0] 2020 April 25th


### Bug fixes

- Jira KLTB002-10223: Fixed can't do manual coach on Huawei P20
- Coach+ crashing at end of session

## [5.5.1] 2020 April 17th

### Bug fixes

- SDK core module doesn't depend on internal modules anymore

## [5.5.0] 2020 April 15th

### Breaking changes

- Updated Dagger to 2.27
- Jira KLTB002-8217: Bump to Android 10 (API 29):
    - Replace Restring library with Pihlology and ViewPump (No change in the interface)
    - Bump material library to 1.0.0

### Enhancements

- Jira KLTB002-10222: TestBrushing now uploads AVRO file when KML is enabled
- Jira KLTB002-9896: Rework GameProgress to work with synchronizationApi
- Jira KLTB002-10384: Added support for Hum devices scan
- Jira KLTB002-10521: Bump KML to 1.3.3
- Jira KLTB002-10568: Introduce `SmilesUseCase` to get the total amount of smiles available
- Jira KLTB002-10590: Add a message on OTA screen to remind users to keep the brush on the base
- Jira KLTB002-10510: OTA Update can't proceed if E2 is not charging when bootloader version is 
strictly lower than 2.2.5.
A new translation key has been created for this purpose: FIRMWARE_UPGRADE_MUST_BE_CHARGING_KEY

### Bug fixes

- Jira KLTB002-9941: Prevent crashes on stats when user changes to a region where weeks start at a different day
- NetworkChecker now only returns true when the network is in 'connected' state.
- Jira KLTB002-10325: Fixed brushing sessions loss after synchronization
- Jira KLTB002-10584: Fixed crash after launching Pirate Compat multiple times on Android 10

## Demo App

- Jira KLTB002-10367: Updated HeaderActivity with pairing prerequisites checking. check the
onStartPairing method.

## [5.4.0] 2020 March 16th

### Enhancements

- Jira KLTB002-10055: Modify how offline brushings are synched in background. How the new mechanism
works

```
- Schedule an alarm some time after the application goes to background
- When the alarm fires, start a scan with PendingIntent

At some point in the future, we should get a scan result on a BroadcastReceiver

- Attempt to extract offline brushings
- Schedule a new alarm after X time
- Cancel alarm when KolibreeService starts
```

Known issues: Not working on Huawei or OPPO due to background restrictions

- Jira KLTB002-10169: Disable underspeed for non manual toothbrush

### Bug fixes

- Jira KLTB002-9984: Fixed Pirate raw data crash on connection not active
- Made the rewards synchronization occur sooner in the synchronization process

### Enhancements

- HTTP connection timeout raised to 1m, read/write timeout raised to 30 seconds.

## [5.3.0] 2020 Feb 20th

### Enhancements

- Jira KLTB002-9761: Personal Challenge APIs
- Jira KLTB002-9760: Personal Challenge persistence & synchronisation
- Jira KLTB002-9978: Add capabilities header to every HTTP call
- Jira KLTB002-9867: Don't scan when app is in the background. This'll prevent the "Colgate attempted
to access your location on the background" dialog. This also implies that if the application never
scanned for the toothbrush after the phone was rebooted OR bluetooth was turned off/on, we won't be
able to sync offline brushings in the background
- Jira KLTB002-9526: Bump KML to 1.3.1
- Jira KLTB002-9523: Add KML support for Pirate

### Breaking changes

- Jira KLTB002-9523: To create a PirateFragment, the static method create should be used. Constructor is now internal.
And ToothbrushModel and mac address should be provided at creation time.

### Demo App 

- Jira KLTB002-9523: `PirateCompatActivity.createPirateIntent` have changed and now takes toothbrushModel and mac
See `BrushingFragment.java` in displayPirateGameCompat
```java
Intent intent = PirateCompatActivity.Companion.createPirateIntent(getContext(), 
                        session.toothbrush().getModel(), 
                        session.toothbrush().getMac(), 
                        MainActivity.class);
``` 

### Bugfixes

- Jira KLTB002-9808: Don't crash if injection fails in NightsWatch JobService:
We retry injection at onStartJob level, if it still fails the service shuts down

## [5.2] 2020 Feb 13th

### Enhancements

- Jira KLTB002-9105: If after deleting a profile there's only 1 profile left AND any toothbrush is shared,
set the only profile as owner of the toothbrush

### Bug fixes

- Jira KLTB002-9105: Forget toothbrush owned by a profile after it's deleted 
- Jira KLTB002-9484: Fixed offline brushing synchronization on E1/Ara with hardware multi user mode enabled.
We now disable it as soon as the connection is established
- Jira KLTB002-9536: TestAngle - Show pause dialog when coming from background

## [5.1.0] 2020 Jan 30th

### Breaking changes

- Dagger was bumped to 2.25.2. Please make sure you use the same version.
- `CoachPlusColoset`'s `create` method now takes a `Context` as parameter

### Enhancements

- Expose `versionName` and `versionCode` in `UnityAndroidSdkApplication` to provide custom values 
for the user agent.
- Jira KLTB002-9335: Make more modules provide dynamic translation. SDK demo app contains a sample, 
which shows how to use the dynamic translations feature. Features supporting dynamic translations:
  - Coach+
  - Test Brushing
  - Text Angles
  - Speed Control
- Jira KLTB002-9380: Implemented `ToothbrushInteractor`
- Jira KLTB002-9391: Fixed Plaqless LED colors
- Jira KLTB002-9616: Prevented Pirate from crashing on UninitializedPropertyAccessException
- Jira KLTB002-9617: Fixed precondition crash on database profile selection

### Bug fixes

- Jira KLTB002-9357: Fix bluetoothStateObservable sometimes emitting `false` when bluetooth was
available
- Jira KLTB002-9357: Fix not recovering properly from bluetooth off/on
- Jira KLTB002-9544: Fix OTA not starting under certain circumstances
- Jira KLTB002-9706: Fixed profile switch crash

### Demo App 

- Jira KLTB002-9618: Provide sample code for setting RXJava undeliverable exceptions handler.
See `MainApp.java`:
```
public class MainApp extends Application implements HasAndroidInjector, HasViewInjector {
    
    @Inject
    Consumer<Throwable> errorHandler;
    
    public void onCreate() {
        super.onCreate();
        
        //...

        initDagger();

        // App won't crash on undeliverable RX exceptions.
        // Has to be called after Dagger initialization
        RxJavaPlugins.setErrorHandler(errorHandler);
    }
}
```
Jira KLTB002-9504: Refactored several parts of the Demo App, same things are demonstrated but the
code base is a little fresher, and features have been sorted per type.
The last fragment has been merged with the aggregated stats one, all the profile / account
demonstrated methods have been moved to the first one (ProfileFragment)
Please note that the "Jaws toggle" feature is now enabled, click on the checkup view for a
demonstration.

## [5.0.0] 2020 Jan 13th

### Enhancements

- Jira KLTB002-8794: Bump kml to 1.1.1
- Jira KLTB002-7662: Simplify Account and Profile operations discoverability by unifying them under
`ProfileFacade` and `AccountFacade`. Please report if you find operations in other components that
aren't available in the corresponding Facade

Most of these operations were previously exposed in `IKolibreeConnector`. They are still there, but
we recommend that you move to the Facade
- Jira KLTB002-9090: Reacting to challenge details push notification
- Jira KLTB002-9171: Fixed jaws bottom tilt
- Upgrade to Dagger 2.25.2

- `DaggerDialogFragment` was added as a new recommended implementation for dialogs. It supports Dagger
injections and databinding for its view.
- `KolibreeDialogFragment` is now deprecated. Migrate to `DaggerDialogFragment`.
- `BaseDaggerDialogFragment` is now deprecated. Migrate to `DaggerDialogFragment`.
- Jira KLTB002-8856: Added plaque level to Coach+
- Jira KLTB002-9015: Disabled Pirate for Plaqless toothbrushes 
- Jira KLTB002-8364: Fast 3D model loading
- Jira KLTB002-9271: Using api.s.kolibree.com as new staging URL
- Jira KLTB002-8295: Support Plaqless error in Coach+ (Feedbacks Out of Mouth and Rinse your brush)
- Jira KLTB002-9232: Added `getBrushingSessions` method to `BrushingFacade`. `getBrushingsSince`
and `getBrushingsBetween` methods have been deprecated. See Demo app section for more information.

### Demo App

- Jira KLTB002-9232: The `getBrushingSessions` method usage is demonstrated in the `BrushingDataFragment` class

### Breaking changes

- Move profile related methods to `ProfileFacade` from `AccountFacade`.
Check HeaderActivity, ProfilesListActivity or DashboardFragment as examples.
- `ProfileFacade.getProfile` now returns the local profile. Up to now, it always requested the
profile from the backend. Use `ProfileFacade.getRemoteProfile` to request the profile from the
backend
- `ProfileFacade.getProfilesList` now returns all local profiles. Up to now, it always requested the
profiles from the backend. Use `ProfileFacade.getRemoteProfiles` to request the profiles from the
backend
- `ToothbrushPickerView` was removed from `SmartBrushingAnalyzer` module. 
Use `ToothbrushPickerDialogFragment` from `Game` module instead. 
- Dagger replaced `HasXXXInjector` for `HasAndroidInjector`. See SdkDemo on how to fix compilation issues.
In our case, we applied changes to `MainApp` and some Activities

`HasActivityInjector, HasSupportFragmentInjector, HasServiceInjector` -> `HasAndroidInjector`
`@Inject DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

```
@Inject DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;
@Inject DispatchingAndroidInjector<Service> dispatchingServiceAndroidInjector;

@Override
public DispatchingAndroidInjector<Activity> activityInjector() {
return dispatchingAndroidInjector;
}

// needed for the services used inside the SDK
@Override
public AndroidInjector<Service> serviceInjector() {
return dispatchingServiceAndroidInjector;
}
```

Replaced for
```
@Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

@Override
public DispatchingAndroidInjector<Object> androidInjector() {
return dispatchingAndroidInjector;
}
```
- Jira KLTB002-8364: `JawsView` has been removed from the codebase
- Jira KLTB002-7311: `Feature`s & `FeatureToggle`s can now hold generic values, not only booleans.
Currently supported types: `Boolean`, `Long`, `String`.
Java injection:
```
  @Inject Set<FeatureToggle<?>> featureToggles;
```
Kotlin injection:
```
  @Inject constructor(featureToggles: FeatureToggleSet) // Set<@JvmSuppressWildcards FeatureToggle<*>>
```

### Bug fixes

- Jira KLTB002-9449: Fixed crash in Coach+ used with a V1
- Jira KLTB002-9472: Added timestamps and labels to Plaqless raw data

## [4.14.1] 2019 December 18th

### Enhancements
- Add SpeedControlActivity to Core

### Demo App
- Demonstrate how to launch SpeedControlActivity in `BrushingFragment`

## [4.14.0] 2019 December 16th

### Enhancements

- Jira KLTB002-3060: Added tilt movement to checkup view
- Jira KLTB002-9122: Expose KML KPIs in aggregated stats. Check interface `SpeedAndAngleStats`
- Jira KLTB002-9154: Upgraded Instabug to 9.0.3
- Jira KLTB002-9096: Allowed creating V3 accounts with `UNKNOWN` gender
- Jira KLTB002-9154: Upgraded Instabug to 9.0.3
- Bump Android Gradle Plugin to 3.5.3
- Bump kotlin to to 1.3.61
- Jira KLTB002-9331: Added a `setBackgroundColor` method to `ColorJawsView`

### Bug fixes

- Jira KLTB002-9167: Ensure we unpair for toothbrush when cancelling pairing
- Jira KLTB002-9151: Fixed main activity's view model not being created at right time
- Jira KLTB002-9202: Refactor SdkDemo's header to use ToothbrushesForProfileUseCase
- Jira KLTB002-9249: Fix crash when processing manual brushings in Aggregated Stats 

### Demo App 

- Please check HeaderActivity for the way to listen the update of connection state
- ToothbrushesForProfileUseCase is added to get all connections of current profile

## [4.13.1] 2019 December 9th

### Enhancements

- Jira KLTB002-9206: Using Jaws v2 in Demo app

### Demo App 

- Please check CheckupFragment and DashboardFragment before you update the 3D Jaws

## [4.13.0] 2019 December 5th

### Enhancements

- Jira KLTB002-8926: Removed Coach+ v1
- Jira KLTB002-8946: Unified Coach+ brush heads animations
- Jira KLTB002-9001: Support bootloader upgrade
- Jira KLTB002-9001: Added `bootloaderVersion` to `Toothbrush`. Supported on all models except V1, Ara and E1.
- Jira KLTB002-9001: Added `dspVersion` to `Toothbrush`. Supported only on Plaqless Pro

### Bugfixes

- Fix `ToothbrushesForProfileUseCaseImpl` sometimes emitting an empty list even tho there were paired toothbrushes
- Fixed legacy jaws not rendered

## [4.12.0] 2019 November 21st

### Enhancements

- Jira KLTB002-8800: React to Brushing Mode changed by user directly on the toothbrush
- Jira KLTB002-8753: Rewrote all 3D rendering code, unified jaws models
- Jira KLTB002-8718: Improved performance of ColorJawsRenderer
- Jira KLTB002-8872: Expose a way for SDK clients to change profile brushing program
- Jira KLTB002-8968: Expose aggregate KPI in checkup data
- Jira KLTB002-8786: Sync brushing mode for new account and multi profile
- Jira KLTB002-8301: Expose getDSPFirmwareVersions in KolibreeBleDriver (only available for Plaqless)
- Jira KLTB002-8859: Disabled CareOS flavor
- Jira KLTB002-7731: Added Plaqless data to AVRO data collection
- Jira KLTB002-8781: Brushing program for B1
- Added `ToothbrushesForProfileUseCase`, a helper to get KLTBConnections available for a given
profile id or the active profile
- Jira KLTB002-9172: Fix bluetooth commands failing randomly

### Bugfixes

- Jira KLTB002-9037: Only adjust zone duration in Coach+ for toothbrushes that support Brushing Mode
- Jira KLTB002-8812: Fixed Ara firmware update
- Jira KLTB002-9172: Fix ghost E2 vibration start

### Breaking changes

- A new registered `ConnectionStateListener` immediately receives the current `KLTBConnectionState`

### Demo App 

- Using BrushingModeSetting to change brushing mode. There is a sample in BrushingFragment of SDK demo.
    1.Inject the BrushingModeSetting to the somewhere you needs:
    ```
    @Inject
    BrushingModeSetting brushingModeSetting;
    
    ```
    2.To get current brushing mode:
    ```
    disposables.add(connection().userMode().profileId()
               .map(profileId -> brushingModeSetting.getBrushingMode(profileId))
               .subscribeOn(Schedulers.io())
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(mode -> ...));
    ```
    3.To set brushing mode to toothbrush:
    ```
     disposables.add(brushingModeSetting.setBrushingMode(profileId, mode)
                    .subscribeOn(Schedulers.io())
                    .subscribe());
    ```
    
- Here is a sample to get the KPI averages.
    1.Toggle on the KML for Checkup
    ```
      private void enableKMLCheckup() {
        toggleForFeature(featureToggles, KMLCheckupFeature.INSTANCE).setEnabled(true);
      }
    ```

    2.Inject the CheckupCalculator. 
     ```
        @Inject
        CheckupCalculator checkupCalculator;
        
     ```
    3.Using CheckupCalculator to get CheckupData
     ```
        CheckupData checkupData = checkupCalculator.calculateCheckup(brushing);
        speedView.setText(checkupData.speedAverage());
     ```

## [4.11.0]

### Enhancements

- Jira KLTB002-8587: Introduce DisposableScope
- Jira KLTB002-8430: Analytics migration from Google Analytics to Firebase Analytics
- Jira KLTB002-8851: Update range of brushing goal duration from [120s, 240s] to [10s, Int.MAX_VALUE]
- Jira KLTB002-7758: Use FileService to extract offline brushings
- Jira KLTB002-8787: Prevent E1 auto shutdown if app in foreground
- Jira KLTB002-8879: Coach+/Test your angles can now recover from app going to background
- Improve bluetooth reliability by running all commands run on the same scheduler 

## [4.10.2] 2019 October 25th

### Enhancements

- Pressing back button in paused Coach+ finishes the game
- Pressing back button in Coach+ before the game starts closes the game

### Bugfixes

- Jira KLTB002-8782: Fix Avro upload (wrong dagger scope)
- Jira KLTB002-8636: Pause menu appears by itself in Coach+ when brushing ends
- Jira KLTB002-8826: Fix Coach+ don't start the toothbrush vibrating
- Jira KLTB002-8463: Fixed app not waking E1 up during slow mode

## [4.10.1] 2019 October 24th

### Breaking changes

- Removed NightsWatchOfflineBrushingsCheckerFeature.INSTANCE. Offline background sync is enabled by default

### Enhancements

- Jira KLTB002-8309: Show notification when offline brushings are synched in background. Optional through FeatureToggle
- Jira KLTB002: ServiceProvider now exposes `connectOnce()` and `connectStream()`. `connect()` and `provide()` are deprecated.
- Offline brushings are synced in background. No longer hidden behind a feature toggle
- Update of KML to 1.0.0
    - Jira KLTB002-8717: Disable LowSpeed detection for E2/B1/PQL
    - Jira KLTB002-8550: Pass `PlaqlessError` to KML to get `PlaqueAggregate` in `SupervisedResult`
    - Update PlaqlessError to match the last revision
- Jira KLTB002-8785: Coach+MVI is enabled by default
- Jira KLTB002-8214: Prevent the user frustration in Coach plus now affect Feedback and it increase to 1s instead of 500ms
- Jira KLTB002-8295 : Pause Coach+ MVI on Plaqless `OUT_OF_MOUTH` and `RINSE_BRUSH_HEAD` errors
- Jira KLTB002-8495 : Added a profileOrSharedModeId() method to the User interface
    
### Demo App 

- Show how to enable/disable offline brushings background synchronization in MainApp
    ```
    private void enableBackgroundOfflineSync(boolean enabled) {
        toggleForFeature(featureToggles, NightsWatchOfflineBrushingsCheckerFeature.INSTANCE).setEnabled(enabled);
      }
    ```

It's enabled by default in your application unless you specify otherwise

- Remove init of KML in MainApp. It's enabled by default in CoreSDK

### Bugfixes

- Jira KLTB002-8574 : Last sync always shows "Never" instead of accurate date
- Jira KLTB002-8775 : Recover from long pause in Coach+ 
- Jira KLTB002-8744 : Fixed laggy blur effect in Coach+ on Android 7
- Jira KLTB002-8707 : Add recovery mechanism for stat offline database migrations
- Jira KLTB002-8776 : Disable SVM detector when KML is enable and not needed
- Jira KLTB002-8797 : Use of multiple toothbrush in Coach+ (MVI enabled) was not working
- Jira KLTB002-8333 : Trigger synchronisation after new account is created
- Jira KLTB002-8782 : Fix Avro files not created in Coach+
- Jira KLTB002-7901 : Forget connections when user logs out

## [4.9.0] 2019 October 10th

### Breaking changes

- Classes from Game module moved from `com.kolibree.game` package to `com.kolibree.android.game`
- OfflineBrushingsRetrieverViewModel:
-- No longer has to be added as LifecycleObserver
-- Needs ExtractOfflineBrushingsModule. See Demo App section for an example
- Jira KLTB002-8214 : Rename package name `com.kolibree.android.app.ui.assign_activity_data` to `com.kolibree.android.app.ui.assignactivitydata`
- Jira KLTB002-8214 : CoachPlusFactory (should be injected) is now the way to instantiate coachPlus instead of `CoachPlusActivity` the interface didn't change
    `createConnectedCoach` and `createManualCoach`are the same as before

### Enhancements

- Jira KLTB002-8309: Remove RecordedSessionPersisted entity and table
- Jira KLTB002-8309: Expose OfflineBrushingsInterface
- Jira KLTB002-4533: Extract offline brushings in a background periodic job and show notification
- UnityAndroidSdkComponent is now scoped with @MagikScope. This shouldn't affect UnitySDKComponent,
since it's scoped with @Singleton
- Jira KLTB002-8560: Send RawData to listeners while game is paused
- Jira KLTB002-8214: Refactor CoachPlus and BaseGame to MVI (rename old implementation to Legacy)
- Jira KLTB002-8576: Fix corrupted account/profile data when the app is updated

### Bugfixes

- Fix KLTBConnection isSharedModeEnabled() sometimes returning false when it was true
- Jira KLTB002-8572: Fix random disconnects while navigating the through different screens
- Jira KLTB002-8388: Fix duplicated offline brushing after long pause during a game
- Jira KLTB002-8599: Fix ANR occurring sometimes after exiting Coach+ and pressing back

### Demo App 

- Remove OfflineBrushingsRetrieverViewModel as lifecycleObserver
- Add ExtractOfflineBrushingsModule to MainActivity in DemoActivityModule
- Add 'Brushing Quiz' in the BrushingFragment. Only works on E2 and B1. On E2, it needs FW 1.6.0
- Add 'Test your angle' in the BrushingFragment
- Remove injection of StatsOfflineFeatureToggle from MainActivity. This feature is enabled by default
- Jira KLTB002-8214 : Inject `CoachPlusFactory` in `BrushingFragment`
    ```
        @Inject
        CoachPlusFactory coachPlusFactory;
    ```
    and replace usage of `CoachPlusActivity` in `BrushingFragment`
    ```
    if (session != null && getContext() != null) {
            ToothbrushFacade toothbrush = session.toothbrush();
            intent =
                coachPlusFactory.createConnectedCoach(
                    getContext(), toothbrush.getMac(), toothbrush.getModel(), null);
          } else {
            intent = coachPlusFactory.createManualCoach(getContext(), null);
          }
    ```

## [4.8.5] 2019 October 9th

### Enhancements

- Fix scoping issues in Magik SDK

## [4.8.4] 2019 October 7th

### Enhancements

- Magik SDK doesn't depend on Toothbrush SDK anymore

## [4.8.3] 2019 September 27th

### Bugfixes

- Jira KLTB002-8485 : Fixed Coach+ restart button making game finishing


## [4.8.2] 2019 September 20th

### Bugfixes

- Removed the OnUserLoggedInCallback provider in UnityAndroidSDKModule. Now provided on Magik's side

## [4.8.1] 2019 September 19th

### Enhancements

- Sanitize dependency list of Magik SDK

## [4.8.0] 2019 September 19th

### Enhancements

- Jira KLTB002-8325 : Add ARM 64 support to SDK & Unity
- Jira KLTB002-8294 : Support of Plaqless Toothbrush in TestBrushing (SBA)
- Jira KLTB002-8124 : Support of Plaqless Toothbrush in Coach+
- Jira KLTB002-8237 : AppData V1 database insertion is now synchronous
- Jira KLTB002-8149 : Added Brushing program selection from Coach+
- Jira KLTB002-7726 : Bind ring LED color in Coach+ with Plaqless 
- Jira KLTB002-8150 : Added offline synchronization to Brushing Program
- Jira KLTB002-7783 : Send supervisionInfo in Coach+

### Bugfixes

- Jira KLTB002-8294 : FIX wrong Plaqless BLE characteristic for notifications
- Jira KLTB002-8294 : FIX wrong animation in TestBrushing (SBA)
- Jira KLTB002-8294 : FIX issue with KML (update to 0.0.8)
- Jira KLTB002-8376 : Fixed shared mode not reflected in the database and removed toothbrush renaming
- Jira KLTB002-8389 : FIX issue with feedback after pause in Coach+

## [4.7.2] 2019 September 12th

### Enhancements

- Patch incorrect values for brushing/profile goal times with default value (120s)

## [4.7.1] 2019 September 11th

### Bugfixes

- Jira KLTB002-7515 : Handle disconnection (BLE) in Coach+

## [4.7.0] 2019 September 5th

### Enhancements

- Jira KLTB002-8146 : Quiz for brushing program
- Jira KLTB002-8248 : Expose Plaqless notification data

### Breaking changes
- Validation added to `Account`, `IProfile` and `IBrushing` objects.  
  - Creation of any of those objects with incorrect parameters will raise a runtime exception.
  - `BrushingFacade` and `AccountFacade` now apply validation to `IProfile` and `IBrushing` method parameters.
  - `Account` validations
    - `pubId` cannot be an empty string
    - `backendId` cannot be < 0
    - `ownerProfileId` cannot be < 0
  - `IProfile` validations
    - `id` cannot be < 0
    - `firstName` cannot be an empty string
    - `brushingGoalTime` must be in range <120, 240> seconds (inclusive)
    - `createdDate` must be a valid date time in `yyyy-MM-dd'T'HH:mm:ssZ` format
    - `points` cannot be < 0
    - `age` cannot be < 0
    - `brushingNumber` cannot be < 0
  - `IBrushing` validations
    - `goalDuration` must be in range <120, 240> seconds (inclusive)
    - `duration` cannot be < 0
    - `timestamp` cannot be < 0
    - `coins` cannot be < 0
    - `points` cannot be < 0
    - `profileId` cannot be < 0
    
### Bugfixes

- Jira KLTB002-8024 : Fixed current profile automatically switched when the rewards tab is shown
- Jira KLTB002-7515 : Handle disconnection (BLE) in Coach+
- Jira KLTB002-7853 : Fixed current profile always be changed to main profile

### Enhancements

- Jira KLTB002-8148 : Added link to Brushing Quiz from Profile screen
- jira KLTB002-8128 : Add `commercialSubscription` field to `CreateAccountV3Data`
- Jira KLTB002-8107 : Remove `RedeemFeatureToggle`

### Demo App 

- Jira KLTB002-8144 : Add `MainApp.enableKML` call in `MainApp.onCreate`to enable KML features 
(Coach + feedbacks speed and angles)

## [4.6.1]

### Bugfixes

- Jira KLTB002-8023 : Update to KML 0.0.6 and FIX usage of wrong timeZone in CreateBrushingData and IBrushing
- SDK clients no longer need to enable databinding for Coach+

## [4.6.0] 2019 August 23rd

### Breaking changes

- Provides magik-sdk module for the Magik team. UnityPluginModule class requires new parameter _clientSecretIv_.
- Jira KLTB002-7526 : Moved Test Brushing code from com.kolibree.sba to com.kolibree.android.sba
- Jira KLTB002-8023 : Integration of the new version of KML 0.0.5 (kml context now needs angles and transitions)
- CoachPlusView should be injected manually. Please check the Demo App for more detail.

### Enhancements

- Jira KLTB002-7734 : Support plaqless service and characteristics
- Jira KLTB002-8036 : Expose Aggregated data reactive stream interfaces
                    : Added monthStatsStream and weekStatsStream to AggregatedStatsRepository
- Jira KLTB002-7859 : Expand DayAggregatedStats
                    : Added isPerfectDay. Value is true when user has brushed more than 2 times
- Jira KLTB002-7661 : Revamp Dagger setup for Magik
- Jira KLTB002-7659 : Added backend error parsing to all backend calls
                    : Added new ApiErrorCode object that lists all the backend API error codes
- Jira KLTB002-7984 : Don't query AppData unless requested in Dagger graph
- Jira KLTB002-7658 : Allow whitelisted SDK consumers to re-enable network logs
- Jira KLTB002-7260 : Add pagination to Brushing API - by date or brushing id-s
- Jira KLTB002-7261 : Prefetch previous month brushings in Calendar
- Jira KLTB002-7526 : Add ability to do TestBrushing with KML
- Jira KLTB002-8023 : No more crash with KML and MouthZone different than 16
- Jira KLTB002-8101 : Add new refresh token V3 API for accounts without email, phone number or appId
- Jira KLTB002-7957 : Unified and improved home page styling

### Bugfixes
- Jira KLTB002-7037 : Fix Java.lang.RuntimeException: createContext failed: EGL_BAD_CONFIG in CoachPlusView
  For more detail, see: https://github.com/kolibree-git/android-monorepo/pull/139 and https://github.com/kolibree-git/android-monorepo/pull/161
- Jira KLTB002-8101 : Fix refresh token mechanism for WeChat-based accounts
- Jira KLTB002-7100 : Fix resume button not working in Coach+ with M1. Fix progress restore to 0 when game paused.

### Demo App 

- Jira KLTB002-7526 : Moved Test Brushing code from com.kolibree.sba to com.kolibree.android.sba
- CoachPlusView should be injected manually by adding extra codes in the implementation of HasViewInjector.viewInjector.

Add new 'if' condition for CoachPlusView in MainApp:

    if (JawsView.class.equals(clazz)) {
         return view -> demoAppComponent.inject((JawsView) view);
       }else if(CoachPlusView.class.equals(clazz)){
         return view -> demoAppComponent.inject((CoachPlusView) view);
       }
       
Add inject method for CoachPlusView in AppComponent 

    public interface DemoAppComponent extends AndroidInjector<MainApp> {
    
      void inject(JawsView mouthView);
    
      void inject(CoachPlusView coachPlusView);


## [4.4.31.1] 2019 August 12th

- 4.4.31 released with proguard improvements from 4.5.x

## [4.5.3] 2019 July 30th

### Enhancements

- SdkDemoApp will be signed with release certificate if we specify the parameters. Only relevant to Kolibree Developers
- Proguard configuration updated
- Jira KLTB002-7672 : Add settings option to StartMessage screen
- Jira KLTB002-7787, KLTB002-7417 : Add Mouth map analysis screen for Plaqless toothbrush
- Jira KLTB002-7846 : Fixed IncInt animations for Plaqless and added a faster color rendering
- N/A               : Changed pact tests URL to use https scheme
- Jira KLTB002-7781 : Detect when an account no longer exists and force a logout
  For implementation details, see https://github.com/kolibree-git/android-monorepo/pull/94

# Breaking changes
- Change `AccountFacade.shouldLogout` return type to `Single<ForceLogoutReason>`
  - We strongly recommend SDK clients to test this
- KML is initialized internally, you no longer need to initialize it
- CoreSDK must be injected into MainApp

## Demo App 
- Remove Kml.init() from MainApp  
- Added `listenToShouldLogout` to SdkDemoBaseActivity. When it receives a value, it cleans dataStore
and navigates to LoginActivity
- Show forced logout reason in LoginActivity, if any
- Inject CoreSDK to MainApp

## [4.5.2] 2019 July 19th

### Bugfixes

- Fixed crashes in Coach+
- Fixed crashes in TestBrushing

## Demo App 

- `ext.versions.name` from `dependencies.gradle` now serves as a single source of truth for SDK version management
  (both publication and fetching)
- `module.version` removed from demo app's `gradle.properties`

## [4.5.0] 2019 July 19th

### Enhancements

- Integration of KML (feature toggle available disabled by default)
  - Jira KLTB002-7380 : Checkup with KML
  - Jira KLTB002-7380 : IBrushing now has durationObject field (org.threeten.bp.Duration)
  - Jira KLTB002-7380 : ProcessedData json generation is handled by KML
  - Jira KLTB002-7380 : CheckupCalculator.calculateCheckup now takes IBrushing or ProcessedBrushing (from KML)
  - Jira KLTB002-7221 : Coach plus with KML
  - Jira KLTB002-7221 : Add RnnWeight encrypted binaries for E1/E2/M1/Kolibree
- Jira KLTB002-7466 : Add FeatureToggle interface and TestFeatureToggle which will provide Feature in a `Set<FeatureToggle>` injected by dagger
- Add fallback to destructive operation on the AppData table 
- Jira KLTB002-7223 : Integrated new Plaqless brush head
- Jira KLTB002-7225 : Mirrored LED signals on 3D brush head 
  
### Breaking changes

- Jira KLTB002-7221 : MouthZone* is provided by KML
- Jira KLTB002-7380 : All checkup related code now has a suffix Legacy (not yet deprecated since KML is disabled by default)
- Jira KLTB002-7380 : CheckupCalculator.calculateCheckup now takes timestamp and duration
- Jira KLTB002-7225 : Moved ToothbrushModel to the common-models module


### Bugfixes

- Jira KLTB002-7221 : Color of Coach + was depending on the MouthZone16 ordinal
- Jira KLTB002-7221 : Coach + was stopping recording data after a restart
- Jira KLTB002-7634 : Processed data calculator had an issue with brushing data processing
- Jira KLTB002-7823 : Block process of cleaning SharedPreference by check package name

## Demo App 

- Jira KLTB002-7380 : Fix new usage of CheckupCalculator

Check MainApp. You need to initialize Kml *before* you initialize dagger

    public void onCreate() {
      initKml();
  
      initDagger();
    }

    private void initKml() {
      Kml.INSTANCE.init();
    }

## [4.4.35]

### Enhancements

- Jira KLTB002-7412 : TestBurshing during session screen for Plaqless toothbrush
- Jira KLTB002-7634 : LegacyCheckupUtils now handles processedData from KML
- Aggregated Stats v0.1
-- Query for LocalDate, YearMonth and Period aggregated stats

### Enhancements

- Jira KLTB002-7654 : Add a constant CODE_ACCOUNT_NOT_EXIST in ApiError

## [4.4.34] 2019 July 9th

### Bugfixes

- Fix getCurrentProfile returning null when it shouldn't

## [4.4.33] 2019 July 5TH

### Breaking changes

- AccountFacade.registerWithWechatWithToken take the wechat code to use

## [4.4.32] 2019 July 4TH - NOT RELEASED

### Enhancements

- Jira KLTB002-7575 : return WeChatData on successful/failed WeChat logging attempt
- Account now contains a list of profiles, List<IProfile>

### Breaking changes

- AccountFacade.attemptLoginWithWechat returns Single<Account> instead of Single<List<IProfile> 
- AccountFacade.registerWithWechat returns Single<Account> instead of Single<List<IProfile> 
- AccountFacade.registerWithWechatWithToken returns Single<Account> instead of Single<List<IProfile> 

## [4.4.31] 2019 July 4th

### Bugfixes

- Jire KLTB002-7571 : FIX AppData usage (Json was escaped, missing Single in API)

## [4.4.30] 2019 July 3rd

### Bugfixes

- FIX attemptLoginWithWechat not persisting account on success

## [4.4.29] 2019 July 2nd

### Bugfixes

- Jire KLTB002-7523 : FIX wrong usage of Gson which was causing a parsing error in 
                      attemptLoginWeChat

### Demo App

- FIX clientId/secret reversed for production
- Jenkins pipeline was not signing the app with the right keystore

## [4.4.28] 2019 July 1st

### Enhancements

- Jira KLTB002-7342 : Added error handling on the SMS login method (see demo app) - PSCI-115
                      Check SmsAccountManager::loginToAccount for SMS login error
- Jire KLTB002-7523 : Add registerWithWechatWithToken to handle return by attemptLogin
                      
### Bugfixes

- Jira KLTB002-7279 : Fixed manual Coach+ in background not showing resume button

### Breaking changes

- Jira KLTB002-7523 : Deprecation of loginWithWeChat now use attemptLogin
                      Suppression of isWeChatAccountUsed (should use attemptLogin)

### Demo App

- Replace usage of isWeChatAccountUsed by attemptLogin

## [4.4.27] 2019 June 26th

### Bugfixes

- N/A                 Fixed MainActivity launching offline instead of orphan brushing screen
- N/A                 BI wanted null instead of 0 for manual Coach coverage value
- N/A                 Added keeps to AccountConverters and AccountUtils

### Enhancements

- Jira KLTB002-7343 : RawDataRecorder knows about vibrator state
- Jira KLTB002-7217 : Add 'plaqless' module and Intro screen

### Breaking changes

- Jira KLTB002-7379   Update of the AES algo used to reveal encrypted res (still needed to encrypt the client secret)
- Jira KLTB002-7221   CoachPlusActivity createIntent now takes a ToothbrushModel (to deal with KML in the future)

### Demo app

- Update CoachPlusActivity createIntent with Toothbrush model

## [4.4.26] 2019 June 18th

### Bugfixes

- N/A                 Migrated to monorepo
- N/A                 Fixed missing keeps


## [4.4.25] 2019 June 17th

### Bugfixes

- Jira KLTB002-6975   Changed Kolibree service's notification text

### Breaking changes

- N/A                 IBluetoothUtils methods no longer require a Context as parameter

### Demo app

- Adapt to IBluetoothUtils changes


## [4.4.24] 2019 June 12th

### Bugfixes

- Jira KLTB002-7252   The offline brushing time is inconsistent with phone time settings
- Jira KLTB002-7135   Fixed M1 sending 'VIBRATING' AVRO brushing mode
- Jira KLTB002-7157   Fixed Manual Coach sending -1% surface average
- N/A                 Added missing keep to sms login

### Breaking changes

- N/A                 Clock is now being provided by SDK dependency injection modules. 
- N/A                 JawsView now can take its dependencies directly for context (either Activity or Application).
                      To provide the dependencies, one of the context classes needs to implement 
                      HasViewInjector<JawsView> interface and provide ViewInjector<JawsView> through it (typically it 
                      will be the application component). Please review the demo app to check the details.
                      Please review your Dagger configuration.

### Bugfixes

- Jira KLTB002-3019   Fixed orphan brushing button not clickable + appearance
                      

## [4.4.23] 2019 June 10th

### Enhancements

- N/A                 External app data synchronization is now part of the synchronising job

### Bugfixes

- N/A                 Fixed empty dependency in game module pom files


## [4.4.21] 2019 June 6th

### Bugfixes

- Jira KLTB002-7194 : Fix BrushingFacade returning duplicate brushings
- N/A                 Fixed module dependencies not defined in published pom files

### Enhancements

- Jira KLTB002-7019 : Parental consent added to account creation in v3 API


## [4.4.16] 2019 June 4th

### Enhancements

- Jira KLTB002-7089 : Improve Bluetooth reconnection. Minor E1 OTA enhancements. 
- Jira KLTB002-7099 : Support dynamic translations in Test Brushing

### Documentation

- Add Test Brushing documentation listing all the translations that can be changed dynamically

### Demo app

- Show how to change initial tip in Test Brushing in MainApp.initDagger()

## [4.4.13] 2019 June 3rd

### Bugfixes

- Jira KLTB002-7030 : Fixed Avro file upload crash when no internet
- Jira KLTB002-7055 : Fixed crash on starting KolibreeService when the app is in background
- Jira KLTB002-7177 : Fixed Game API V1 sending empty JSON
- Jira KLTB002-7089 : Create BrushingSessionStat for every brushing inserted. First step towards offline stats

### Enhancements

- Jira KLTB002-7056 : Add support for Plaqless Toothbrush

## [4.4.12] 2019 May 23rd

### Enhancements

- Jira KLTB002-6805 : Add missing timeout to OTA 
- Jira KLTB002-6942 : E1 & ARA OTA recovery mechanism added
- Jira KLTB002-6605 : Add DelegateSubscription interface which should be used to get rid of the subscribe and forget pattern
- Jira KLTB002-6605 : Migration of KLTBConnection and KLTBConnectionImpl to Kotlin
- Jira KLTB002-6725 : Add BaseMVI implementation
- Jira KLTB002-7085 : Disable OTA for E1 and Ara when Android version is below N.
- Jira KLTB002-6946 (PSCI99) : Added new WeChat account usage method (see Demo app)

### Breaking Changes 

- ProfileManager.getProfilesLocally does not take accountId as parameter anymore 

## [4.4.11]

### Bugfixes

- Fixed Game API v2 classes exposition

### Enhancements

- Moved backend error parsing to extension functions in the Network module
- Updated gradle.properties flags

## [4.4.10]

### Breaking Changes 

Jira KLTB002-6731 - We had to refactor some ViewModel and Module to work with the new [KolibreeAppVersions](CommonsAndroid/src/main/kotlin/com/kolibree/android/utils/KolibreeAppVersions.kt) object which
handle the retrieve of the AppVersions (which has been remove from KolibreeUtils). 
  - For BaseGameViewModel the context has been replace by [KolibreeAppVersions](CommonsAndroid/src/main/kotlin/com/kolibree/android/utils/KolibreeAppVersions.kt).
  - OrphanBrushingMapper need a [KolibreeAppVersions](CommonsAndroid/src/main/kotlin/com/kolibree/android/utils/KolibreeAppVersions.kt) provided you might need to create a module 
 for your activity which provide the KolibreeAppVersions
 
### Enhancements

Jira KLTB002-6875 - Exposed backend error codes for email account creation

### Demo app

- Add OrphanBrushingsActivityModule to the DemoAppComponent 


### Bugfixes

Jira KLTB002-6801 - Fixed online brushing saved as offline session
Jira KLTB002-5644 - Coach+ brushing not detected after setting sound


## [4.4.9]

### Bugfixes

NA                - FIX missing register of RawData in coach

### Other

NA                - Fixed TrustedClock being proguarded
NA                - PrivateAccessToken class is public

## [4.4.8]

### Breaking Changes

- The injected clock is now UTC by default from the TrustedClock (which should be used everywhere).
To get the system time please use the TrustedClock.getNow* methods. Please avoid the clock inject with dagger prefer a direct 
call to TrustedClock you will get more control of what you want. There is some extension available for TrustedClock in test scope 
to set/reset the internal clock of the TrustedClock.
We did this change to uniform the usage of the clock everywhere and be able to control the clock in test scope.
- Usage of Date/Time API, System.current* is discouraged like the call to *.now() of the Threeten Library (use the *.now(TrustedClock.clod) or TrustedClock.getNow* methods)

### Enhancements

Jira KLTB002-6807 - Exposed backend API errors through WeChat and phone linking methods 

#### Demo app

- Replaced usage of Date and now() in favor of TrustedClock
- See BrushingFragment for detailed use of backend API errors

### Bugfixes

Jira KLTB002-6613 - Activities pause ignored
Jira KLTB002-6622 - When the toothbrush evokes coach+, the progress is always 0%, and it returns to normal after starting and stopping.

## [4.4.6]

Note: this version has been released to the Magik team only

### Breaking Changes

- Replaced DebugLoggingTree for KLTimberTree. It's usable both in debug and release builds
- PirateActivity replaced with PirateCompatActivity

#### Demo app

- Replaced DebugLoggingTree for KLTimberTree
- PirateActivity replaced with PirateCompatActivity
- Integration with asset bundles showcase added. UnityPlayerLifecycleActivity should be used as a base class for Unity game host screen.

### Enhancements

Jira KLTB002-6547 - Added support for B1
Jira KLTB002-5315 - Synchronize and persist Profile Tier
Jira KLTB002-6639 - Synchronize Tier catalog
Jira KLTB002-6587 - Instabug user steps

### Bugfixes

Jira KLTB002-6255 - Fix E1 OTA failure
NA                - Detect external app data conflicts using save time and not version numbers

## [4.4.2]

### Bugfixes

Jira KLTB002-6622 - When the toothbrush evokes coach+, the progress is always 0% 

## [4.4.1]

### Breaking Changes

The whole demo app directory tree has been changed to match WeChat login app ID.
The application ID is now cn.colgate.colgateconnect

### Enhancements

Jira KLTB002-6382 - Added method to unlink phone number
Jira KLTB002-6380 - Added method to link a phone number
Jira KLTB002-6384 - Added method to link a WeChat account
Jira KLTB002-6387 - Added method to unlink a WeChat account

#### Request enable location

Jira KLTB002-6320 Reconnection with location off

Scenario
- User paired a toothbrush in a previous session
- User disabled Location after closing the app
- User opens the app

Result: can't connect to toothbrush
Reason: we scan before attempting to connect. We need location to be enabled before we can start to scan.

Changes to behavior and code
1. KolibreeServiceActivity invokes `onEnableLocationActionNeeded()` when it detects that it can't establish a connection
due to location disabled or location permission revoked
2. LocationStatus can be used at any point to check if any action is needed

#### Demo app

Demonstrate how to use `onEnableLocationActionNeeded()` in BaseActivity
Demonstrate how to use LocationStatus in ScanActivity and BaseActivity

See BrushingFragment for phone number and WeChat links demonstration and usage samples


## [4.4.0]

### Enhancements

Jira KLTB002-6350 - Add Test Brushing to SDK

#### Demo app

Jira KLTB002-6350 - Show how to launch Test Brushing

### Breaking Changes
1. Bump AppCompat dependency to 1.1.0-alpha
This might break your code.

2. PairingAssistant
- Remove isScanning. We now launch a scan per device, so it no longer makes sense
- scanFor requires a SpecificToothbrushScanCallback 
- startScan requires a AnyToothbrushScanCallback 
- stopScan requires AnyToothbrushScanCallback or SpecificToothbrushScanCallback

3. Non public code obfuscation. The non public code is now obfuscated

### Bugfixes

Jira KLTB002-5060 - Fix toothbrush disconnects in Coach and Coach+ 
Jira KLTB002-5979 - Improve toothbrush not detected due to too frequent scans
Jira KLTB002-6252 - After choosing a music file as the sound/music effect, the file name is not
                    displayed in the settings. But there is music playing when Coach+ is triggered
Jira KLTB002-6250 - Trigger account synchronisation after login using SMS code 
Jira KLTB002-6449 - Support blinking in M1 with firmware >= 1.4.0
Jira KLTB002-6356 - Fix infinite connection loop if loadSensorCalibration fails

## [4.3.2]

### Breaking Changes

Jira KLTB002-1902 - Battery related methods have been move to toothbrush().battery() with M1 support

### Enhancements

Jira KLTB002-5970 - Support Encrypted OTA
Jira KLTB002-6130 - Add 6PO endpoints
Jira KLTB002-6104 - Support custom endpoint through secret settings
Jira KLTB002-6192 - Improve the user_agent send to the backend for Android (OS version and API level added)

### Bugfixes

Jira KLTB002-5970 - OTA fails at 95%
Jira KLTB002-5970 - Encrypted OTA fails if there's a timeout in one of the objects
Jira KLTB002-5945 - Fixed Avro toothbrush model and transition table for Coach+

## [4.3.1]

### Breaking Changes

### Enhancements

Jira KLTB002-5724 - Improved Coach+ and Checkup rendering time and memory footprint

### Bugfixes


## [4.3.0]

/!\ Make sure the GoPirate game is disabled for Android 5 (unknown Unity issue)

### Breaking Changes

* Migration to AndroidX! https://developer.android.com/jetpack/androidx/migrate

1. Use Android Studio's Refactor > Migrate to AndroidX
2. Upgraded dependencies. They may or may not apply to your project.
- Upgrade dagger to 2.21
- Upgrade butterknife to 10.0.0
- Upgrade rxjava to 2.2.6
- Upgrade androidx.lifecycle to 2.0.0
- Upgrade appcompat to to 1.0.2
- Upgrade recyclerview to to 1.0.0
- Upgrade cardview to to 1.0.0
- Upgrade com.google.android.material to to 1.0.0
- Upgrade constraintlayout to to 1.1.3

Hopefully you don't run into issues 

* brushingFacade.deleteBrushing(brushing) no longer needs the profileId parameter
* IProfile moved to com.kolibree.android.accountinternal.profile.models.IProfile
* Some other classes have been moved, but they didn't break DemoApp.
* User interface methods have been changed

### Enhancements

Jira KLTB002-5469 - Improve toothbrush notification behaviour and text

#### IBrushing

- Add the profileId associated to the brushing

#### BrushingFacade

- deleteBrushing does not need profileId anymore : deleteBrushing(brushing: IBrushing)

#### Demo app
- Preload 3D models used in Coach+ to speed up Coach+ launch. See HeaderActivity.init changes

### Bugfixes

Jira KLTB002-5624 - Unable to access the Brusing data owner from SDK after a brushing session
Jira KLTB002-5723 - deleteProfile method returning error profileInternal.Field_stats is null
Jira KLTB002-5621 - Unable to delete brushing data
Jira KLTB002-5725 - Fix brushings duplicate if we disconnect after brushing session
Jira KLTB002-5141 - Fix some OTA issues

## [4.2.3]

### Bugfixes

Jira KLTB002-5650 - Fixed V1 not reconnecting when the service restarts

## [4.2.2]

### Bugfixes

Jira KLTB002-5508 - Clear stats table on logout
Jira KLTB002-4993 - Fix some E2 resources (new strings, rotated image, video and gif)
Jira KLTB002-5521 - Don't notify connection ready unless all setup commands succeed

## [4.2.1]

### Breaking Changes

Jira KLTB002-5337 - All non public code is now obfuscated

### Bugfixes

Jira KLTB002-5526 - Expose deleteAccount
Jira KLTB002-5525 - Coach+ brushing data is showing up twice
Jira KLTB002-5476 - Unity issues when Pirate and Umeng library are in the same project
Jira PSCI-72 - ANR in kolibree.com.demoapp

## [4.2.0]

### Bugfixes

Jira 5410 - Fixed crash when synchronizing a profile on two phones


## [4.1.1]

### Breaking Changes
CoachPlusActivity now exposes 2 ways to start it 

    public static Intent createConnectedCoach

    public static Intent createManualCoach

This helps in supporting manual mode if paired toothbrush is not connected

#### Demo APP

Showcase usage of new CoachPlusActivity initializers
Handling CheckupData -1 as surface (CheckupFragment)

### Bugfixes

Jira 5017 - Fixed User-Agent header value in all HTTP calls
Jira 4744 - Support profiles deleted remotely. If the active profile was removed, the app crashed.
Jira 5117 - Support Coach+ manual mode even if user has toothbrush
Jira 5298 - Offline brushings are not synched
Jira 5307 - Android : Switching profiles added in phone A, is causing the SDK to error out when displayed in phone B
Jira 5306 - Android : Issue with the profile photos not being displayed
Jira 5300 - Updated wording from POEditor
Jira 5339 - Added backendId to the IBrushing interface
Jira 5333 - Account class now provides WeChat data

## [4.1.0] 

### Breaking Changes

Toothbrush's update method parameter is now AvailableUpdate type

### Enhancements

New operations to listen to updates
Moved the AvailableUpdate class to the commons module
Changed the way the version and crc are got and checked

#### AccountFacade

    fun activeProfileFlowable(): Flowable<IProfile>
    
    Emits the active profile, as well as future active profile changes

#### BrushingFacade

    fun brushingsFlowable(profileId: Long): Flowable<List<IBrushing>>
    
    Emits all the  brushings stored locally for a given profile, associated by its profile
    Id. 
    
    If the brushings database is updated, the [Flowable] will emit a new [List]
    

    fun getLastBrushingSessionFlowable(profileId: Long): Flowable<IBrushing>    
    
    Get the latest brushing sessions for a profile
     
    Whenever there are changes to brushing database, it emits the new last brushing session
    if it is different from the previous one
    
    If there is no existing brushing for this user, it won't emit anything

#### Demo APP

Added how to use activeProfileFlowable() in HeaderActivity and DashboardFragment
Added how to use getLastBrushingSessionFlowable() and brushingsFlowable() in DashboardFragment
    
### Bugfixes

Jira 4972 - Brushing data is not being displayed for profiles in an account

## [4.0.9] 

### Breaking Changes

### Enhancements

### Bugfixes

Jira 4931 - Fix M1 OTA issues


## [4.0.8] 2018-12-14

### Breaking Changes

### Enhancements

### Bugfixes

Jira 5102 - Fixed raw data stream crash on Vivo Y79


## [4.0.7] 2018-12-04

### Breaking Changes

### Enhancements

#### Offline Brushings module
* Expose SDKOrphanBrushingRepository

#### Demo APP
* Add a complete example of Orphan Brushing reading/management to SDKDemo app. See CheckupFragment.countOrphanBrushings and OrphanBrushingsActivity

### Bugfixes

### Notes

Steps to generate an Orphan brushing

0. Pair to a toothbrush
0. Go to Toothbrush screen
0. Click on Toothbrush User
0. Set Shared Toothbrush
0. Press back twice to exit app
0. Wait for 40 seconds
0. Do an offline brushing, at least 25 seconds
0. Open Demo app. Once it connects, you should be notified that you synched one offline brushing
0. Navigate to Checkup tab
0. There's a new icon at the bottom right corner. It should show a red circe indicating that there are new orphan brushings.
0. Click on the icon, which takes you to OrphanBrushingsActivity
0. See the OrphanBrushing. Now you can delete it or assign it
0. Delete, it's removed from the database
0. Assign, it creates a remote brushing


## [4.0.6] 2018-11-29

### Breaking Changes

### Enhancements

#### Account module
* Add method "setActiveProfile" to set the active profile. to call when switching account.

### Bugfixes

#### General
* Error message should be in chinese when the language of the phone is chinese (zh). (https://jira.kolibree.com/browse/KLTB002-4730)

#### Demo APP
* Add setActiveProfile when switching account
* Reload the view after sync the data
* The code has been updated in the demo app. It's not a SDK issue.
* https://jira.kolibree.com/browse/KLTB002-4972 → probably because of the setActiveProfile and the fact the view is not updated correctly after sync the brushing of the current profile.


## [4.0.5] 2018-11-20

### Breaking Changes

### Enhancements

#### Connectivity
* Improvements made to Connectivity

#### Account module
* Edit birthday on profile
* Now possible to upload picture when creating account

### Bugfixes

#### Coach + game
* General bug fixes

#### Account module
* WeChat fixes

#### Demo APP
* Brushing data is not displayed for profiles : Show how to switch between profiles. (ProfilesListActivity.java shows how to create a new profile with a profile picture AND how to switch account and reload the data of the homepage)
* Display Error message from API request : check ApiError.java.
* Show how to display empty Jaws (with .empty() in jaw object )


## [4.0.4] 2018-10-31

### Breaking Changes

### Enhancements

#### Pirate Game
* New version, with some bug fixes and performance improvement.

### Bugfixes

#### Connectivity
* Bug fixes

#### Coach + game
* Bug fix : Cant quit a session after the  bluetooth gets off
* General bug fixes


## [4.0.2] 2018-10-25

### Breaking Changes

#### Brushing  Module
* IBrushing : remove "quality" attribute

#### Account  Module
* IProfile : remove abstract method "getAgeFromBirthday" (compute the age internally)

### Enhancements

#### Jaws Module
* Add method empty() to display the mouth zone with no data

#### Auth  Module
* display error message when auth / sms validation is incorrect

#### Brushing  Module
* IBrushing : Add "game" attribute
* BrushingFacade : add method “getQualityBrushing()“ to get the quality of a brushing

#### Demo App
* Add sync brushing to get the list of brushings.
* optimisation usage of the popup displayed when there is an update

### Bugfixes


## [4.0.1] 2018-10-19

### Breaking Changes

#### Demo App
* Rename ProfileFacade by AccountFacade.

#### Profile module (now Account module)
* Rename ProfileFacade by AccountFacade (com.kolibree.account.AccountFacade) (BREAKING CHANGE)
* IProfile is now in com.kolibree.sdkws.profile.models.IProfile (BREAKING CHANGE)

### Enhancements

#### Demo App
* Update import IProfile.

#### Profile module (now Account module)
* add Logout Method. It also clean the local DB and the cache.
* add getAccount method. It includes the publicId, backendIf, phone number and ownerProfileId.

### Bugfixes

#### Demo App
* Fix bug in the demo app, now OTA is working. (please update your OtaCheckerManager.java )


## [4.0.0] 2018-10-17

### Breaking Changes

### Enhancements

#### Pairing module 
* Add viewModel "VibrationCheckerViewModel", used to detect when a toothbrush vibrates.

#### Toothbrush module 
* Major Bluetooth connectivity improvement on huawei

#### Profile module
* Add method  "getPrivateAccessToken" to get the access token for account linking
* Add method  "loginWithWechat" 
* Add method "registerWithWechat"
* Add method "getPublicId"
* IProfile : add country attribute

#### Coach + 
* add settings

### Bugfixes

#### Coach + 
* Major bug fixes


## [3.0.0] 2018-10-04

### Breaking Changes

### Enhancements

#### Core module
* Integration Pirate game
* Integration Toothbrush UI update module

#### Toothbrush module 
* Major Bluetooth connectivity improvement

### Bugfixes

#### Coach + 
* Some bug fixes


## [1.0.2] 2018-09-27

### Breaking Changes

### Enhancements

#### Demo app
* Usage of the core SDK 1.0.2
* Update profile picture by clicking on the avatar and pick an image from the gallery
* Update of the CredentialsModule in the demoApp (please use the same)
* Update of the DemoAppComponent in the demoApp (please use the same)
* Integration of the Pirate game (still in draft, do not copy the code now)
* Integration of the Toothbrush UI update module (still in draft, do not copy the code now)

#### Toothbrush module
* Minor improvements on Huawei 10

#### Core module
* Integration Pirate game
* Integration Toothbrush UI update module

#### Profile module
* Add method to upload an image in the Profile module
* Added changePicture method in the ProfileFacade.kt
* Add picturePath in the IProfile interface

### Bugfixes

#### Coach + 
* Some bug fixes


## [1.0.0] 2018-09-10

### Breaking Changes

#### Brushing module
* BrushingManagerWrapper has been renamed BrushingFacade

#### Profile module
* ProfileManagerWrapper has been renamed ProfileFacade
* Remove login method from the ProfileFacade interface. You will need to use the authentication module to login and create and account.

#### Toothbrush module
* ToothbrushWrapper has been renamed ToothbrushFacade

#### Core module
* include all the modules contained in the SDK and all the dependencies for those to work, so you don't have to ! 
* From now on, you will only need to include the core module into your app to get access to all the features.
* Including the core SDK in your app means you need to update you current app and :
* Remove all the others dependencies and add just the core module  (there is a documentation about it) in your gradle file.
* Remove "kolibree_staging_server" from your manifest file and the value associated to it.
* Remove all the modules from the Component file and add just the core module (please check the documentation and the demo app to get more details).
* Update of the client_id and client_secret for staging.
* Provide your client_id and secret to the core module.

### Enhancements

* Add more detailed comments to documentation

#### Demo App
* Integration of the core SDK
* Display the toothbrush settings once paired
* Implementation of the offline and orphan brushings module
* Implementation of the SMS auth to login/create an account
* Auto reconnection to the toothbrush after re-opening the app.
* Add fragments to shoe usage of Dagger in fragments
* Improvement of the last brushing session view.

#### Integration of the offline and orphan brushings module
* This module help you to sync the brushings to your profile from your toothbrush. Please refer to the documentation for more infos.

#### Integration of the phone number auth module
* This module help you to create an account or login using your phone number. Please refer to the documentation for more infos.

#### Integration of the coach+ module

### Bugfixes
