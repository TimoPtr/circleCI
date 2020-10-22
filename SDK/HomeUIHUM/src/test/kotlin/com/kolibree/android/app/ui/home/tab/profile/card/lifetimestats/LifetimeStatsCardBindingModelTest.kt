/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.card.lifetimestats

import android.content.Context
import android.view.View
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.homeui.hum.R
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class LifetimeStatsCardBindingModelTest : BaseUnitTest() {
    @Test
    fun `when lifetime points is equal to current points, return profile_stats_card_keep_earning_points`() {
        val viewStateZero = viewState().copy(
            lifetimePoints = 0,
            currentPoints = 0
        )

        val points = 456
        val viewStateOverZero = viewState().copy(
            lifetimePoints = points,
            currentPoints = points
        )

        val context: Context = mock()

        val expectedText = "expected text"
        whenever(context.getString(R.string.profile_stats_card_keep_earning_points))
            .thenReturn(expectedText)

        assertEquals(
            expectedText,
            bindingModel(viewStateZero).formatLifetimePoints(context)
        )

        assertEquals(
            expectedText,
            bindingModel(viewStateOverZero).formatLifetimePoints(context)
        )
    }

    @Test
    fun `when lifetime points is different than current points, return profile_stats_card_keep_earning_points`() {
        val viewState = viewState().copy(
            lifetimePoints = 2,
            currentPoints = 1
        )

        val context: Context = mock()

        val expectedText = "expected text"
        whenever(context.getString(eq(R.string.profile_stats_card_lifetime_points), anyVararg()))
            .thenReturn(expectedText)

        assertEquals(
            expectedText,
            bindingModel(viewState).formatLifetimePoints(context)
        )
    }

    @Test
    fun `brushing chart is VISIBLE when inAppStats is enabled and there is more than 0 brushings`() {
        val onlyInAppBrushings = viewState().copy(
            inAppCount = 0,
            offlineCount = 1
        )
        assertEquals(View.VISIBLE, bindingModel(onlyInAppBrushings).brushingsChartVisibility())

        val onlyOfflineBrushings = viewState().copy(
            inAppCount = 0,
            offlineCount = 1
        )
        assertEquals(View.VISIBLE, bindingModel(onlyOfflineBrushings).brushingsChartVisibility())

        val bothInAppOfflineBrushings = viewState().copy(
            inAppCount = 4,
            offlineCount = 8
        )
        assertEquals(View.VISIBLE, bindingModel(bothInAppOfflineBrushings).brushingsChartVisibility())
    }

    @Test
    fun `brushing chart is GONE when there is 0 brushings`() {
        val onlyInAppBrushings = viewState().copy(
            inAppCount = 0,
            offlineCount = 0
        )
        assertEquals(View.GONE, bindingModel(onlyInAppBrushings).brushingsChartVisibility())
    }

    private fun viewState() = LifetimeStatsCardViewState.initial(DynamicCardPosition.EIGHT)

    private fun bindingModel(viewState: LifetimeStatsCardViewState) =
        LifetimeStatsCardBindingModel(viewState)
}
