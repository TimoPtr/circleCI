/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.kolibree.android.test.espresso_helpers.CustomMatchers.withDrawable;
import static org.hamcrest.CoreMatchers.allOf;

import android.content.Intent;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.kolibree.android.brushingquiz.presentation.BrushingProgramActivity;
import com.kolibree.android.test.BaseActivityTestRule;
import com.kolibree.android.test.BaseEspressoTest;
import com.kolibree.android.test.KolibreeActivityTestRule;
import com.kolibree.android.test.idlingresources.IdlingResourceFactory;

@SuppressWarnings("KotlinInternalInJava")
public class BrushingProgramActivityBaseEspressoBaseTest
    extends BaseEspressoTest<BrushingProgramActivity> {
  private static final int TOTAL_SCREENS = 3;

  protected void titleIsDisplayed(@StringRes int titleResId) {
    String expectedTitle = context().getString(titleResId);

    onView(allOf(withId(R.id.quiz_question_title), isCompletelyDisplayed()))
        .check(matches(withText(expectedTitle)));
  }

  protected void answerIsDisplayed(@StringRes int answerResId) {
    onView(withText(answerResId)).check(matches(isDisplayed()));
  }

  protected void selectAnswer(@StringRes int answerResId) {
    onView(withText(answerResId)).check(matches(isDisplayed())).perform(click());
  }

  protected void logoIsDisplayed(@DrawableRes int logoId) {
    onView(withId(R.id.quiz_confirmation_logo))
        .check(matches(allOf(isDisplayed(), withDrawable(logoId))));
  }

  @Override
  protected BaseActivityTestRule<BrushingProgramActivity> createRuleForActivity() {
    return new KolibreeActivityTestRule.Builder<>(BrushingProgramActivity.class)
        .launchActivity(false)
        .build();
  }

  @NonNull
  protected Intent createLaunchBrushingQuizActivityIntent() {
    return new Intent();
  }

  protected void waitForQuizPager() {
    IdlingResourceFactory.INSTANCE.viewPagerIdle(R.id.quiz_viewpager).waitForIdle();
  }
}
