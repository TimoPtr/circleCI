/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.startscreen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.EXTRA_GUIDED_BRUSHING_PARAMS
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.GuidedBrushingStartScreenActivity
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.GuidedBrushingStartScreenParams
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.startGuidedBrushingStartScreenIntent
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KLBaseActivityTestRule
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.utils.ActivityUtils.assertActivityIsFinished
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Test

internal class GuidedBrushingStartScreenActivityEspressoTest :
    BaseEspressoTest<GuidedBrushingStartScreenActivity>() {

    @Test
    fun start_guided_brushing_happy_path_without_brush() {
        AppMocker.create().mock()

        activityTestRule.launchActivity(defaultIntent())

        onView(withId(R.id.logo))
            .check(matches(isDisplayed()))

        makeScreenshot("GuidedBrushingStartScreen_no_brush")

        onView(withId(R.id.start_button))
            .check(matches(allOf(isDisplayed(), isEnabled())))
            .perform(click())

        assertActivityIsFinished(activity)
    }

    @Test
    fun start_guided_brushing_happy_path_with_brush_vibrator_state_changing() {
        val connection = KLTBConnectionBuilder.createWithDefaultState().withMac("hello").build()
        val processor = PublishProcessor.create<Boolean>()

        processor.onNext(true)

        whenever(connection.vibrator().vibratorStream).thenReturn(processor)

        val sdkBuilder = SdkBuilder.create()
            .withKLTBConnections(connection)

        AppMocker.create().withSdkBuilder(sdkBuilder).mock()
        val intent = defaultIntent()

        intent.putExtra(
            EXTRA_GUIDED_BRUSHING_PARAMS,
            GuidedBrushingStartScreenParams(
                false,
                connection.toothbrush().mac,
                connection.toothbrush().model
            )
        )
        activityTestRule.launchActivity(intent)

        onView(withId(R.id.logo))
            .check(matches(isDisplayed()))

        makeScreenshot("GuidedBrushingStartScreen_with_brush_vibrating")

        onView(withId(R.id.start_button))
            .check(matches(allOf(isDisplayed(), not(isEnabled()))))

        processor.onNext(false)

        IdlingResourceFactory.viewEnabled(R.id.start_button, true).waitForIdle()

        onView(withId(R.id.start_button))
            .check(matches(allOf(isDisplayed(), isEnabled())))
            .perform(click())

        assertActivityIsFinished(activity)
    }

    override fun createRuleForActivity(): KLBaseActivityTestRule<GuidedBrushingStartScreenActivity> {
        return KolibreeActivityTestRule.Builder(GuidedBrushingStartScreenActivity::class.java)
            .launchActivity(false).build()
    }

    private fun defaultIntent() =
        startGuidedBrushingStartScreenIntent(context())
}
