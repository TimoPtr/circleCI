/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import org.junit.Assert.assertEquals
import org.junit.Test

/** [LastBrushingCardViewState] unit tests */
class LastBrushingCardViewStateTest : BaseUnitTest() {

    @Test
    fun `position is passed to binding model`() {
        val state = LastBrushingCardViewState.initial(
            DynamicCardPosition.SEVEN
        )
        assertEquals(DynamicCardPosition.SEVEN, state.asBindingModel().position)
    }

    @Test
    fun `withItems keeps selected position when it is less than item list size`() {
        val expectedSelectedPosition = 1
        val state = LastBrushingCardViewState.initial(
            DynamicCardPosition.ONE
        )
            .withItems(
                items = listOf(BrushingCardData.empty(), BrushingCardData.empty()),
                selectedPosition = expectedSelectedPosition
            )

        assertEquals(expectedSelectedPosition, state.items.indexOfFirst { it.isSelected })
    }

    @Test
    fun `withItems sets last item as selected when the last brushing session has been deleted`() {
        val state = LastBrushingCardViewState.initial(
            DynamicCardPosition.ONE
        )
            .withItems(
                items = listOf(BrushingCardData.empty(), BrushingCardData.empty()),
                selectedPosition = 2
            )

        assertEquals(state.items.size - 1, state.items.indexOfFirst { it.isSelected })
    }
}
