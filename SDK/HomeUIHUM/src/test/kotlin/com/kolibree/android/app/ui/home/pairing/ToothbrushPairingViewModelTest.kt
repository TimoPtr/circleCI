/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing

import com.jraska.livedata.test
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.onboarding.FakePairingSharedViewModel
import com.kolibree.android.app.ui.pairing.PairingViewState.Companion.initial
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.livedata.TwoWayTestObserver.Companion.testTwoWay
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ToothbrushPairingViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: ToothbrushPairingViewModel

    private val pairingSharedViewModel = FakePairingSharedViewModel()

    override fun setup() {
        super.setup()

        viewModel = ToothbrushPairingViewModel(
            initialViewState = initialViewState(),
            pairingSharedViewModel = pairingSharedViewModel
        )
    }

    /*
    getSharedViewState
     */

    @Test
    fun `getSharedViewState combines ViewModel's viewstate with Pairing's ViewState`() {
        val toothbrushPairingViewState =
            initialViewState().copy(snackbarConfiguration = SnackbarConfiguration(isShown = false))
        val pairingViewState = initial().copy(progressVisible = true)
        pairingSharedViewModel.newViewState(pairingViewState)

        val expectedViewState = toothbrushPairingViewState.copy(pairingViewState = pairingViewState)

        viewModel.updateViewState { toothbrushPairingViewState }

        assertEquals(expectedViewState, viewModel.getSharedViewState())
    }

    /*
    progressVisible
     */

    @Test
    fun `progressVisible updates when pairingSharedViewModel's progress is updated`() {
        val observer = viewModel.progressVisible.test().assertValue(false)

        pairingSharedViewModel.showProgress(false)
        pairingSharedViewModel.showProgress(true)
        pairingSharedViewModel.showProgress(true)
        pairingSharedViewModel.showProgress(false)

        observer.assertValueHistory(false, true, false)
    }

    /*
    updating pairingViewState
     */

    @Test
    fun `updating pairingViewState updates progressVisible`() {
        val observer = viewModel.progressVisible.test()

        updateProgress(true)
        updateProgress(false)

        observer.assertValueHistory(false, true, false)
    }

    @Test
    fun `updating pairingViewState progress visibility to true hides the error`() {
        val initialSnackbarConfiguration = viewModel.getSharedViewState()!!.snackbarConfiguration
        val observer =
            viewModel.snackbarConfiguration.test().assertValue(initialSnackbarConfiguration)

        val error = Error.from("exception")
        viewModel.showError(error)

        var expectedSnackbarConfiguration = SnackbarConfiguration(isShown = true, error = error)
        observer.assertValue(expectedSnackbarConfiguration)

        updateProgress(true)

        expectedSnackbarConfiguration = expectedSnackbarConfiguration.copy(isShown = false)

        observer.assertValue(expectedSnackbarConfiguration)
    }

    @Test
    fun `updating pairingViewState should not touch the error if the progress is done`() {
        val initialSnackbarConfiguration = viewModel.getSharedViewState()!!.snackbarConfiguration
        val observer =
            viewModel.snackbarConfiguration.test().assertValue(initialSnackbarConfiguration)

        val error = Error.from("exception")
        viewModel.showError(error)

        val expectedSnackbarConfiguration = SnackbarConfiguration(isShown = true, error = error)
        observer.assertValue(expectedSnackbarConfiguration)

        updateProgress(false)

        observer.assertValue(expectedSnackbarConfiguration)
    }

    /*
    toolbarBackNavigationEnabled
     */

    @Test
    fun `toolbarBackNavigationEnabled is the opposite of progress state`() {
        val observer = viewModel.toolbarBackNavigationEnabled.test().assertValue(true)

        updateProgress(true)
        observer.assertValue(false)

        updateProgress(false)
        observer.assertValue(true)

        observer.assertValueHistory(true, false, true)
    }

    /*
    hideError
     */

    @Test
    fun `hideError should update the ViewState and hide the error if an error is displayed`() {
        viewModel.showError(Error.from("exception"))
        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)

        viewModel.hideError()

        assertFalse(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `hideError should not update the ViewState if no error is displayed`() {
        val viewState = viewModel.getViewState()
        assertFalse(viewState!!.snackbarConfiguration.isShown)

        viewModel.hideError()

        // Assert references are equals and not updated by updateViewState
        assertTrue(viewState === viewModel.getViewState())
    }

    /*
    showError
     */

    @Test
    fun `showError should update the ViewState and make the SnackbarConfiguration shown`() {
        viewModel.showError(Error.from("exception", Error.ErrorStyle.Indefinite))

        assertTrue(viewModel.getViewState()!!.snackbarConfiguration.isShown)
    }

    @Test
    fun `showError should fail early if the style is AutoDismiss`() {
        FailEarly.overrideDelegateWith { throwable: Throwable, _: () -> Unit ->
            assertEquals(
                throwable.message,
                "This Snackbar Handler should only be used with a LENGTH_INDEFINITE duration"
            )
        }

        viewModel.showError(Error.from("exception", Error.ErrorStyle.AutoDismiss))

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    /*
    snackbarConfig
     */

    @Test
    fun `snackbarConfig offers 2-way binding`() {
        val observer = viewModel.snackbarConfiguration.testTwoWay()
        observer.assertValue(SnackbarConfiguration(false, null))

        val config1 = SnackbarConfiguration(true, Error.from("exception"))
        observer.update(config1)
        observer.assertValue(config1)
        assertEquals(config1, viewModel.getSharedViewState()!!.snackbarConfiguration)

        val config2 = SnackbarConfiguration(false, Error.from("exception2"))
        observer.update(config2)
        observer.assertValue(config2)
        assertEquals(config2, viewModel.getSharedViewState()!!.snackbarConfiguration)
    }

    /*
    Utils
     */
    private fun initialViewState() = ToothbrushPairingViewState.initial()

    private fun updateProgress(value: Boolean) =
        pairingSharedViewModel.newViewState(initial().copy(progressVisible = value))
}
