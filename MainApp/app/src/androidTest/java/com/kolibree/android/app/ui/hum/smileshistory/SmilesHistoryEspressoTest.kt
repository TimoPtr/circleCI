/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.smileshistory

import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.kolibree.R
import com.kolibree.android.app.ui.home.HomeScreenActivityEspressoTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.rewards.smileshistory.startSmilesHistoryIntent
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.test.assertions.RecyclerViewItemCountAssertion
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withDrawable
import com.kolibree.android.test.espresso_helpers.CustomMatchers.withRecyclerView
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.rewards.ProfileRewardsBuilder
import com.kolibree.android.test.mocks.rewards.ProfileSmilesHistoryHelper
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.hamcrest.CoreMatchers.not
import org.junit.Test
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle

internal class SmilesHistoryEspressoTest : HomeScreenActivityEspressoTest() {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    private fun launchSmilesHistory() {
        launchActivity()
        context().startActivity(startSmilesHistoryIntent(context()).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
        })
    }

    override fun setUp() {
        super.setUp()

        whenever(component().firstLoginDateProvider().firstRunDate())
            .thenReturn(TrustedClock.getNowZonedDateTime())
    }

    @Test
    fun open_empty_smiles_history() {
        val profileIdHenry = 5L
        val profileHenry = ProfileBuilder.create().withId(profileIdHenry).withName("Henry").build()

        SdkBuilder.create()
            .withProfiles(profileHenry)
            .withActiveProfile(profileHenry)
            .prepareForMainScreen()

        launchSmilesHistory()

        onView(withId(R.id.item_list)).check(matches(not(isDisplayed())))
        onView(withText(R.string.smiles_history_empty)).check(matches(isDisplayed()))
    }

    @Test
    fun smiles_history_with_items() {
        val profileIdHenry = 5L
        val profileHenry = ProfileBuilder.create().withId(profileIdHenry).withName("Henry").build()
        val creationTime = TrustedClock.getNowZonedDateTime()
        val expectedSmiles = 50

        profileSmilesDatastore().replace(
            ProfileRewardsBuilder.createProfileSmiles(
                profileIdHenry,
                500
            )
        )

        val sdkBuilder = SdkBuilder.create()
            .withProfiles(profileHenry)
            .withActiveProfile(profileHenry)
            .prepareForMainScreen()

        AppMocker.create().withSdkBuilder(sdkBuilder).prepareForMainScreen().mock()

        val smilesHistoryEventPositive =
            ProfileSmilesHistoryHelper.ProfileSmilesHistoryBuilder.create()
                .withId(1)
                .withCreationTime(creationTime.plusDays(1))
                .withEventType("Account created")
                .withProfileId(profileIdHenry)
                .withSmiles(expectedSmiles)
                .buildSmilesHistoryEvent()

        val smilesHistoryEventNegative =
            ProfileSmilesHistoryHelper.ProfileSmilesHistoryBuilder.create()
                .withId(2)
                .withCreationTime(creationTime)
                .withEventType("Points expired")
                .withProfileId(profileIdHenry)
                .withSmiles(-expectedSmiles)
                .buildSmilesHistoryEvent()

        val profileSmilesHistoryHelper = ProfileSmilesHistoryHelper.create()
            .setProfileId(profileIdHenry)
            .addHistoryEvent(smilesHistoryEventPositive)
            .addHistoryEvent(smilesHistoryEventNegative)

        (component().profileSmilesHistoryDatastore() as SynchronizableReadOnlyDataStore).replace(
            profileSmilesHistoryHelper.smilesHistoryEventEntities
        )

        launchSmilesHistory()

        onView(withId(R.id.item_list)).check(matches(isDisplayed()))
        onView(withText(R.string.smiles_history_empty)).check(matches(not(isDisplayed())))

        onView(withId(R.id.item_list)).check(RecyclerViewItemCountAssertion.withItemCount(3))

        checkHistoryItemAt(
            1,
            R.drawable.ic_star,
            R.string.smiles_history_account_created,
            creationTime.plusDays(1),
            expectedSmiles
        )

        checkHistoryItemAt(
            2,
            R.drawable.ic_discount_applied,
            R.string.smiles_history_smiles_expired,
            creationTime,
            -expectedSmiles
        )
    }

    @Test
    fun smiles_history_with_item_info() {
        val profileIdHenry = 5L
        val profileHenry = ProfileBuilder.create().withId(profileIdHenry).withName("Henry").build()
        val creationTime = TrustedClock.getNowZonedDateTime()
        val brushingId = 10L
        val brushing = Brushing(
            0L,
            0,
            TrustedClock.getNowOffsetDateTime(),
            0,
            0,
            null,
            profileIdHenry,
            brushingId,
            GameApiConstants.GAME_COACH_PLUS
        )

        whenever(
            component().brushingsRepository()
                .getBrushings(profileIdHenry)
        ).thenReturn(
            Single.just(
                listOf(brushing)
            )
        )

        profileSmilesDatastore().replace(
            ProfileRewardsBuilder.createProfileSmiles(
                profileIdHenry,
                500
            )
        )

        val sdkBuilder = SdkBuilder.create()
            .withProfiles(profileHenry)
            .withActiveProfile(profileHenry)
            .prepareForMainScreen()

        AppMocker.create().withSdkBuilder(sdkBuilder).prepareForMainScreen().mock()

        val dailyLimitReached =
            ProfileSmilesHistoryHelper.ProfileSmilesHistoryBuilder.create()
                .withId(1)
                .withCreationTime(creationTime.plusDays(1))
                .withEventType("Brushing session daily rewards reached")
                .withProfileId(profileIdHenry)
                .withBrushingId(brushingId)
                .withSmiles(0)
                .buildSmilesHistoryEvent()

        val incompleteSession =
            ProfileSmilesHistoryHelper.ProfileSmilesHistoryBuilder.create()
                .withId(2)
                .withCreationTime(creationTime)
                .withEventType("Brushing session incomplete")
                .withBrushingId(brushingId)
                .withProfileId(profileIdHenry)
                .withSmiles(0)
                .buildSmilesHistoryEvent()

        val profileSmilesHistoryHelper = ProfileSmilesHistoryHelper.create()
            .setProfileId(profileIdHenry)
            .addHistoryEvent(dailyLimitReached)
            .addHistoryEvent(incompleteSession)

        (component().profileSmilesHistoryDatastore() as SynchronizableReadOnlyDataStore).replace(
            profileSmilesHistoryHelper.smilesHistoryEventEntities
        )

        launchSmilesHistory()

        onView(withId(R.id.item_list)).check(matches(isDisplayed()))
        onView(withText(R.string.smiles_history_empty)).check(matches(not(isDisplayed())))

        onView(withId(R.id.item_list)).check(RecyclerViewItemCountAssertion.withItemCount(3))

        checkHistoryItemAt(
            1,
            R.drawable.ic_not_completed_brushing,
            R.string.smiles_history_guided_brushing,
            creationTime.plusDays(1),
            0,
            R.string.smiles_history_daily_limit_reached
        )

        checkHistoryItemAt(
            2,
            R.drawable.ic_not_completed_brushing,
            R.string.smiles_history_guided_brushing,
            creationTime,
            0,
            R.string.smiles_history_not_enough_coverage
        )
    }

    private fun checkHistoryItemAt(
        index: Int,
        @DrawableRes drawableRes: Int,
        @StringRes titleRes: Int,
        zonedDateTime: ZonedDateTime,
        smiles: Int,
        infoRes: Int? = null
    ) {
        onView(
            withRecyclerView(R.id.item_list).atPositionOnView(
                index,
                R.id.smiles_history_item_drawable
            )
        ).check(matches(withDrawable(drawableRes)))

        onView(
            withRecyclerView(R.id.item_list).atPositionOnView(
                index,
                R.id.smiles_history_item_title
            )
        ).check(matches(withText(titleRes)))

        onView(
            withRecyclerView(R.id.item_list).atPositionOnView(
                index,
                R.id.smiles_history_item_creation_time
            )
        ).check(
            matches(
                withText(
                    context().getString(
                        R.string.smiles_history_datetime_placeholder,
                        zonedDateTime.format(dateFormatter),
                        zonedDateTime.format(timeFormatter)
                    )
                )
            )
        )

        checkSmiles(index, smiles)

        if (infoRes != null) {
            onView(
                withRecyclerView(R.id.item_list).atPositionOnView(
                    index,
                    R.id.smiles_history_item_info
                )
            ).check(matches(withText(infoRes)))
        }
    }

    private fun checkSmiles(index: Int, smiles: Int) {
        when {
            smiles == 0 -> {
                noSmilesChanges(index)
            }
            smiles >= 0 -> {
                smilesAwarded(index, smiles)
            }
            else -> {
                smilesSpent(index, smiles)
            }
        }
    }

    private fun noSmilesChanges(index: Int) {
        onView(
            withRecyclerView(R.id.item_list).atPositionOnView(
                index,
                R.id.smiles_history_no_smiles_change
            )
        ).check(matches(isDisplayed()))
            .check(
                matches(
                    withText(
                        context().getString(
                            R.string.smiles_history_smiles_placeholder,
                            "+0"
                        )
                    )
                )
            )

        notInItemList(index, R.id.smiles_history_smiles_spent)
        notInItemList(index, R.id.smiles_history_smiles_awarded)
    }

    private fun smilesAwarded(index: Int, smiles: Int) {
        onView(
            withRecyclerView(R.id.item_list).atPositionOnView(
                index,
                R.id.smiles_history_smiles_awarded
            )
        ).check(matches(isDisplayed()))
            .check(
                matches(
                    withText(
                        context().getString(
                            R.string.smiles_history_smiles_placeholder,
                            "+$smiles"
                        )
                    )
                )
            )

        notInItemList(index, R.id.smiles_history_smiles_spent)
        notInItemList(index, R.id.smiles_history_no_smiles_change)
    }

    private fun smilesSpent(index: Int, smiles: Int) {
        onView(
            withRecyclerView(R.id.item_list).atPositionOnView(
                index,
                R.id.smiles_history_smiles_spent
            )
        ).check(matches(isDisplayed()))
            .check(
                matches(
                    withText(
                        context().getString(
                            R.string.smiles_history_smiles_placeholder,
                            "$smiles"
                        )
                    )
                )
            )

        notInItemList(index, R.id.smiles_history_smiles_awarded)
        notInItemList(index, R.id.smiles_history_no_smiles_change)
    }

    private fun notInItemList(index: Int, @IdRes id: Int) {
        onView(
            withRecyclerView(R.id.item_list).atPositionOnView(
                index,
                id
            )
        ).check(matches(not(isDisplayed())))
    }
}
