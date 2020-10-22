/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.brush_found

import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.PairingNavigator
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.FINISH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.MODEL_MISMATCH
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.NO_BLINKING_CONNECTION
import com.kolibree.android.app.ui.pairing.usecases.ConnectionConfirmedNavigationAction.SIGN_UP
import com.kolibree.android.app.ui.pairing.usecases.NextNavigationActionUseCase
import com.kolibree.android.app.ui.pairing.wake_your_brush.mockUnpairblinking
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.pushLifecycleTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class BrushFoundViewModelTest : BaseUnitTest() {

    private val sharedFacade: PairingFlowSharedFacade = mock()
    private val navigator: PairingNavigator = mock()
    private val nextNavigationActionUseCase: NextNavigationActionUseCase = mock()
    private val brushFoundConfirmConnectionUseCase: BrushFoundConfirmConnectionUseCase = mock()
    private val timeoutScheduler = TestScheduler()

    private lateinit var viewModel: BrushFoundViewModel

    override fun setup() {
        super.setup()

        viewModel =
            BrushFoundViewModel(
                pairingFlowSharedFacade = sharedFacade,
                navigator = navigator,
                brushFoundConfirmConnectionUseCase = brushFoundConfirmConnectionUseCase,
                timeoutScheduler = timeoutScheduler,
                nextNavigationActionUseCase = nextNavigationActionUseCase
            )
    }

    /*
    onResume
     */

    @Test
    fun `onResume navigates to WakeYourBrush if blinkingConnection is null`() {
        assertNull(sharedFacade.blinkingConnection())

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(navigator).navigateFromBrushFoundToWakeYourBrush()
    }

    @Test
    fun `onResume never navigates to WakeYourBrush if blinkingConnection is not null`() {
        mockBlinkingConnection()

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_RESUME)

        verify(navigator, never()).navigateFromBrushFoundToWakeYourBrush()
    }

    /*
    confirmConnection
     */
    @Test
    fun `when confirm completes successfully and ConnectionConfirmedUseCase returns NO_BLINKING_CONNECTION, navigate to Wake your brush`() {
        whenever(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
            .thenReturn(Completable.complete())

        mockConnectionConfirmedUseCase(NO_BLINKING_CONNECTION)

        viewModel.confirmConnectionClick()

        advanceFakeDialogTime()

        verify(navigator).navigateFromBrushFoundToWakeYourBrush()
    }

    @Test
    fun `when confirm completes successfully and ConnectionConfirmedUseCase returns SIGN_UP, navigate to signup screen and showProgress false`() {
        whenever(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
            .thenReturn(Completable.complete())

        mockConnectionConfirmedUseCase(SIGN_UP)

        viewModel.confirmConnectionClick()

        verify(sharedFacade).showProgress(true)
        verify(sharedFacade, never()).showProgress(false)

        verify(navigator, never()).navigateFromBrushFoundToSignUp()

        advanceFakeDialogTime()

        verify(navigator).navigateFromBrushFoundToSignUp()
        verify(sharedFacade).showProgress(false)
    }

    @Test
    fun `when confirm completes successfully and ConnectionConfirmedUseCase returns FINISH, finish flow and showProgress false`() {
        whenever(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
            .thenReturn(Completable.complete())

        mockConnectionConfirmedUseCase(FINISH)

        viewModel.confirmConnectionClick()

        verify(sharedFacade).showProgress(true)
        verify(sharedFacade, never()).showProgress(false)

        verify(navigator, never()).navigateFromBrushFoundToSignUp()

        advanceFakeDialogTime()

        verify(navigator).finishFlow()
        verify(sharedFacade).showProgress(false)
    }

    @Test
    fun `when confirm completes successfully and ConnectionConfirmedUseCase returns MODEL_MISTACH, navigate to navigateToToothbrushModelMismatch and showProgress false`() {
        whenever(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
            .thenReturn(Completable.complete())

        mockConnectionConfirmedUseCase(MODEL_MISMATCH)

        viewModel.confirmConnectionClick()

        verify(sharedFacade).showProgress(true)
        verify(sharedFacade, never()).showProgress(false)

        verify(navigator, never()).navigateFromBrushFoundToSignUp()

        advanceFakeDialogTime()

        verify(navigator).navigateToToothbrushModelMismatch()
        verify(sharedFacade).showProgress(false)
    }

    @Test
    fun `when confirm errors, navigate to wake your brush and showProgress false`() {
        whenever(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
            .thenReturn(Completable.error(TestForcedException()))

        viewModel.confirmConnectionClick()

        verify(sharedFacade).showProgress(true)
        verify(sharedFacade, never()).showProgress(false)

        advanceFakeDialogTime()

        verify(navigator).navigateFromBrushFoundToWakeYourBrush()

        verify(sharedFacade).showProgress(true)
    }

    @Test
    fun `when confirm errors, show error and showProgress false`() {
        whenever(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
            .thenReturn(Completable.error(TestForcedException()))

        viewModel.confirmConnectionClick()

        verify(sharedFacade).showProgress(true)
        verify(sharedFacade, never()).showProgress(false)

        advanceFakeDialogTime()

        verify(sharedFacade).showError(Error.from(R.string.pairing_something_went_wrong))

        verify(sharedFacade).showProgress(true)
    }

    @Test
    fun `confirmConnectionClick invokes showProgress true when it is called`() {
        whenever(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
            .thenReturn(Completable.never())

        viewModel.confirmConnectionClick()

        verify(sharedFacade).showProgress(true)
        verify(sharedFacade, never()).showProgress(false)
    }

    @Test
    fun `confirmConnectionClick invokes send connect analytics`() {
        whenever(brushFoundConfirmConnectionUseCase.maybeConfirmConnection())
            .thenReturn(Completable.never())

        viewModel.confirmConnectionClick()
        verify(eventTracker).sendEvent(BrushFoundAnalytics.connect())
    }

    /*
    notRightConnectionClick
     */

    @Test
    fun `notRightConnectionClick invokes send notRightToothbrush analytics`() {
        mockUnpairblinking()

        viewModel.notRightConnectionClick()

        verify(eventTracker).sendEvent(BrushFoundAnalytics.notRightToothbrush())
    }

    @Test
    fun `notRightConnectionClick navigates to ListToothbrush screen`() {
        mockUnpairblinking()

        viewModel.notRightConnectionClick()

        verify(navigator).navigateToScanList()
    }

    @Test
    fun `notRightConnectionClick unpairs blinking connection`() {
        val unpairSubject = mockUnpairblinking()

        viewModel.notRightConnectionClick()

        assertTrue(unpairSubject.hasObservers())
    }

    /*
    Utils
     */

    private fun mockBlinkingConnection() {
        sharedFacade.mockBlinkingConnection()
    }

    private fun advanceFakeDialogTime() {
        timeoutScheduler.advanceTimeBy(SHOW_FAKE_CONNECTING_DIALOG_SECONDS, TimeUnit.SECONDS)
    }

    private fun mockUnpairblinking(): CompletableSubject {
        return sharedFacade.mockUnpairblinking()
    }

    private fun mockConnectionConfirmedUseCase(valueToReturn: ConnectionConfirmedNavigationAction) {
        whenever(nextNavigationActionUseCase.nextNavitationStep()).thenReturn(valueToReturn)
    }
}

internal fun PairingFlowSharedFacade.mockBlinkingConnection(connection: KLTBConnection = mock()) {
    whenever(blinkingConnection()).thenReturn(connection)
}
