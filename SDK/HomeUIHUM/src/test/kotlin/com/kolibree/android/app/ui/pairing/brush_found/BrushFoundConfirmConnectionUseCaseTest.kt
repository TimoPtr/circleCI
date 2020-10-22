/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.brush_found

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingFlowSharedFacade
import com.kolibree.android.app.ui.pairing.usecases.ConfirmConnectionUseCase
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class BrushFoundConfirmConnectionUseCaseTest : BaseUnitTest() {

    private val pairingFacade: PairingFlowSharedFacade = mock()
    private val confirmConnectionUseCase: ConfirmConnectionUseCase = mock()

    private val useCase =
        BrushFoundConfirmConnectionUseCase(pairingFacade, confirmConnectionUseCase)

    @Test
    fun `when connection is null and host is not OnBoardingFlow flow, useCase does not crash`() {
        assertNull(pairingFacade.blinkingConnection())

        whenever(pairingFacade.isOnboardingFlow()).thenReturn(false)

        mockConfirm().onComplete()

        useCase.maybeConfirmConnection().test().assertComplete()
    }

    @Test
    fun `when connection is null and host is OnBoardingFlow flow, useCase does not crash`() {
        assertNull(pairingFacade.blinkingConnection())

        whenever(pairingFacade.isOnboardingFlow()).thenReturn(true)

        useCase.maybeConfirmConnection().test().assertComplete()
    }

    @Test
    fun `when connection is not null and host is not OnBoardingFlow, confirm attempts to turn off vibration before confirming`() {
        whenever(pairingFacade.isOnboardingFlow()).thenReturn(false)

        val connection = mockConnection()

        val inOrder = inOrder(connection.vibrator(), confirmConnectionUseCase)

        mockConfirm().onComplete()

        useCase.maybeConfirmConnection().test().assertComplete()

        inOrder.verify(connection.vibrator()).off()

        inOrder.verify(confirmConnectionUseCase).confirm(true)
    }

    @Test
    fun `when connection is not null and host is OnBoardingFlow, confirm attempts to turn off vibration before completing`() {
        whenever(pairingFacade.isOnboardingFlow()).thenReturn(true)

        val connection = mockConnection()

        mockConfirm()

        useCase.maybeConfirmConnection().test().assertComplete()

        verify(connection.vibrator()).off()
    }

    @Test
    fun `confirm is never invoked if host is OnboardingFlow`() {
        whenever(pairingFacade.isOnboardingFlow()).thenReturn(true)

        val confirmSubject = mockConfirm()

        useCase.maybeConfirmConnection().test()

        assertFalse(confirmSubject.hasObservers())
    }

    @Test
    fun `confirm is invoked if host is not OnboardingFlow`() {
        whenever(pairingFacade.isOnboardingFlow()).thenReturn(false)

        val confirmSubject = mockConfirm()

        useCase.maybeConfirmConnection().test()

        assertTrue(confirmSubject.hasObservers())
    }

    /*
    Utils
     */
    private fun mockConfirm(): CompletableSubject {
        val subject = CompletableSubject.create()
        whenever(confirmConnectionUseCase.confirm(any())).thenReturn(subject)

        return subject
    }

    private fun mockConnection(): InternalKLTBConnection {
        val connection =
            KLTBConnectionBuilder.createAndroidLess().withSupportVibrationCommands().build()

        whenever(pairingFacade.blinkingConnection()).thenReturn(connection)

        return connection
    }
}
