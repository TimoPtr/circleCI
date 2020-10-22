/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class BrushingModeObserverTest : BaseUnitTest() {
    private val synchronizeBrushingModeUseCase: SynchronizeBrushingModeUseCaseImpl = mock()
    private lateinit var connection: InternalKLTBConnection

    private lateinit var brushingModeObserver: BrushingModeObserver

    private fun init(
        model: ToothbrushModel = ToothbrushModel.CONNECT_E2,
        brushingModeStateFlowable: Flowable<BrushingModeState> = Flowable.never()
    ) {
        connection = KLTBConnectionBuilder.createAndroidLess()
            .withModel(model)
            .build()

        val managerMock = mock<BrushingManagerImplMock>()
        whenever(managerMock.brushingModeStateFlowable())
            .thenReturn(brushingModeStateFlowable)
        whenever(connection.brushingMode()).thenReturn(managerMock)

        brushingModeObserver = BrushingModeObserver(
            connection,
            synchronizeBrushingModeUseCase
        )
    }

    /*
    init
     */
    @Test
    fun `init registers as state listener if ToothbrushModel supports brushing mode`() {
        ToothbrushModel.values()
            .filter { it.supportsVibrationSpeedUpdate() }
            .forEach { model ->
                init(model)

                verify(connection.state()).register(brushingModeObserver)
            }
    }

    @Test
    fun `init never registers as state listener if ToothbrushModel doesn't support brushing mode`() {
        ToothbrushModel.values()
            .filterNot { it.supportsVibrationSpeedUpdate() }
            .forEach { model ->
                init(model)

                verify(connection.state(), never()).register(brushingModeObserver)
            }
    }

    /*
    updateCacheOnNewBrushingModeState
     */
    @Test
    fun `synchronizeOnNewBrushingMode disposes previous subscription to device parameters stream`() {
        init()

        val oldDisposable = mock<Disposable>()
        brushingModeObserver.brushingModeChangedDisposable = oldDisposable

        brushingModeObserver.synchronizeOnNewBrushingMode()

        verify(oldDisposable).dispose()
    }

    @Test
    fun `synchronizeOnNewBrushingMode stores subscription to device parameters stream`() {
        val subject = PublishProcessor.create<BrushingModeState>()
        init(brushingModeStateFlowable = subject)

        val oldSubscription = brushingModeObserver.brushingModeChangedDisposable

        brushingModeObserver.synchronizeOnNewBrushingMode()

        assertTrue(subject.hasSubscribers())
        assertNotNull(brushingModeObserver.brushingModeChangedDisposable)
        assertNotSame(oldSubscription, brushingModeObserver.brushingModeChangedDisposable)
    }

    @Test
    fun `synchronizeOnNewBrushingMode invokes synchronizeBrushingMode when brushingModeStateFlowable emits`() {
        val subject = PublishProcessor.create<BrushingModeState>()
        init(brushingModeStateFlowable = subject)

        val synchronizeSubject = CompletableSubject.create()
        whenever(synchronizeBrushingModeUseCase.synchronizeBrushingMode(connection))
            .thenReturn(synchronizeSubject)

        brushingModeObserver.synchronizeOnNewBrushingMode()

        subject.onNext(
            BrushingModeState(
                currentMode = BrushingMode.Regular,
                availableModes = listOf(BrushingMode.Regular),
                lastUpdateDate = TrustedClock.getNowOffsetDateTime()
            )
        )

        verify(synchronizeBrushingModeUseCase).synchronizeBrushingMode(connection)
        assertTrue(synchronizeSubject.hasObservers())

        // verify no crash or anything weird on complete
        synchronizeSubject.onComplete()
    }

    /*
    onConnectionStateChanged
     */
    @Test
    fun `onConnectionStateChanged ACTIVE invokes synchronizeOnNewBrushingMode`() {
        init()

        spyCoordinator()

        doNothing().whenever(brushingModeObserver).synchronizeOnNewBrushingMode()

        brushingModeObserver.onConnectionStateChanged(connection, KLTBConnectionState.ACTIVE)

        verify(brushingModeObserver).synchronizeOnNewBrushingMode()
    }

    @Test
    fun `onConnectionStateChanged newState different than ACTIVE invokes brushingModeChangedDisposable dispose`() {
        init()

        KLTBConnectionState.values()
            .filterNot { it == KLTBConnectionState.ACTIVE }
            .forEach { state ->
                brushingModeObserver.brushingModeChangedDisposable = mock()

                brushingModeObserver.onConnectionStateChanged(connection, state)

                verify(brushingModeObserver.brushingModeChangedDisposable!!).dispose()
            }
    }

    /*
    Utils
     */
    private fun spyCoordinator() {
        brushingModeObserver = spy(brushingModeObserver)
    }

    private interface BrushingManagerImplMock : BrushingModeManager, BrushingModeStateObserver
}
