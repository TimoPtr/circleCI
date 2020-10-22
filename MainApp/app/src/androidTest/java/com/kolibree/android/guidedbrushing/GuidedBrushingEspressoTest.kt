/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.ActivityUtils.assertActivityIsFinished
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import org.hamcrest.Matchers.allOf
import org.junit.Test

internal class GuidedBrushingEspressoTest : GuidedBrushingEspressoTestBase() {

    private lateinit var stateSubject: PublishRelay<KLTBConnectionState>

    @Test
    fun lostConnectionDialog() {
        setupAndLaunch()

        assertDialogIsNotVisible()

        makeScreenshot("GuidedBrushingScreen_ActiveConnection")

        stateSubject.accept(KLTBConnectionState.TERMINATED)

        makeScreenshot("GuidedBrushingScreen_TerminatedConnection_LostConnectionDialog")

        assertDialogIsVisible()

        stateSubject.accept(KLTBConnectionState.ACTIVE)

        assertDialogIsNotVisible()

        stateSubject.accept(KLTBConnectionState.TERMINATED)

        assertDialogIsVisible()

        onView(withText(R.string.dialog_lost_connection_quit_button)).perform(click())
        assertActivityIsFinished(activity)
    }

    private fun setupAndLaunch() {
        stateSubject = PublishRelay.create()

        val connection = KLTBConnectionBuilder.createWithDefaultState()
            .withOwnerId(PROFILE_ID)
            .withMac(TB_MAC)
            .withModel(TB_MODEL)
            .withStateListener(stateSubject)
            .build()

        val activeProfile = ProfileBuilder.create().withId(PROFILE_ID).build()

        val sdkBuilder = SdkBuilder.create()
            .withKLTBConnections(connection)
            .withActiveProfile(activeProfile)

        AppMocker.create().withSdkBuilder(sdkBuilder).mock()

        activityTestRule.launchActivity(intentFrom(TB_MODEL, TB_MAC))
    }

    private fun assertDialogIsVisible() {
        onView(
            allOf(
                isDescendantOfA(withId(R.id.alert_dialog_root_layout)),
                withText(R.string.dialog_lost_connection_title)
            )
        )
            .check(matches(isDisplayed()))
    }

    private fun assertDialogIsNotVisible() {
        onView(withId(R.id.alert_dialog_root_layout))
            .check(doesNotExist())
    }

    companion object {
        private val TB_MODEL = ToothbrushModel.CONNECT_E1
        private const val TB_MAC = "C0:4B:8B:0B:CD:41"
        private const val PROFILE_ID = 123L
    }
}
