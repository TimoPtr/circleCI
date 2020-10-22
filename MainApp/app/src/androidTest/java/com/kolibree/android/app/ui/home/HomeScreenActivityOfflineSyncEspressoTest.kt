/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.app.ui.home

import android.content.Intent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.BaseActivityTestRule
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createOfflineBrushing
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import org.junit.Test

internal class HomeScreenActivityOfflineSyncEspressoTest :
    BaseEspressoTest<HomeScreenActivity>() {

    @Test
    @Suppress("FunctionNaming")
    fun a_new_offline_brushing_sync_should_open_the_checkup_screen() {
        prepareMocks()

        launchActivity()

        checkIfCheckupIsDisplayed()
    }

    private fun checkIfCheckupIsDisplayed() {
        IdlingResourceFactory.viewVisibility(
            resId = R.id.checkup_header,
            visibility = View.VISIBLE
        ).waitForIdle()
        onView(withId(R.id.checkup_header)).check(matches(isDisplayed()))
        makeScreenshot("Checkup_OfflineBrushing")
    }

    private fun prepareMocks() {
        val sdkBuilder = getSdkBuilder(
            getConnection(),
            getOwnerProfile()
        )

        AppMocker.create().withSdkBuilder(sdkBuilder).mock()
    }

    private fun getSdkBuilder(connection: KLTBConnection, ownerProfile: Profile) =
        SdkBuilder.create()
            .withKLTBConnections(connection)
            .prepareForMainScreen()
            .withBluetoothEnabled(true)
            .withActiveProfile(ownerProfile)

    private fun getConnection(): KLTBConnection =
        KLTBConnectionBuilder.createWithDefaultState()
            .withMac(MAC)
            .withSerialNumber(SERIAL)
            .withOwnerId(OWNER_ID)
            .withOfflineBrushings(createOfflineBrushing())
            .withState(KLTBConnectionState.ACTIVE)
            .build()

    private fun getOwnerProfile() =
        ProfileBuilder.create()
            .withId(OWNER_ID)
            .withTargetBrushingTime(TARGET_TIME_BRUSHING)
            .withPoints()
            .build()

    override fun createRuleForActivity(): BaseActivityTestRule<HomeScreenActivity> =
        KolibreeActivityTestRule.Builder(HomeScreenActivity::class.java)
            .launchActivity(false)
            .build()

    private fun launchActivity() {
        activityTestRule.launchActivity(Intent(context(), HomeScreenActivity::class.java))
    }
}

const val TARGET_TIME_BRUSHING = 20
const val OWNER_ID = 3432L
const val MAC = "da:de"
const val SERIAL = "dasdasd"
