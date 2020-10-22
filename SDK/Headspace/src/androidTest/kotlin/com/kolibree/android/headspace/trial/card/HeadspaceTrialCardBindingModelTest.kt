/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import android.content.Context
import android.text.SpannableString
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.text.CustomTypefaceSpan
import com.kolibree.android.headspace.R
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class HeadspaceTrialCardBindingModelTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Goal of this test is to ensure that no one updated POEditor texts without changing the
     * highlight keys
     *
     * If they change it to something that doesn't contain 2 highlights, just update this test to
     * whatever we should expect
     */
    @Test
    fun descriptionText_containsTwoSpans() {
        val text = bindingModel().descriptionText(context()) as SpannableString

        assertEquals(
            2,
            text.getSpans(0, text.length, CustomTypefaceSpan::class.java).size
        )
    }

    @Test
    fun mutableDescriptionText_returnsSpannedPendingPointsOnInProgress() {
        val expectedPoints = 43
        val viewState = initialViewState().copy(pointsNeeded = expectedPoints)

        val expectedPointsAsString = context().getString(
            R.string.headspace_card_progress_points_to_unlock,
            expectedPoints.toString()
        )

        val text = bindingModel(viewState).mutableDescriptionText(context()) as SpannableString

        val pointsPendingStart = text.indexOf(expectedPointsAsString)
        val pointsPendingEnd = pointsPendingStart + expectedPointsAsString.length

        assertEquals(
            1,
            text.getSpans(pointsPendingStart, pointsPendingEnd, CustomTypefaceSpan::class.java).size
        )
    }

    @Test
    fun mutableDescriptionText_returnsSimpleTextOnUnlockable() {
        val viewState = initialViewState().copy(pointsNeeded = 0, isUnlocked = false)

        assertTrue(viewState.isUnlockable)

        assertEquals(
            context().getString(R.string.headspace_card_unlock_description),
            bindingModel(viewState).mutableDescriptionText(context())
        )
    }

    @Test
    fun mutableDescriptionText_returnsSpannedOnUnlocked() {
        val viewState = initialViewState().copy(isUnlocked = true)

        assertTrue(viewState.isUnlocked)

        val text = bindingModel(viewState).mutableDescriptionText(context()) as SpannableString

        val expectedHighlighted =
            context().getString(R.string.headspace_card_unlocked_description_highlight)

        val highlightStart = text.indexOf(expectedHighlighted)
        val highlightEnd = highlightStart + expectedHighlighted.length

        assertEquals(
            1,
            text.getSpans(highlightStart, highlightEnd, CustomTypefaceSpan::class.java).size
        )
    }

    /*
    callToActionText
     */
    @Test
    fun callToActionText_returns_headspace_card_visit_headspace_whenViewStateIsUnlocked() {
        val viewState = initialViewState().copy(isUnlocked = true)

        assertEquals(
            context().getString(R.string.headspace_card_visit_headspace),
            bindingModel(viewState).callToActionText(context())
        )
    }

    @Test
    fun callToActionText_returns_headspace_card_unlock_button_whenViewStateIsNotUnlocked() {
        val viewState = initialViewState().copy(isUnlocked = false)

        assertEquals(
            context().getString(R.string.headspace_card_unlock_button),
            bindingModel(viewState).callToActionText(context())
        )
    }

    /*
    copyCodeButtonText
     */
    @Test
    fun copyCodeButtonText_returnsTapToCopyWithCode_whenViewStateCopiedToClipboardIsFalse() {
        val discountCode = "Discount"
        val viewState =
            initialViewState().copy(copiedToClipboard = false, discountCode = discountCode)

        val text = bindingModel(viewState).copyCodeButtonText(context()) as SpannableString

        val highlightStart = text.indexOf(discountCode)
        val highlightEnd = highlightStart + discountCode.length

        assertEquals(
            1,
            text.getSpans(highlightStart, highlightEnd, CustomTypefaceSpan::class.java).size
        )
    }

    @Test
    fun copyCodeButtonText_returnsCopiedToClipboard_whenViewStateCopiedToClipboardIsTrue() {
        val viewState = initialViewState().copy(copiedToClipboard = true)

        val text = bindingModel(viewState).copyCodeButtonText(context()) as SpannableString

        assertEquals(
            1,
            text.getSpans(0, text.length, CustomTypefaceSpan::class.java).size
        )
    }

    /*
    Utils
     */

    private fun bindingModel(
        viewState: HeadspaceTrialCardViewState = initialViewState()
    ) =
        HeadspaceTrialCardBindingModel(viewState)

    private fun initialViewState() = HeadspaceTrialCardViewState.initial(
        position = DynamicCardPosition.EIGHT
    )
}
