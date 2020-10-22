/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import androidx.test.espresso.intent.Intents;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.guidedbrushing.mvi.GuidedBrushingActivity;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.vibrator.VibratorListener;
import com.kolibree.android.test.mocks.KLTBConnectionBuilder;
import com.kolibree.android.test.mocks.ProfileBuilder;
import com.kolibree.android.test.utils.AppMocker;
import com.kolibree.android.test.utils.SdkBuilder;
import io.reactivex.subjects.BehaviorSubject;
import org.junit.Test;

public class HomeScreenActivityGameLaunchEspressoTest extends HomeScreenActivityEspressoTest {

  @SuppressWarnings("KotlinInternalInJava")
  @Test
  public void vibration_doesNotStartCoachPlusForM1_startsCoachPlusForE1() {
    when(component().profileUtils().isAllowedToBrush()).thenReturn(true);

    BehaviorSubject<VibratorListener> m1VibratorSubject = BehaviorSubject.create();
    KLTBConnection m1Connection =
        KLTBConnectionBuilder.createWithDefaultState()
            .withModel(ToothbrushModel.CONNECT_M1)
            .withOwnerId(PROFILE_ID)
            .withListenerInterception(m1VibratorSubject)
            .build();

    BehaviorSubject<VibratorListener> e1VibratorSubject = BehaviorSubject.create();
    KLTBConnection e1Connection =
        KLTBConnectionBuilder.createWithDefaultState()
            .withListenerInterception(e1VibratorSubject)
            .withModel(ToothbrushModel.CONNECT_E1)
            .withOwnerId(PROFILE_ID)
            .build();

    SdkBuilder sdkBuilder =
        SdkBuilder.create()
            .withActiveProfile(
                ProfileBuilder.create().withName(PROFILE_NAME).withId(PROFILE_ID).build())
            .withKLTBConnections(m1Connection, e1Connection)
            .prepareForMainScreen();

    AppMocker.create()
        .withSdkBuilder(sdkBuilder)
        .withLocationPermissionGranted(true)
        .withLocationEnabled(true)
        .withMockedShopifyProducts()
        .prepareForMainScreen()
        .mock();

    launchActivity();

    try {
      Intents.init();

      assertFalse(m1VibratorSubject.hasObservers());

      intended(hasComponent(GuidedBrushingActivity.class.getName()), times(0));

      Instrumentation.ActivityResult intentResult =
          new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, new Intent());
      intending(hasComponent(GuidedBrushingActivity.class.getName())).respondWith(intentResult);

      assertFalse(e1VibratorSubject.hasObservers());

      intended(hasComponent(GuidedBrushingActivity.class.getName()), times(0));
    } finally {
      Intents.release();
    }
  }
}
