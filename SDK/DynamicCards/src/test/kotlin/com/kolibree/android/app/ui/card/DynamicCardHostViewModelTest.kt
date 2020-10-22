/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.mock.InvisibleCardViewModel
import com.kolibree.android.app.ui.card.mock.InvisibleCardViewState
import com.kolibree.android.app.ui.card.mock.ToggleableCardBindingModel
import com.kolibree.android.app.ui.card.mock.ToggleableCardViewModel
import com.kolibree.android.app.ui.card.mock.ToggleableCardViewState
import com.kolibree.android.app.ui.card.mock.VisibleCardViewModel
import com.kolibree.android.app.ui.card.mock.VisibleCardViewState
import com.kolibree.android.app.ui.host.DynamicCardHostViewModel
import com.kolibree.android.app.ui.host.DynamicCardHostViewState
import com.kolibree.android.test.lifecycleTester
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class DynamicCardHostViewModelTest : BaseUnitTest() {

    @Test
    fun `cards contain only visible card states`() {
        val cardViewModels = setOf(
            VisibleCardViewModel(initialState = VisibleCardViewState(DynamicCardPosition.ONE)),
            InvisibleCardViewModel(initialState = InvisibleCardViewState(DynamicCardPosition.TWO)),
            ToggleableCardViewModel(
                true,
                initialState = ToggleableCardViewState(
                    visible = true,
                    position = DynamicCardPosition.THREE
                )
            )
        )
        val viewModel = DynamicCardHostViewModel(
            DynamicCardHostViewState.fromViewModels(cardViewModels),
            cardViewModels
        )

        val cardTester = viewModel.cards.test()
        assertEquals(2, cardTester.value().size)

        viewModel.updateViewState {
            copyWithUpdatedCardState(
                ToggleableCardBindingModel(
                    false,
                    cardPosition = DynamicCardPosition.FOUR
                )
            )
        }
        assertEquals(1, cardTester.value().size)
    }

    @Test
    fun `onStart subscribes to cardViewState flowables`() {
        val cardViewModel = ToggleableCardViewModel(false)

        val cardViewModels = setOf(cardViewModel)
        val viewModel = DynamicCardHostViewModel(
            DynamicCardHostViewState.fromViewModels(cardViewModels),
            cardViewModels
        )
        val cardTester = viewModel.cards.test()
        val lifecycleTester = viewModel.lifecycleTester()

        lifecycleTester.pushLifecycleTo(Lifecycle.Event.ON_START)

        cardTester.assertValue(emptyList())

        cardViewModel.updateViewState { ToggleableCardViewState(visible = true) }

        cardTester.assertHasValue()
        assertFalse(cardTester.value().isEmpty())
        assertTrue(cardTester.value()[0] is ToggleableCardBindingModel)
        assertTrue((cardTester.value()[0] as ToggleableCardBindingModel).visible)
    }
}
