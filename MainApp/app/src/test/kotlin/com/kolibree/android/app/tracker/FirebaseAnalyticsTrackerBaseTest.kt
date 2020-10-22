/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.tracker

import android.app.Activity
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.tracker.logic.userproperties.UserProperties.ACCOUNT
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FirebaseAnalyticsTrackerBaseTest : BaseUnitTest() {

    private lateinit var tracker: FirebaseAnalyticsTrackerUnderTest

    override fun setup() {
        super.setup()
        tracker = createTracker()
    }

    @Test
    fun `sendEvent with screen name invokes logEvent`() {
        tracker.sendEvent("TestScreenName", emptyMap())
        verify(tracker).logEvent("TestScreenName", null)
    }

    @Test
    fun `sendEvent with screen name sanitizes the event name`() {
        tracker.sendEvent("Test Screen Name No. #01", emptyMap())
        verify(tracker).logEvent("Test_Screen_Name_No_01", null)
    }

    @Test
    fun `sendEvent with screen name updates user properties if they're available and saves them`() {
        tracker.sendEvent(
            "TestScreenName",
            mapOf(ACCOUNT to "1", GENDER to Gender.MALE.serializedName)
        )

        verify(tracker, times(2)).setUserProperty(any(), any())
        verify(tracker).logEvent("TestScreenName", null)
    }

    @Test
    fun `sendEvent with screen name maps nulls to NA strings`() {
        tracker.sendEvent("TestScreenName", mapOf(ACCOUNT to null, GENDER to null))

        verify(tracker, times(2)).setUserProperty(any(), eq(NOT_AVAILABLE))
        verify(tracker).logEvent("TestScreenName", null)
    }

    @Test
    fun `sendEvent with screen name updates send arguments that are not user properties`() {
        tracker.sendEvent(
            "TestScreenName", mapOf(
                ACCOUNT to "1",
                "CustomArgument" to "CustomArgumentValue",
                GENDER to Gender.MALE.serializedName
            )
        )

        argumentCaptor<String> {
            verify(tracker, times(2)).setUserProperty(capture(), any())

            assertEquals(ACCOUNT, firstValue)
            assertEquals(GENDER, secondValue)
        }
        verify(tracker).logEvent("TestScreenName", mapOf("CustomArgument" to "CustomArgumentValue"))
    }

    private fun createTracker(): FirebaseAnalyticsTrackerUnderTest {
        return spy(FirebaseAnalyticsTrackerUnderTest())
    }

    private class FirebaseAnalyticsTrackerUnderTest : FirebaseAnalyticsTrackerBase() {

        public override fun logEvent(
            eventName: String,
            arguments: Map<String, String>?
        ) {
            /* no-op */
        }

        public override fun setUserProperty(key: String, value: String) { /* no-op */
        }

        override fun setCurrentScreen(activity: Activity, screenName: String) {
            /* no-op */
        }
    }
}
