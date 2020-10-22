/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.earningpoints

import android.view.View
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import org.junit.Assert.assertEquals
import org.junit.Test

class EarningPointsCardBindingModelTest : BaseUnitTest() {

    @Test
    fun `expandedVisibility returns VISIBLE when the card is expanded`() {
        assertEquals(
            View.VISIBLE,
            createInstance(expanded = true).expandedVisibility()
        )
    }

    @Test
    fun `expandedVisibility returns GONE when the card is collapsed`() {
        assertEquals(
            View.GONE,
            createInstance(expanded = false).expandedVisibility()
        )
    }

    private fun createInstance(
        position: DynamicCardPosition = DynamicCardPosition.ZERO,
        pointsPerBrush: Int = 1,
        expanded: Boolean = false
    ) = EarningPointsCardBindingModel(
        EarningPointsCardViewState(true, position, pointsPerBrush, expanded)
    )
}
