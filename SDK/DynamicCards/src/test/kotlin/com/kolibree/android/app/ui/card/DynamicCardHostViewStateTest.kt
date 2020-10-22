/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.mock.InvisibleCardBindingModel
import com.kolibree.android.app.ui.card.mock.ToggleableCardBindingModel
import com.kolibree.android.app.ui.card.mock.VisibleCardBindingModel
import com.kolibree.android.app.ui.host.DynamicCardHostViewState
import com.kolibree.android.failearly.FailEarly
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DynamicCardHostViewStateTest : BaseUnitTest() {

    @Test
    fun `getCards returns empty list if map is empty`() {
        assertTrue(
            DynamicCardHostViewState(
                emptyMap()
            ).cards.isEmpty())
    }

    @Test
    fun `getCards returns cards sorted by display position`() {
        var viewState = DynamicCardHostViewState.fromBindingModels(
            listOf(
                VisibleCardBindingModel(cardPosition = DynamicCardPosition.EIGHT),
                InvisibleCardBindingModel(cardPosition = DynamicCardPosition.THREE),
                ToggleableCardBindingModel(
                    isVisible = true,
                    cardPosition = DynamicCardPosition.FOUR
                )
            )
        )

        assertTrue(viewState.cards[0] is ToggleableCardBindingModel)
        assertTrue(viewState.cards[1] is VisibleCardBindingModel)

        viewState = viewState.copyWithUpdatedCardState(
            VisibleCardBindingModel(cardPosition = DynamicCardPosition.TWO)
        )

        assertTrue(viewState.cards[0] is VisibleCardBindingModel)
        assertTrue(viewState.cards[1] is ToggleableCardBindingModel)
    }

    @Test
    fun `getCards filters out invisible cards`() {
        val visible = VisibleCardBindingModel(cardPosition = DynamicCardPosition.EIGHT)
        val visibleToggleableModel = ToggleableCardBindingModel(
            true,
            cardPosition = DynamicCardPosition.FOUR
        )

        var viewState =
            DynamicCardHostViewState(
                mapOf(
                    VisibleCardBindingModel::class.java to visible,
                    InvisibleCardBindingModel::class.java to InvisibleCardBindingModel(cardPosition = DynamicCardPosition.THREE),
                    ToggleableCardBindingModel::class.java to visibleToggleableModel
                )
            )

        assertEquals(2, viewState.cards.size)
        assertTrue(viewState.cards.contains(visible))
        assertTrue(viewState.cards.contains(visibleToggleableModel))

        val invisibleToggleableModel = ToggleableCardBindingModel(
            false,
            cardPosition = DynamicCardPosition.FOUR
        )
        viewState = viewState.copyWithUpdatedCardState(invisibleToggleableModel)

        assertEquals(1, viewState.cards.size)
        assertTrue(viewState.cards.contains(visible))
        assertFalse(viewState.cards.contains(invisibleToggleableModel))
    }

    @Test(expected = IllegalStateException::class)
    fun `sanitizeState does not allow layoutId duplicates`() {
        FailEarly.overrideDelegateWith { _, _ -> throw IllegalStateException() }
        DynamicCardHostViewState(
            mapOf(
                VisibleCardBindingModel::class.java to VisibleCardBindingModel(
                    layoutId = 0,
                    cardPosition = DynamicCardPosition.EIGHT
                ),
                InvisibleCardBindingModel::class.java to InvisibleCardBindingModel(
                    layoutId = 0,
                    cardPosition = DynamicCardPosition.THREE
                ),
                ToggleableCardBindingModel::class.java to ToggleableCardBindingModel(
                    false,
                    cardPosition = DynamicCardPosition.FOUR
                )
            )
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `sanitizeState does not allow position duplicates`() {
        FailEarly.overrideDelegateWith { _, _ -> throw IllegalStateException() }
        DynamicCardHostViewState(
            mapOf(
                VisibleCardBindingModel::class.java to VisibleCardBindingModel(
                    cardPosition = DynamicCardPosition.EIGHT
                ),
                InvisibleCardBindingModel::class.java to InvisibleCardBindingModel(
                    cardPosition = DynamicCardPosition.EIGHT
                ),
                ToggleableCardBindingModel::class.java to ToggleableCardBindingModel(
                    false,
                    cardPosition = DynamicCardPosition.FOUR
                )
            )
        )
    }
}
