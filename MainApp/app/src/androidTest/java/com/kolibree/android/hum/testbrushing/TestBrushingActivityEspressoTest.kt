/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum.testbrushing

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.R
import com.kolibree.android.app.dagger.EspressoSingleThreadSchedulerModule
import com.kolibree.android.app.dagger.EspressoTestBrushingGameLogicModule
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.connection.vibrator.VibratorListener
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KLBaseActivityTestRule
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.ActivityUtils.assertActivityIsFinished
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.kolibree.android.testbrushing.TestBrushingActivity
import com.kolibree.android.testbrushing.startHumTestBrushingIntent
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test

class TestBrushingActivityEspressoTest : BaseEspressoTest<TestBrushingActivity>() {

    @Test
    fun test_brushing_happy_path() {
        val listenerSubject = BehaviorSubject.create<Set<VibratorListener>>()
        val connectionStateSubject = PublishRelay.create<KLTBConnectionState>()

        val connection = KLTBConnectionBuilder.createWithDefaultState()
            .withOwnerId(PROFILE_ID)
            .withMac(TB_MAC)
            .withModel(TB_MODEL)
            .withMultiListenerInterception(listenerSubject)
            .withStateListener(connectionStateSubject)
            .build()

        val toothbrushSwitcher = init(connection, listenerSubject)

        makeScreenshot("StartSessionScreen")
        StartSessionScreen.checkIfScreenIsDisplayed()

        toothbrushSwitcher.on()

        makeScreenshot("OngoingSessionScreen")
        OngoingSessionScreen.checkIfScreenIsDisplayed(infoMessageShouldBeVisible = false)
        setupAppContext()

        toothbrushSwitcher.off()

        makeScreenshot("PauseSessionScreen")
        PauseSessionScreen.checkIfScreenIsDisplayed()

        toothbrushSwitcher.on()

        makeScreenshot("OngoingSessionScreen_AfterResume")
        OngoingSessionScreen.checkIfScreenIsDisplayed(infoMessageShouldBeVisible = true)

        toothbrushSwitcher.off()

        connectionStateSubject.accept(KLTBConnectionState.TERMINATED)

        makeScreenshot("ConnectionLostSessionScreen")
        ConnectionLostSessionScreen.checkIfScreenIsDisplayed()

        connectionStateSubject.accept(KLTBConnectionState.ACTIVE)

        onView(withId(R.id.complete_button))
            .check(matches(isDisplayed()))
            .perform(click())

        assertActivityIsFinished(activity)
    }

    private fun setupAppContext() {
        with(EspressoTestBrushingGameLogicModule.appContext) {
            doReturn(true).whenever(this).isFullBrushingProcessingPossible
        }
    }

    fun defaultIntent(model: ToothbrushModel = TB_MODEL) =
        startHumTestBrushingIntent(context(), TB_MAC, model)

    override fun createRuleForActivity(): KLBaseActivityTestRule<TestBrushingActivity> {
        return KolibreeActivityTestRule.Builder(TestBrushingActivity::class.java)
            .launchActivity(false).build()
    }

    private fun init(
        connection: InternalKLTBConnection,
        listenerSubject: BehaviorSubject<Set<VibratorListener>>
    ): VibratorSwitcher {
        val activeProfile = ProfileBuilder.create().withId(PROFILE_ID).build()

        val sdkBuilder = SdkBuilder.create()
            .withKLTBConnections(connection)
            .withActiveProfile(activeProfile)
            .prepareForMainScreen()

        AppMocker.create().withSdkBuilder(sdkBuilder).mock()

        activityTestRule.launchActivity(defaultIntent())

        return VibratorSwitcher(listenerSubject, activity, connection)
    }

    object StartSessionScreen {

        fun checkIfScreenIsDisplayed() {
            IdlingResourceFactory.viewVisibility(
                R.id.brushing_start_container,
                View.VISIBLE
            ).waitForIdle()

            onView(withId(R.id.brushing_start_container))
                .check(matches(isDisplayed()))
            onView(withText(R.string.test_brushing_start_brushing_header))
                .check(matches(isDisplayed()))
        }
    }

    object OngoingSessionScreen {
        fun checkIfScreenIsDisplayed(infoMessageShouldBeVisible: Boolean) {
            IdlingResourceFactory.viewVisibility(
                R.id.ongoing_brushing_container,
                View.VISIBLE
            ).waitForIdle()

            onView(allOf(withId(R.id.ongoing_brushing_container)))
                .check(matches(isDisplayed()))

            val visibility = if (infoMessageShouldBeVisible)
                ViewMatchers.Visibility.VISIBLE
            else
                ViewMatchers.Visibility.INVISIBLE

            onView(withId(R.id.turn_off_toothbrush_info_message)).check(
                matches(withEffectiveVisibility(visibility))
            )

            EspressoSingleThreadSchedulerModule.scheduler.advanceTimeBy(20, TimeUnit.SECONDS)

            IdlingResourceFactory.viewVisibility(
                R.id.turn_off_toothbrush_info_message,
                View.VISIBLE
            ).waitForIdle()

            onView(withId(R.id.turn_off_toothbrush_info_message)).check(matches(isDisplayed()))
        }
    }

    object PauseSessionScreen {
        fun checkIfScreenIsDisplayed() {
            IdlingResourceFactory.viewVisibility(
                R.id.paused_headline,
                View.VISIBLE
            ).waitForIdle()

            onView(allOf(withId(R.id.paused_headline)))
                .check(matches(isDisplayed()))
            onView(allOf(withId(R.id.complete_button)))
                .check(matches(isDisplayed()))
            onView(allOf(withId(R.id.continue_button)))
                .check(matches(isDisplayed()))
        }
    }

    object ConnectionLostSessionScreen {
        fun checkIfScreenIsDisplayed() {
            onView(withText(R.string.dialog_lost_connection_title))
                .check(matches(isDisplayed()))
        }
    }

    internal class VibratorSwitcher(
        val subject: BehaviorSubject<Set<VibratorListener>>,
        val activity: TestBrushingActivity,
        val connection: KLTBConnection
    ) {

        fun on() = switchTo(true)

        fun off() = switchTo(false)

        private fun switchTo(state: Boolean) {
            activity.runOnUiThread {
                whenever(connection.vibrator().isOn).thenReturn(state)
                subject.take(1).blockingFirst().forEach {
                    it.onVibratorStateChanged(connection, state)
                }
            }
        }
    }

    companion object {
        private val TB_MODEL = ToothbrushModel.CONNECT_E1
        private const val TB_MAC = "C0:4B:8B:0B:CD:41"
        private const val PROFILE_ID = 123L
    }
}
