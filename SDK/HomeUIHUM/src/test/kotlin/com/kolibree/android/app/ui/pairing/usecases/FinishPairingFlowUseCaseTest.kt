/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.usecases

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.pairing.PairingSharedViewModel
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FinishPairingFlowUseCaseTest : BaseUnitTest() {
    private val sharedFacade: PairingSharedViewModel = mock()
    private val confirmConnectionUseCase: ConfirmConnectionUseCase = mock()

    private val useCase = FinishPairingFlowUseCase(sharedFacade, confirmConnectionUseCase)

    @Test
    fun `finish subscribes to confirmConnectionUseCase with parameter value`() {
        val failOnMissingConnection = true
        val subject = CompletableSubject.create()
        whenever(confirmConnectionUseCase.confirm(failOnMissingConnection = failOnMissingConnection))
            .thenReturn(subject)

        useCase.finish(failOnMissingConnection = failOnMissingConnection).test()

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `finish invokes onPairingFlowSuccess on success`() {
        whenever(confirmConnectionUseCase.confirm(any()))
            .thenReturn(Completable.complete())

        useCase.finish(failOnMissingConnection = true).test()

        verify(sharedFacade).onPairingFlowSuccess()
    }

    @Test
    fun `finish never invokes onPairingFlowSuccess on confirm error`() {
        whenever(confirmConnectionUseCase.confirm(any()))
            .thenReturn(Completable.error(TestForcedException()))

        useCase.finish(failOnMissingConnection = true).test()

        verify(sharedFacade, never()).onPairingFlowSuccess()
    }
}
