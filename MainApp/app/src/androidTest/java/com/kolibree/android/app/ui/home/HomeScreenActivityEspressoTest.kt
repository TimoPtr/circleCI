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
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.ui.game.ActivityGame
import com.kolibree.android.app.ui.navigation.horizontal.itemForId
import com.kolibree.android.feature.AmazonDashFeature
import com.kolibree.android.feature.ShowAllMoreWaysCardsFeature
import com.kolibree.android.feature.ShowMindYourSpeedFeature
import com.kolibree.android.feature.ShowShopTabsFeature
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.test.BaseActivityTestRule
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.dagger.EspressoBrushingUseCaseModule
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import com.kolibree.sdkws.data.model.GruwareData
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDate

internal abstract class HomeScreenActivityEspressoTest :
    BaseEspressoTest<HomeScreenActivity>() {

    override fun createRuleForActivity(): BaseActivityTestRule<HomeScreenActivity> {
        return KolibreeActivityTestRule.Builder(HomeScreenActivity::class.java)
            .launchActivity(false)
            .build()
    }

    override fun setUp() {
        super.setUp()
        whenever(component().kolibreeConnector().syncAndNotify())
            .thenReturn(Single.just(true))
        whenever(component().brushingsRepository().getLastBrushingSessionFlowable(PROFILE_ID))
            .thenReturn(Flowable.never())
    }

    protected fun prepareMocks(
        brushingTime: Int = 120,
        birthDate: LocalDate? = null,
        profile: Profile = ProfileBuilder
            .create()
            .withName(PROFILE_NAME)
            .withId(PROFILE_ID)
            .withTargetBrushingTime(brushingTime)
            .withBirthday(birthDate)
            .build(),
        profileSmiles: Int = PROFILE_SMILES,
        lifetimeSmiles: Int = LIFETIME_SMILES,
        connectionBuilder: KLTBConnectionBuilder?,
        brushingNumber: Long = 1L,
        pulsingDotAlwaysVisible: Boolean = false,
        showMindYourSpeed: Boolean = false,
        earnPointsChallenges: List<EarnPointsChallenge>? = null,
        hasToolboxExplanationBeenShown: Boolean = true,
        isNewsletterSubscribed: Boolean = false,
        gruwareData: GruwareData? = null,
        showAllEarnPointsCards: Boolean = false,
        amazonDrsEnabled: Boolean = false,
        showTabsInShop: Boolean = false
    ) {
        var builder = SdkBuilder.create()
            .withEmail(EMAIL)
            .withActiveProfile(profile)
            .withProfiles(profile)
            .withAmazonDrsEnabled(amazonDrsEnabled)

        if (gruwareData != null) {
            builder.withGruwareData(gruwareData)
            connectionBuilder?.withOTAAvailable(gruwareData)
        }

        if (connectionBuilder != null) {
            val connection = connectionBuilder.withOwnerId(profile.id).build()
            builder = builder.withKLTBConnections(connection)
        }

        AppMocker.create().withSdkBuilder(builder)
            .withProfileSmiles(
                profile.id,
                profile.firstName,
                profileSmiles
            )
            .withLifetimeSmiles(profile.id, lifetimeSmiles)
            .withLocationPermissionGranted(true)
            .withLocationEnabled(true)
            .withMockedShopifyProducts()
            .withPulsingDotsAlwaysVisible(pulsingDotAlwaysVisible)
            .withFeature(ShowMindYourSpeedFeature, showMindYourSpeed)
            .withFeature(ShowAllMoreWaysCardsFeature, showAllEarnPointsCards)
            .withFeature(AmazonDashFeature, true)
            .withFeature(ShowShopTabsFeature, showTabsInShop)
            .withToolboxExplanationShown(hasToolboxExplanationBeenShown)
            .withNewsletterSubscription(isNewsletterSubscribed)
            .withEarnPointsChallenges(earnPointsChallenges)
            .prepareForMainScreen()
            .mock()

        whenever(EspressoBrushingUseCaseModule.mock.getBrushingCount(ActivityGame.TestBrushing))
            .thenReturn(Flowable.just(brushingNumber))
    }

    protected fun prepareMocks(
        brushingTime: Int = 120,
        birthDate: LocalDate? = null,
        profile: Profile = ProfileBuilder
            .create()
            .withName(PROFILE_NAME)
            .withId(PROFILE_ID)
            .withTargetBrushingTime(brushingTime)
            .withBirthday(birthDate)
            .build(),
        profileSmiles: Int = PROFILE_SMILES,
        lifetimeSmiles: Int = LIFETIME_SMILES,
        mockConnectionWithState: KLTBConnectionState? = null,
        mockConnectionStateListener: Observable<KLTBConnectionState>? = null,
        brushingNumber: Long = 1L,
        pulsingDotAlwaysVisible: Boolean = false,
        showMindYourSpeed: Boolean = false,
        earnPointsChallenges: List<EarnPointsChallenge>? = null,
        hasToolboxExplanationBeenShown: Boolean = true,
        isNewsletterSubscribed: Boolean = false,
        showAllEarnPointsCards: Boolean = false,
        amazonDrsEnabled: Boolean = false,
        showTabsInShop: Boolean = false
    ) {
        prepareMocks(
            brushingTime,
            birthDate,
            profile,
            profileSmiles,
            lifetimeSmiles,
            getKLTBConnectionBuilder(mockConnectionWithState, mockConnectionStateListener),
            brushingNumber,
            pulsingDotAlwaysVisible,
            showMindYourSpeed,
            earnPointsChallenges,
            hasToolboxExplanationBeenShown,
            isNewsletterSubscribed,
            null,
            showAllEarnPointsCards,
            amazonDrsEnabled,
            showTabsInShop
        )
    }

    @Suppress("LongMethod")
    private fun getKLTBConnectionBuilder(
        mockConnectionWithState: KLTBConnectionState?,
        mockConnectionStateListener: Observable<KLTBConnectionState>?
    ): KLTBConnectionBuilder? {
        if (mockConnectionWithState != null || mockConnectionStateListener != null) {
            return KLTBConnectionBuilder.createWithDefaultState().apply {
                if (mockConnectionWithState != null) {
                    withState(mockConnectionWithState)
                }
                if (mockConnectionStateListener != null) {
                    withStateListener(mockConnectionStateListener)
                }
            }
        }
        return null
    }

    protected fun profileSmilesDatastore() =
        component().profileSmilesDatastore() as SynchronizableReadOnlyDataStore

    protected fun launchActivity() {
        activityTestRule.launchActivity(createIntent())
    }

    protected fun bottomNavigationTo(tab: BottomNavigationTab) {
        val item = bottomNavigationItems.itemForId(tab.ordinal)
        checkNotNull(item) {
            "Item is not available in bottomNavigationItems"
        }
        onView(withId(item.triggerId)).perform(click())
    }

    private fun createIntent(): Intent {
        return Intent(context(), HomeScreenActivity::class.java)
    }

    companion object {
        const val PROFILE_ID = 2L
        const val PROFILE_NAME = "Peter"
        const val PROFILE_SMILES = 4350
        const val LIFETIME_SMILES = 5678
        const val EMAIL = "mr@robot.com"
    }
}
