/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding

import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingViewState
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class OnboardingSharedViewStateTest : BaseUnitTest() {
    @Test
    fun `progressVisible returns true if viewstate's pogressVisible is true and pairingViewstate is false`() {
        val pairingViewState = PairingViewState.initial().copy(progressVisible = false)
        assertTrue(
            viewState()
                .copy(progressVisible = true, pairingViewState = pairingViewState)
                .progressVisible()
        )
    }

    @Test
    fun `progressVisible returns true if viewstate's progressVisible is false pairingViewstate's pogressVisible is true`() {
        val pairingViewState = PairingViewState.initial().copy(progressVisible = true)
        assertTrue(
            viewState()
                .copy(progressVisible = false, pairingViewState = pairingViewState)
                .progressVisible()
        )
    }

    @Test
    fun `progressVisible returns true if viewstate's progressVisible is true pairingViewstate's pogressVisible is true`() {
        val pairingViewState = PairingViewState.initial().copy(progressVisible = true)
        assertTrue(
            viewState()
                .copy(progressVisible = true, pairingViewState = pairingViewState)
                .progressVisible()
        )
    }

    @Test
    fun `progressVisible returns false if viewstate's progressVisible is false pairingViewstate's pogressVisible is false`() {
        val pairingViewState = PairingViewState.initial().copy(progressVisible = false)
        assertFalse(
            viewState()
                .copy(progressVisible = false, pairingViewState = pairingViewState)
                .progressVisible()
        )
    }

    @Test
    fun `dismissSnackbar should copy the viewstate and hide the snackbar`() {
        val viewState = initialSharedState().copy(
            snackbarConfiguration = SnackbarConfiguration(
                isShown = true,
                error = Error.from("exception")
            )
        )

        assertFalse(viewState.withSnackbarDismissed().snackbarConfiguration.isShown)
    }

    @Test
    fun `withPromotionsAndUpdatesAccepted should copy viewState with new value`() {
        var newValue = true
        var expectedViewState = viewState().copy(
            promotionsAndUpdatesAccepted = newValue
        )
        assertEquals(expectedViewState, viewState().withPromotionsAndUpdatesAccepted(newValue))

        newValue = false
        expectedViewState = viewState().copy(
            promotionsAndUpdatesAccepted = newValue
        )
        assertEquals(expectedViewState, viewState().withPromotionsAndUpdatesAccepted(newValue))
    }

    /*
    Utils
     */
    private fun viewState() = initialSharedState()
}
