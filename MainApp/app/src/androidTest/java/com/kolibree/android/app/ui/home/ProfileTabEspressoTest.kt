/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import android.Manifest.permission
import android.app.Activity.RESULT_CANCELED
import android.app.Instrumentation
import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT
import android.content.Intent.CATEGORY_OPENABLE
import android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.graphics.Bitmap
import android.provider.MediaStore
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasCategories
import androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.GrantPermissionRule
import com.kolibree.R
import com.kolibree.android.app.dagger.EspresssoInOffBrushingsCountModule
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.test.idlingresources.IdlingResourceFactory
import com.kolibree.charts.inoff.domain.model.InOffBrushingsCount
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test

internal class ProfileTabEspressoTest : HomeScreenActivityEspressoTest() {

    @Rule
    @JvmField
    var permissionRule = GrantPermissionRule.grant(
        permission.CAMERA,
        permission.READ_EXTERNAL_STORAGE
    )

    @Test
    fun headerShowsAvatar_andSupportsNewAvatarFromGallery() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToProfile()

        clickAvatar()
        checkChangeAvatarDialog()

        try {
            Intents.init()

            intending(galleryIntentMatcher())
                .respondWith(Instrumentation.ActivityResult(RESULT_CANCELED, null))

            onView(withId(R.id.dialog_select_avatar_select_picture))
                .check(matches(isDisplayed()))
                .perform(click())

            intended(galleryIntentMatcher())
        } finally {
            Intents.release()
        }
    }

    @Test
    fun headerShowsProfileCompletionBubble() {
        prepareMocks(
            earnPointsChallenges = listOf(
                EarnPointsChallenge(
                    EarnPointsChallenge.Id.COMPLETE_YOUR_PROFILE,
                    100
                )
            )
        )

        launchActivity()

        BottomNavigationUtils.navigateToProfile()

        IdlingResourceFactory.viewVisibility(
            R.id.profile_complete_profile_bubble,
            View.VISIBLE
        ).waitForIdle()

        makeScreenshot("CompleteProfileBubble")

        onView(withId(R.id.profile_complete_profile_bubble)).check(matches(isDisplayed()))

        onView(
            allOf(
                withText(R.string.profile_incomplete_bubble_header),
                isDescendantOfA(withId(R.id.profile_complete_profile_bubble))
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(R.string.profile_incomplete_bubble_body),
                isDescendantOfA(withId(R.id.profile_complete_profile_bubble))
            )
        ).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(R.string.profile_incomplete_bubble_button),
                isDescendantOfA(withId(R.id.profile_complete_profile_bubble))
            )
        ).check(matches(isDisplayed())).perform(click())

        onView(withId(R.id.profile_complete_profile_bubble)).check(matches(not(isDisplayed())))
    }

    @Test
    fun lifetimeStatsCard_withBrushings_showsExpectedStats() {
        val expectedCurrentSmiles = 5454
        val expectedLifetimeSmiles = 78990
        prepareMocks(profileSmiles = expectedCurrentSmiles, lifetimeSmiles = expectedLifetimeSmiles)

        val expectedInAppBrushings = 12
        val expectedOfflineBrushings = 20
        val mock = EspresssoInOffBrushingsCountModule.inOffBrushingsCountProvider
        whenever(mock.brushingsCountStream(PROFILE_ID))
            .thenReturn(
                Flowable.just(
                    InOffBrushingsCount(
                        profileId = PROFILE_ID,
                        offlineBrushingCount = expectedOfflineBrushings,
                        onlineBrushingCount = expectedInAppBrushings
                    )
                )
            )

        launchActivity()

        BottomNavigationUtils.navigateToProfile()

        onAppBar().perform(swipeUp())

        scrollToCardWithId(R.id.stats_card_current_points)

        val expectedCurrentSmilesText = context().getString(
            R.string.profile_stats_card_current_points,
            expectedCurrentSmiles
        )

        IdlingResourceFactory.textViewContent(
            R.id.stats_card_current_points,
            expectedCurrentSmilesText
        ).waitForIdle()

        makeScreenshot("LifetimeStatsCard")

        onView(withId(R.id.stats_card_current_points))
            .check(matches(allOf(isDisplayed(), withText(expectedCurrentSmilesText))))

        onView(withId(R.id.stats_card_lifetime_points))
            .check(
                matches(
                    allOf(
                        isDisplayed(),
                        withText(
                            context().getString(
                                R.string.profile_stats_card_lifetime_points,
                                expectedLifetimeSmiles
                            )
                        )
                    )
                )
            )

        onView(withId(R.id.stats_card_no_brushings_icon))
            .check(matches(isDisplayed()))

        val expectedOfflineText = context().getString(
            R.string.profile_stats_card_offline_value,
            expectedOfflineBrushings.toString()
        )
        onView(withId(R.id.stats_card_offline_value))
            .check(matches(allOf(isDisplayed(), withText(expectedOfflineText))))

        val expectedInAppText = context().getString(
            R.string.profile_stats_card_inapp_value,
            expectedInAppBrushings.toString()
        )
        onView(withId(R.id.stats_card_inapp_value))
            .check(matches(allOf(isDisplayed(), withText(expectedInAppText))))
    }

    @Test
    fun profile_lastBrushingCard() {
        prepareMocks()

        launchActivity()

        BottomNavigationUtils.navigateToProfile()

        onAppBar().perform(swipeUp())

        scrollToCardWithId(R.id.last_brushing_card_container)

        makeScreenshot("LastBrushingCard")

        checkLastBrushingCard()
    }

    @Test
    fun profile_collapsingToolbar() {
        prepareMocks()

        launchActivity()
        makeScreenshot("InitialState")

        BottomNavigationUtils.navigateToProfile()

        assertProfileShowing()

        onAppBar().perform(swipeUp())

        assertProfileGone()

        onRecyclerView().perform(swipeDown())

        assertProfileShowing()
    }

    private fun checkChangeAvatarDialog() {
        onView(withId(R.id.dialog_select_avatar_select_picture))
            .check(matches(isDisplayed()))
        onView(withId(R.id.dialog_select_avatar_take_picture))
            .check(matches(isDisplayed()))
        onView(withId(R.id.dialog_select_avatar_icon))
            .check(matches(isDisplayed()))
        onView(withId(R.id.dialog_select_avatar_title))
            .check(matches(isDisplayed()))
    }

    private fun checkLastBrushingCard() {
        onView(
            allOf(
                withText(R.string.last_brushing_card_title),
                isDescendantOfA(withId(R.id.profile_tab))
            )
        ).check(isVisible())

        val coverageTitle = context().getString(R.string.last_brushing_card_coverage).toUpperCase()
        onView(
            allOf(
                withText(coverageTitle),
                isDescendantOfA(withId(R.id.profile_tab))
            )
        ).check(isVisible())

        val durationTitle = context().getString(R.string.last_brushing_card_duration).toUpperCase()
        onView(
            allOf(
                withText(durationTitle),
                isDescendantOfA(withId(R.id.profile_tab))
            )
        ).check(isVisible())

        onView(
            allOf(
                withText(R.string.last_brushing_card_no_brushing),
                isDescendantOfA(withId(R.id.profile_tab))
            )
        ).check(isVisible())
    }

    /*
    Utils
     */

    private fun scrollToCardWithId(@IdRes resId: Int) {
        onRecyclerView()
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(hasDescendant(withId(resId)))
            )
    }

    private fun clickAvatar() {
        onView(withId(R.id.hum_profile_header_avatar))
            .check(matches(isDisplayed()))
            .perform(click())
    }

    private fun intentResultWithBitmap(): Pair<Bitmap, Intent> {
        val conf = Bitmap.Config.ARGB_8888
        val bitmap = Bitmap.createBitmap(1, 1, conf)
        val resultIntent = Intent().apply {
            putExtra("data", bitmap)
        }
        return Pair(bitmap, resultIntent)
    }

    private fun pictureIntentMatcher(): Matcher<Intent> = hasAction(MediaStore.ACTION_IMAGE_CAPTURE)

    private fun galleryIntentMatcher(): Matcher<Intent> =
        allOf(
            hasAction(ACTION_OPEN_DOCUMENT),
            hasCategories(setOf(CATEGORY_OPENABLE)),
            hasFlags(FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_PERSISTABLE_URI_PERMISSION),
            hasType("image/*")
        )

    private fun mockKolibrizeAvatar(bitmap: Bitmap) {
        whenever(component().apiSdkUtils().kolibrizeAvatar(bitmap))
            .thenReturn(bitmap)
    }

    private fun assertChangeAvatarDialogIsDismissed() {
        onView(withId(R.id.dialog_select_avatar_take_picture))
            .check(doesNotExist())
    }

    private fun assertProfileShowing() {
        onView(withId(R.id.header_pattern)).check(matches(isDisplayed()))
        onView(withId(R.id.hum_profile_header_avatar)).check(matches(isDisplayed()))
        onView(withId(R.id.avatar_badge)).check(matches(isDisplayed()))
        onView(withId(R.id.hum_profile_header_message)).check(matches(isDisplayed()))
        onView(withId(R.id.profile_settings_button)).check(matches(isDisplayed()))
    }

    private fun assertProfileGone() {
        onView(withId(R.id.header_pattern)).check(matches(not(isVisible())))
        onView(withId(R.id.hum_profile_header_avatar)).check(matches(not(isVisible())))
        onView(withId(R.id.avatar_badge)).check(matches(not(isVisible())))
        onView(withId(R.id.hum_profile_header_message)).check(matches(not(isVisible())))
        onView(withId(R.id.profile_settings_button)).check(matches(not(isVisible())))
    }

    private fun isVisible(): ViewAssertion {
        return matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
    }

    private fun onAppBar() =
        onView(allOf(withId(R.id.appbar), withParent(withId(R.id.profile_tab))))

    private fun onRecyclerView() =
        onView(allOf(withId(R.id.content_recyclerview), withParent(withId(R.id.profile_tab))))
}
