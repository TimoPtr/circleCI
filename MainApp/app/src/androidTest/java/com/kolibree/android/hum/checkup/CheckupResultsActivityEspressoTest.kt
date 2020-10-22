/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum.checkup

import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.R
import com.kolibree.android.app.ui.checkup.results.CheckupOrigin
import com.kolibree.android.app.ui.checkup.results.CheckupResultsActivity
import com.kolibree.android.app.ui.checkup.results.startCheckupResultsActivityIntent
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.game.Game
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KLBaseActivityTestRule
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.kolibree.sdkws.data.model.Brushing
import org.junit.Test
import org.threeten.bp.Duration

/** [CheckupResultsActivity] Espresso tests */
@Suppress("FunctionNaming", "MagicNumber")
class CheckupResultsActivityEspressoTest : BaseEspressoTest<CheckupResultsActivity>() {

    override fun createRuleForActivity(): KLBaseActivityTestRule<CheckupResultsActivity> =
        KolibreeActivityTestRule.Builder(CheckupResultsActivity::class.java)
            .launchActivity(false).build()

    @Test
    fun checkupHumActivity_allWidgetsAreVisible() {
        val activeProfile = ProfileBuilder.create().build()

        val sdkBuilder = SdkBuilder.create()
            .withActiveProfile(activeProfile)
            .withBrushingsForProfile(ProfileBuilder.DEFAULT_ID, createBrushing())
            .prepareForMainScreen()

        AppMocker.create().withSdkBuilder(sdkBuilder).mock()

        activityTestRule.launchActivity(defaultIntent())

        assertVisible(R.id.checkup_back)
        assertVisible(R.id.checkup_header)
        assertVisible(R.id.checkup_date)
        assertVisible(R.id.checkup_brushing_summary)
        assertVisible(R.id.checkup_view)
        assertVisible(R.id.last_brushing_card_action)
        assertVisible(R.id.checkup_delete)

        makeScreenshot("CheckupScreen_Result")
    }

    private fun assertVisible(@IdRes id: Int) =
        onView(withId(id))
            .check(matches(isDisplayed()))

    private fun createBrushing() = Brushing(
        duration = Duration.ofSeconds(120L).seconds,
        coins = 0,
        dateTime = TrustedClock.getNowOffsetDateTime(),
        game = Game.COACH_PLUS.name,
        goalDuration = DEFAULT_BRUSHING_GOAL,
        kolibreeId = null,
        points = 0,
        processedData = "",
        profileId = ProfileBuilder.DEFAULT_ID,
        toothbrushMac = null
    )

    private fun defaultIntent() =
        startCheckupResultsActivityIntent(
            context(),
            CheckupOrigin.HOME
        )
}
