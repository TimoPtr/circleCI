/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.usecases

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCase
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertTrue
import org.junit.Test

class OnConnectionActiveUseCaseTest : BaseUnitTest() {

    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCase = mock()
    private val persistedToothbrushRefreshUseCase: PersistedToothbrushRefreshUseCase = mock()

    private val useCase =
        OnConnectionActiveUseCase(
            synchronizeBrushingModeUseCase = synchronizeBrushingModeUseCase,
            persistedToothbrushRefreshUseCase = persistedToothbrushRefreshUseCase
        )

    private val brushingModeCompletable = CompletableSubject.create()
    private val persistedVersionsCompletable = CompletableSubject.create()
    private val connection = KLTBConnectionBuilder.createAndroidLess().build()

    override fun setup() {
        super.setup()

        whenever(synchronizeBrushingModeUseCase.synchronizeBrushingMode(connection))
            .thenReturn(brushingModeCompletable)

        whenever(persistedToothbrushRefreshUseCase.maybeUpdateVersions(connection))
            .thenReturn(persistedVersionsCompletable)
    }

    @Test
    fun `apply subscribes to everyUsecase`() {
        useCase.apply(connection).test()

        assertTrue(brushingModeCompletable.hasObservers())
        assertTrue(persistedVersionsCompletable.hasObservers())
    }

    @Test
    fun `apply completes only after every internal usecase completes`() {
        val observer = useCase.apply(connection).test().assertNotComplete()

        persistedVersionsCompletable.onComplete()

        observer.assertNotComplete()

        brushingModeCompletable.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `apply delays error emission until all usescases have completed`() {
        val observer = useCase.apply(connection).test().assertNotComplete()

        persistedVersionsCompletable.onError(TestForcedException())

        observer.assertNotComplete().assertNoErrors()

        brushingModeCompletable.onComplete()

        observer.assertError(TestForcedException::class.java)
    }
}
