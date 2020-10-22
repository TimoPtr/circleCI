Test Brushing
=======

Test Brushing activity


# Providing Translations

If you whish to change the text displayed in TestBrushing,
you can provide your own TranslationsProvider when initializing the component

```
private void initDagger() {
    TranslationsProvider translationsProvider = new TranslationsProvider();
    
    Map<String, String> map = new HashMap<>();
    chinaMap.put(INTRO_BRUSHING_TIP, "Chinese message");
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

/*
Screen 1
 */
- INTRO_BRUSHING_TIP
- INTRO_INSTRUCTIONS_1
- INTRO_INSTRUCTIONS_2
- INTRO_INSTRUCTIONS_3
- INTRO_BUTTON

/*
Screen 2
 */

- SESSION_TOP_TEXT
- SESSION_BOTTOM_TEXT

/*
Screen 3
 */

- DURING_SESSION_STEP1_DESCRIPTION_TEXT
- DURING_SESSION_STEP1_HIGHLIGHTED_TEXT

/*
Screen 4
 */

- DURING_SESSION_STEP2_DESCRIPTION_TEXT

/*
Screen 5
 */

- DURING_SESSION_STEP3_DESCRIPTION_TEXT
- DURING_SESSION_STEP3_HIGHLIGHTED_TEXT
- DURING_SESSION_STEP3_MANUAL_DESCRIPTION_TEXT
- DURING_SESSION_STEP3_MANUAL_HIGHLIGHTED_TEXT

/*
Screen 6
 */

- DURING_SESSION_STEP4_DESCRIPTION_TEXT
- DURING_SESSION_STEP4_HIGHLIGHTED_TEXT

/*
Popup 7
 */

- POPUP_TITLE
- POPUP_CONFIRM
- POPUP_RESUME

/*
Screen 8
 */

- OPTIMIZE_TITLE
- OPTIMIZE_HANDEDNESS_QUESTION
- OPTIMIZE_BRUSHINGS_PER_DAY_QUESTION
- OPTIMIZE_BUTTON

/*
Screen 9
 */

- ANALYSIS_IN_PROGRESS_TITLE
- ANALYSIS_IN_PROGRESS_MESSAGE_1
- ANALYSIS_IN_PROGRESS_MESSAGE_2
- ANALYSIS_IN_PROGRESS_MESSAGE_3
- ANALYSIS_IN_PROGRESS_MESSAGE_4

/*
Screen 10
 */
- RESULTS_PAGE1_TITLE
- RESULTS_PAGE1_BODY
- RESULTS_PAGE1_HAND

/*
Screen 11
 */
- RESULTS_PAGE2_TITLE
- RESULTS_PAGE2_BODY_PERFECT
- RESULTS_PAGE2_BODY_GOOD
- RESULTS_PAGE2_BODY_MEDIUM
- RESULTS_PAGE2_BODY_BAD
- RESULTS_PAGE2_BODY_NO_DATA
- RESULTS_PAGE2_CLEAN
- RESULTS_PAGE2_DIRTY

/*
Popup 12
 */
- READ_SCHEMA_TITLE
- READ_SCHEMA_BODY
- READ_SCHEMA_TOP
- READ_SCHEMA_BOTTOM
- READ_SCHEMA_RIGHT
- READ_SCHEMA_LEFT
