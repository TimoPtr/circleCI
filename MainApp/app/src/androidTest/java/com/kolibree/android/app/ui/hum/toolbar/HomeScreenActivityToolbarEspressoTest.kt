/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toolbar

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.annotation.ColorRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withAlpha
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.google.android.material.shape.MaterialShapeDrawable
import com.kolibree.R
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.dagger.EspressoShopDataModule.defaultProductList
import com.kolibree.android.app.ui.home.HomeScreenActivity
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.BaseActivityTestRule
import com.kolibree.android.test.BaseEspressoTest
import com.kolibree.android.test.KolibreeActivityTestRule
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.AppMocker
import com.kolibree.android.test.utils.SdkBuilder
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test

class HomeScreenActivityToolbarEspressoTest : BaseEspressoTest<HomeScreenActivity>() {

    override fun createRuleForActivity(): BaseActivityTestRule<HomeScreenActivity> =
        KolibreeActivityTestRule.Builder(HomeScreenActivity::class.java)
            .launchActivity(false)
            .build()

    @Test
    fun testShoppingCartBadge() {
        prepareMocks()

        launchActivity()

        onToolbar(withId(R.id.cart_count))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testNoBrush() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.TERMINATED)

        launchActivity()

        onToolbar(withId(R.id.main_icon))
            .check(matches(withAlpha(THIRTY_PERCENT)))
    }

    @Test
    fun testConnectedBrush() {
        prepareMocks(mockConnectionWithState = KLTBConnectionState.ACTIVE)

        launchActivity()

        onToolbar(withId(R.id.main_icon))
            .check(matches(withAlpha(1.0f)))
    }

    private fun onToolbar(matches: Matcher<View>): ViewInteraction {
        return onView(allOf(matches, isDescendantOfA(withId(R.id.toolbar))))
    }

    private fun hasBackgroundColor(@ColorRes colorRes: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {

            override fun describeTo(description: Description) {
                description.appendText("background color: $colorRes")
            }

            override fun matchesSafely(item: View): Boolean {
                val context = item.context
                val backgroundColor =
                    (item.background as MaterialShapeDrawable).fillColor?.defaultColor
                val expectedColor = if (Build.VERSION.SDK_INT <= 22) {
                    @Suppress("DEPRECATION")
                    context?.resources?.getColor(colorRes)
                } else {
                    context?.getColor(colorRes)
                }

                return backgroundColor == expectedColor
            }
        }
    }

    private fun withTint(
        @ColorRes colorRes: Int
    ) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("ImageView with tintAttr set to $colorRes")
        }

        override fun matchesSafely(view: View): Boolean {
            val actual = view.getTag(R.attr.tint) as? Int ?: 0
            val expected = view.context.getColor(colorRes)
            return actual == expected
        }
    }

    private fun prepareMocks(
        profile: Profile = ProfileBuilder.create().withName(PROFILE_NAME).withId(
            PROFILE_ID
        )
            .build(),
        profileSmiles: Int = PROFILE_SMILES,
        mockConnectionWithState: KLTBConnectionState? = null
    ) {
        var builder = SdkBuilder.create()
            .withActiveProfile(profile)
            .withProfiles(profile)
        if (mockConnectionWithState != null) {
            val connection = KLTBConnectionBuilder.createWithDefaultState()
                .withState(mockConnectionWithState)
                .withOwnerId(profile.id)
                .build()

            builder = builder.withKLTBConnections(connection)
        }

        AppMocker.create().withSdkBuilder(builder)
            .withProfileSmiles(
                profile.id,
                profile.firstName,
                profileSmiles
            )
            .withLocationPermissionGranted(true)
            .withLocationEnabled(true)
            .withMockedShopifyData(defaultProductList.map { it.copy(quantity = 1) })
            .prepareForMainScreen()
            .mock()
    }

    private fun launchActivity() {
        activityTestRule.launchActivity(Intent(context(), HomeScreenActivity::class.java))
    }

    private companion object {

        const val PROFILE_ID = 2L
        const val PROFILE_NAME = "Peter"
        const val PROFILE_SMILES = 4350

        const val THIRTY_PERCENT = 0.3f
    }
}
