# Changelog for hum by Colgate app

All changes to the hum by Colgate Android app are documented below.

All new versions adhere to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
- Increment **MAJOR** version when massive, app-wide changes were introduced (like app rebranding, totally new UX) - needs approal from the product team,
- Increment **MINOR** version when new functionalities were added to an existing app, and
- Increment **PATCH** version when only bug fixes were added.

To generate this file, use [Jira Releases page](https://kolibree.atlassian.net/projects/KLTB002?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page).
You can use [Release notes](https://kolibree.atlassian.net/secure/ReleaseNote.jspa?projectId=10103&version=10403) functionality to help you with that.

After release, please push changes to [Google Doc](https://docs.google.com/document/d/1q1ikuVXp1Ag-PEM5U7ZnAtP2lPsA43o3ANGFUFgZ7kQ/edit#heading=h.blfb2jcvue9t) to make them public.

## 1.2.0 RC2

### Bug fixes

- [[KLTB002-13194](https://kolibree.atlassian.net/browse/KLTB002-13194)] - Fix Brushing reminder notifications

### Enhancements

- [[KLTB002-13193](https://kolibree.atlassian.net/browse/KLTB002-13193)] - Pressing the power button does not start GuidedBrushing anymore
- [[KLTB002-13193](https://kolibree.atlassian.net/browse/KLTB002-13193)] - OfflineBrushing can be retrieve even when it has been created
while connected to the app


## 1.2.0 RC1

### Enhancements

- [[KLTB002-11428](https://kolibree.atlassian.net/browse/KLTB002-11428)] - [Toothbrush Settings} Add DSP section
- [[KLTB002-11642](https://kolibree.atlassian.net/browse/KLTB002-11642)] - [Toolbox] Pulsing dot explanation screen after smile counter animation
- [[KLTB002-12090](https://kolibree.atlassian.net/browse/KLTB002-12090)] - Settings - Rate the app
- [[KLTB002-12230](https://kolibree.atlassian.net/browse/KLTB002-12230)] - Make sure there is no regression on Android 11
- [[KLTB002-12579](https://kolibree.atlassian.net/browse/KLTB002-12579)] - FIX location scan fail on Android 11
- [[KLTB002-12609](https://kolibree.atlassian.net/browse/KLTB002-12609)] - Add missing analytics part 2
- [[KLTB002-12616](https://kolibree.atlassian.net/browse/KLTB002-12616)] - Brush reminders - UI
- [[KLTB002-12630](https://kolibree.atlassian.net/browse/KLTB002-12630)] - FIX profileTab on android 11
- [[KLTB002-12635](https://kolibree.atlassian.net/browse/KLTB002-12635)] - Send Amazon's OAuth Token to backend
- [[KLTB002-12653](https://kolibree.atlassian.net/browse/KLTB002-12653)] - Brush reminders - logic
- [[KLTB002-12658](https://kolibree.atlassian.net/browse/KLTB002-12658)] - Add section for amazon dash to settings
- [[KLTB002-12678](https://kolibree.atlassian.net/browse/KLTB002-12678)] - Create a proper splashscreen to avoid red screen
- [[KLTB002-12696](https://kolibree.atlassian.net/browse/KLTB002-12696)] - Notify backend about brushhead replacement
- [[KLTB002-12708](https://kolibree.atlassian.net/browse/KLTB002-12708)] - Receive AWS DRS link from the BE
- [[KLTB002-12730](https://kolibree.atlassian.net/browse/KLTB002-12730)] - Fix GuidedBrushingEspressoTest
- [[KLTB002-12798](https://kolibree.atlassian.net/browse/KLTB002-12798)] - KML new metric integration
- [[KLTB002-12818](https://kolibree.atlassian.net/browse/KLTB002-12818)] - Finalize game middleware integration
- [[KLTB002-12837](https://kolibree.atlassian.net/browse/KLTB002-12837)] - [TIMEBOXED] DRS issues
- [[KLTB002-12839](https://kolibree.atlassian.net/browse/KLTB002-12839)] - Migrate login deep links
- [[KLTB002-12841](https://kolibree.atlassian.net/browse/KLTB002-12841)] - Send battery data to backend
- [[KLTB002-12844](https://kolibree.atlassian.net/browse/KLTB002-12844)] - Send existing users brush head data to backend
- [[KLTB002-12854](https://kolibree.atlassian.net/browse/KLTB002-12854)] - Finalize DRS integration for the 3 core features
- [[KLTB002-12857](https://kolibree.atlassian.net/browse/KLTB002-12857)] - Read brush head replacement date from backend after pairing TB
- [[KLTB002-12878](https://kolibree.atlassian.net/browse/KLTB002-12878)] - Remove/hide "Get the most out of your data" section in profile settings
- [[KLTB002-12931](https://kolibree.atlassian.net/browse/KLTB002-12931)] - New mechanism for managing KolibreeService
- [[KLTB002-12945](https://kolibree.atlassian.net/browse/KLTB002-12945)] - [Timeboxed] Amazon DRS change requests
- [[KLTB002-12982](https://kolibree.atlassian.net/browse/KLTB002-12982)] - Add support for tabbed sections in Shop fragment

### Bug fixes

- [[KLTB002-12547](https://kolibree.atlassian.net/browse/KLTB002-12547)] - Mandatory update dialog is displayed multiple times
- [[KLTB002-12646](https://kolibree.atlassian.net/browse/KLTB002-12646)] - Was logged out for no reason after pressing Home tab
- [[KLTB002-12647](https://kolibree.atlassian.net/browse/KLTB002-12647)] - No smile points celebration anymore when I earn points, and no '+2 points' text... tried several times
- [[KLTB002-12662](https://kolibree.atlassian.net/browse/KLTB002-12662)] - JV issue: Phone gets very hot when doing coach plus with G2 brush.
- [[KLTB002-12711](https://kolibree.atlassian.net/browse/KLTB002-12711)] - ANR when add a toothbrush
- [[KLTB002-12751](https://kolibree.atlassian.net/browse/KLTB002-12751)] - Crash in HumHomeNavigatorViewModel
- [[KLTB002-12771](https://kolibree.atlassian.net/browse/KLTB002-12771)] - 3D jaws are drawn on top of each other
- [[KLTB002-12802](https://kolibree.atlassian.net/browse/KLTB002-12802)] - Not friendly Error message
- [[KLTB002-12803](https://kolibree.atlassian.net/browse/KLTB002-12803)] - The jaw looks a bit squeezed vertically
- [[KLTB002-12807](https://kolibree.atlassian.net/browse/KLTB002-12807)] - Doesn't reconnect to my brush
- [[KLTB002-12923](https://kolibree.atlassian.net/browse/KLTB002-12923)] - Fix "Something wrong happened" error message in Android toothbrush settings
- [[KLTB002-12954](https://kolibree.atlassian.net/browse/KLTB002-12954)] - The toothbrush won't connect anymore
- [[KLTB002-12955](https://kolibree.atlassian.net/browse/KLTB002-12955)] - [Brushing Program] An infinite loop loading is displayed when clicking on "Try it now" CTA
- [[KLTB002-13057](https://kolibree.atlassian.net/browse/KLTB002-13057)] - Switch app button redirects to the wrong app

### Internal

- [[KLTB002-10205](https://kolibree.atlassian.net/browse/KLTB002-10205)] - Integrate avro into GameMiddleware
- [[KLTB002-10210](https://kolibree.atlassian.net/browse/KLTB002-10210)] - Integrate new game middleware and loader version in Android app
- [[KLTB002-11040](https://kolibree.atlassian.net/browse/KLTB002-11040)] - Refactor UnityPlayerLifecycleActivity to use MVI
- [[KLTB002-10366](https://kolibree.atlassian.net/browse/KLTB002-10366)] - HW and FW versions are not always reported to the backend
- [[KLTB002-12974](https://kolibree.atlassian.net/browse/KLTB002-12974)] - Lint: Force SerializedName on public fields used as @Body Retrofit

## 1.1.0

### New features

- [[KLTB002-10512](https://kolibree.atlassian.net/browse/KLTB002-10512)] - [Stats] In-app vs Offline, User with brushings Card
- [[KLTB002-11573](https://kolibree.atlassian.net/browse/KLTB002-11573)] - Add help screen to settings
- [[KLTB002-11694](https://kolibree.atlassian.net/browse/KLTB002-11694)] - Add sync notifications
- [[KLTB002-11699](https://kolibree.atlassian.net/browse/KLTB002-11699)] - [More ways to earn points] - Synchronize brushing sync reminders
- [[KLTB002-12139](https://kolibree.atlassian.net/browse/KLTB002-12139)] - [More ways to earn points] - React to Challenge completed
- [[KLTB002-12271](https://kolibree.atlassian.net/browse/KLTB002-12271)] - Create low battery popup
- [[KLTB002-12272](https://kolibree.atlassian.net/browse/KLTB002-12272)] - Create "replace toothbrush" popup

### New features (hidden behind feature toggles)

- [[KLTB002-12612](https://kolibree.atlassian.net/browse/KLTB002-12612)] - Get OAuth Token from Amazon
- [[KLTB002-12613](https://kolibree.atlassian.net/browse/KLTB002-12613)] - Create amazon dash screen
- [[KLTB002-12614](https://kolibree.atlassian.net/browse/KLTB002-12614)] - Add new MWTEP challenge for Amazon Dash
- [[KLTB002-12636](https://kolibree.atlassian.net/browse/KLTB002-12636)] - Handle errors while getting OAuth Token from Amazon
- [[KLTB002-12610](https://kolibree.atlassian.net/browse/KLTB002-12610)] - Celebration screen for amazon

### Enhancements

- [[KLTB002-12323](https://kolibree.atlassian.net/browse/KLTB002-12323)] - [Timeboxed] Reduce jankiness
- [[KLTB002-11698](https://kolibree.atlassian.net/browse/KLTB002-11698)] - Handle "user disabled notifications" case when user opts in
- [[KLTB002-12273](https://kolibree.atlassian.net/browse/KLTB002-12273)] - Investigate/work on mandatory update
- [[KLTB002-12328](https://kolibree.atlassian.net/browse/KLTB002-12328)] - Finetune Hum 3D Jaws
- [[KLTB002-12560](https://kolibree.atlassian.net/browse/KLTB002-12560)] - Update Guided brushing algo file & KPI limits
- [[KLTB002-12608](https://kolibree.atlassian.net/browse/KLTB002-12608)] - Add screen names
- [[KLTB002-12586](https://kolibree.atlassian.net/browse/KLTB002-12586)] - Update of weight file for B1 and E2

### Bugfixes

- [[KLTB002-10527](https://kolibree.atlassian.net/browse/KLTB002-10527)] - Picture is replaced when logging in with Google
- [[KLTB002-12013](https://kolibree.atlassian.net/browse/KLTB002-12013)] - Toothbrush settings UI not updated after update
- [[KLTB002-12117](https://kolibree.atlassian.net/browse/KLTB002-12117)] - Additional offline brushing after guided brushing
- [[KLTB002-12306](https://kolibree.atlassian.net/browse/KLTB002-12306)] - Infinite calculating points
- [[KLTB002-12330](https://kolibree.atlassian.net/browse/KLTB002-12330)] - Wrong display in calendar
- [[KLTB002-12341](https://kolibree.atlassian.net/browse/KLTB002-12341)] - Show 3D jaw for manual brushings on Last Brushing card
- [[KLTB002-12543](https://kolibree.atlassian.net/browse/KLTB002-12543)] - FIX missing smiles history event
- [[KLTB002-12553](https://kolibree.atlassian.net/browse/KLTB002-12553)] - Offline brushings do not show checkup after retrieval
- [[KLTB002-12587](https://kolibree.atlassian.net/browse/KLTB002-12587)] - MWTEP Challenge completion screen sometimes not appearing
- [[KLTB002-12642](https://kolibree.atlassian.net/browse/KLTB002-12642)] - 'Get the most of your data' toggle goes back to on automatically
- [[KLTB002-12645](https://kolibree.atlassian.net/browse/KLTB002-12645)] - Smile points conversion is not showing properly on the reward yourself section
- [[KLTB002-12650](https://kolibree.atlassian.net/browse/KLTB002-12650)] - Frequency chart popup becomes white after leaving the app
- [[KLTB002-12713](https://kolibree.atlassian.net/browse/KLTB002-12713)] - Drawable on release are wrong

## [1.0.1](https://github.com/kolibree-git/android-monorepo/releases/tag/1.0.1-HumRC1)

### Bugfixes

- [[KLTB002-12053](https://kolibree.atlassian.net/browse/KLTB002-12053)] - CLONE - When i change date to 30 June, guided brushing is not paused by powering off the brush anymore
- [[KLTB002-12555](https://kolibree.atlassian.net/browse/KLTB002-12555)] - Offline brushing retrieval crash when device clock has been set to a past date

## [1.0.0](https://github.com/kolibree-git/android-monorepo/releases/tag/1.0.0-HumRC5)

### Enhancements

- [[KLTB002-12283](https://kolibree.atlassian.net/browse/KLTB002-12283)] - [Question of the Day] If question has already been answered handle sendAnswer error code
- [[KLTB002-11701](https://kolibree.atlassian.net/browse/KLTB002-11701)] - [More ways to earn points] - Complete your profille
- [[KLTB002-11709](https://kolibree.atlassian.net/browse/KLTB002-11709)] - [Mind your speed] - zones progress
- [[KLTB002-11752](https://kolibree.atlassian.net/browse/KLTB002-11752)] - [Mind your speed] - Logic
- [[KLTB002-12092](https://kolibree.atlassian.net/browse/KLTB002-12092)] - [More ways to earn points] - Complete your profille black box
- [[KLTB002-12178](https://kolibree.atlassian.net/browse/KLTB002-12178)] - [Smiles History] integrate short task and brushing correctly

### Bugfixes

- [[KLTB002-10777](https://kolibree.atlassian.net/browse/KLTB002-10777)] - Avatar photo is badly rotated after taking a picture
- [[KLTB002-11963](https://kolibree.atlassian.net/browse/KLTB002-11963)] - [Onboarding - Create your profile] The back arrow is not correctly displayed
- [[KLTB002-12107](https://kolibree.atlassian.net/browse/KLTB002-12107)] - Home content displayed, but Activities tab is highlighted
- [[KLTB002-12191](https://kolibree.atlassian.net/browse/KLTB002-12191)] - Shop sends product's variant ID in tag name
- [[KLTB002-12263](https://kolibree.atlassian.net/browse/KLTB002-12263)] - Navigation dot not correctly displayed on Tablets
- [[KLTB002-12278](https://kolibree.atlassian.net/browse/KLTB002-12278)] - Add missing analytics
- [[KLTB002-12305](https://kolibree.atlassian.net/browse/KLTB002-12305)] - Unable to recover after interrupted OTA update
- [[KLTB002-12321](https://kolibree.atlassian.net/browse/KLTB002-12321)] - `javax.net.ssl.SSLException` when LocalAvatarCache download avatar with FileDownloader
- [[KLTB002-12351](https://kolibree.atlassian.net/browse/KLTB002-12351)] - Set Gender to Unknown when creating an account
- [[KLTB002-12531](https://kolibree.atlassian.net/browse/KLTB002-12531)] - Hide lifetime points when equal to current points
- [[KLTB002-12553](https://kolibree.atlassian.net/browse/KLTB002-12553)] - Offline brushings do not show checkup after retrieval
