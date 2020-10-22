/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KLBaseActivityTestRule
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import org.junit.Test

class PushNotificationsHomeScreenActivityEspressoTest :
    BaseEspressoTest<HomeScreenActivity>() {

    private val ownerProfileId = 10L
    private val ownerProfileName = "Owner"
    private val secondProfileId = 11L
    private val secondProfileName = "Second"

    override fun setUp() {
        super.setUp()

        val ownerProfile = ProfileBuilder.create()
            .withId(ownerProfileId)
            .withName(ownerProfileName)
            .build()

        val secondProfile = ProfileBuilder.create()
            .withId(secondProfileId)
            .withName(secondProfileName)
            .build()

        val sdkBuilder = SdkBuilder.create()
            .withProfiles(ownerProfile, secondProfile)
            .withActiveProfile(secondProfile)
            .withOwnerProfile(ownerProfile)
            .withSupportAllowDataCollecting()

        val appMocker = AppMocker.create()
            .withMockedShopifyProducts()
            .withSdkBuilder(sdkBuilder)

        appMocker.mock()
    }

    @Test
    fun activityNotRunningRunning_reactsOnPushNotificationIntents() {
        launchMainActivityWithPushNotificationPayload(emptyMap())

        onView(withId(R.id.root_content_layout)).check(matches(isDisplayed()))
    }

    private fun launchMainActivityWithPushNotificationPayload(payload: Map<String, String>) {
        activityTestRule.launchActivity(
            getMainActivityIntentWithPushNotificationPayload(
                payload
            )
        )
    }

    override fun createRuleForActivity(): KLBaseActivityTestRule<HomeScreenActivity> {
        return KolibreeActivityTestRule.Builder(HomeScreenActivity::class.java)
            .launchActivity(false)
            .build()
    }
}
