/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode;
import com.kolibree.android.sdk.connection.brushingmode.BrushingModePerProfileRepository;
import com.kolibree.android.sdk.connection.brushingmode.ProfileBrushingMode;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.test.extensions.TrustedClockExtensionsKt;
import com.kolibree.android.test.mocks.KLTBConnectionBuilder;
import com.kolibree.android.test.mocks.ProfileBuilder;
import com.kolibree.android.test.utils.ActivityUtils;
import com.kolibree.android.test.utils.SdkBuilder;
import org.junit.Test;
import org.threeten.bp.OffsetDateTime;

@SuppressWarnings("KotlinInternalInJava")
public class BrushingProgramActivityEspressoTest
    extends BrushingProgramActivityBaseEspressoBaseTest {
  private static final int TOTAL_SCREENS = 3;

  @Test
  public void brushingProgram_navigation() {
    getActivityTestRule().launchActivity(createLaunchBrushingQuizActivityIntent());

    waitForQuizPager();

    titleIsDisplayed(R.string.brushing_quiz_screen_1_title);

    answerIsDisplayed(R.string.brushing_quiz_screen_1_answer_1);
    answerIsDisplayed(R.string.brushing_quiz_screen_1_answer_3);
    selectAnswer(R.string.brushing_quiz_screen_1_answer_2);

    titleIsDisplayed(R.string.brushing_quiz_screen_2_title);

    answerIsDisplayed(R.string.brushing_quiz_screen_2_answer_2);
    answerIsDisplayed(R.string.brushing_quiz_screen_2_answer_3);
    selectAnswer(R.string.brushing_quiz_screen_2_answer_1);

    titleIsDisplayed(R.string.brushing_quiz_screen_3_title);

    answerIsDisplayed(R.string.brushing_quiz_screen_3_answer_1);
    answerIsDisplayed(R.string.brushing_quiz_screen_3_answer_2);
    answerIsDisplayed(R.string.brushing_quiz_screen_3_answer_3);

    pressBack();

    titleIsDisplayed(R.string.brushing_quiz_screen_2_title);

    pressBack();

    titleIsDisplayed(R.string.brushing_quiz_screen_1_title);
  }

  @Test
  public void brushingProgram_confirmationScreen_revert_finishesActivity() {
    getActivityTestRule().launchActivity(createLaunchBrushingQuizActivityIntent());

    waitForQuizPager();

    selectAnswer(R.string.brushing_quiz_screen_1_answer_2);

    selectAnswer(R.string.brushing_quiz_screen_2_answer_1);

    selectAnswer(R.string.brushing_quiz_screen_3_answer_3);

    onView(withId(R.id.quiz_confirmation_reset)).check(matches(isDisplayed())).perform(click());

    ActivityUtils.assertActivityIsFinished(getActivity());

    ActivityUtils.assertResult(getActivityTestRule(), RESULT_CANCELED);
  }

  @Test
  public void brushingProgram_confirmationScreen_confirm_setsBrushingModeAndFinishesActivity() {
    TrustedClockExtensionsKt.setFixedDate(TrustedClock.INSTANCE);
    OffsetDateTime expectedOffsetDateTime = TrustedClock.getNowOffsetDateTime();

    KLTBConnection ce2Active =
        KLTBConnectionBuilder.createWithDefaultState()
            .withMac("1")
            .withModel(ToothbrushModel.CONNECT_E2)
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode()
            .build();

    KLTBConnection ce2NonActive =
        KLTBConnectionBuilder.createWithDefaultState()
            .withMac("2")
            .withModel(ToothbrushModel.CONNECT_E2)
            .withState(KLTBConnectionState.ESTABLISHING)
            .withBrushingMode()
            .build();

    KLTBConnection m1Active =
        KLTBConnectionBuilder.createWithDefaultState()
            .withMac("3")
            .withModel(ToothbrushModel.CONNECT_M1)
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode()
            .build();

    KLTBConnection e1Active =
        KLTBConnectionBuilder.createWithDefaultState()
            .withMac("4")
            .withModel(ToothbrushModel.CONNECT_E1)
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode()
            .build();

    KLTBConnection b1Active =
        KLTBConnectionBuilder.createWithDefaultState()
            .withMac("5")
            .withModel(ToothbrushModel.CONNECT_B1)
            .withState(KLTBConnectionState.ACTIVE)
            .withBrushingMode()
            .build();

    SdkBuilder.create()
        .withKLTBConnections(ce2Active, m1Active, e1Active, b1Active, ce2NonActive)
        .build();

    getActivityTestRule().launchActivity(createLaunchBrushingQuizActivityIntent());

    waitForQuizPager();

    selectAnswer(R.string.brushing_quiz_screen_1_answer_2);

    selectAnswer(R.string.brushing_quiz_screen_2_answer_1);

    selectAnswer(R.string.brushing_quiz_screen_3_answer_1);

    BrushingMode expectedBrushingMode = BrushingMode.Strong;

    onView(withId(R.id.quiz_confirmation_confirm)).check(matches(isDisplayed())).perform(click());

    ActivityUtils.assertActivityIsFinished(getActivity());

    ActivityUtils.assertResult(getActivityTestRule(), RESULT_OK);

    verify(ce2Active.brushingMode()).set(expectedBrushingMode);
    verify(b1Active.brushingMode()).set(expectedBrushingMode);
    verify(ce2NonActive.brushingMode(), never()).set(any());
    verify(m1Active.brushingMode(), never()).set(any());
    verify(m1Active.brushingMode(), never()).set(any());

    // verify BrushingMode was stored for profile
    BrushingModePerProfileRepository brushingModeRepository =
        new BrushingModePerProfileRepository(context());

    ProfileBrushingMode profileBrushingMode =
        brushingModeRepository.getForProfile(ProfileBuilder.DEFAULT_ID);
    assertNotNull(profileBrushingMode);

    assertEquals(expectedBrushingMode, profileBrushingMode.getBrushingMode());
    assertEquals(expectedOffsetDateTime, profileBrushingMode.getDateTime());
  }
}
