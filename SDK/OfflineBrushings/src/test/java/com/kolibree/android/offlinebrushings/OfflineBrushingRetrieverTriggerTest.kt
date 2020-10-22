/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushing.BrushingSessionMonitor
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class OfflineBrushingRetrieverTriggerTest : BaseUnitTest() {

    private val activeConnectionUseCase: ActiveConnectionUseCase = mock()

    private val trigger = OfflineBrushingsRetrieverTrigger(activeConnectionUseCase)

    @Test
    fun `trigger emits when there is a new active connection whatever is the brushing session status`() {

        val sessionMonitor: BrushingSessionMonitor = mock()
        val relay = PublishProcessor.create<KLTBConnection>()
        whenever(sessionMonitor.sessionMonitorStream).thenReturn(Flowable.never())

        whenever(activeConnectionUseCase.onConnectionsUpdatedStream()).thenReturn(relay)
        val testObserver = trigger.trigger.test().assertNotComplete().assertNoValues()

        relay.onNext(
            KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE)
                .withBrushingSessionMonitor(sessionMonitor)
                .build()
        )

        testObserver.assertValueCount(1).assertNotComplete()
    }

    @Test
    fun `trigger emits when there is a brushing session end on an active connection`() {
        val sessionMonitor: BrushingSessionMonitor = mock()
        val brushingSessionMonitorStream = PublishProcessor.create<Boolean>()
        val connection = KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE)
            .withBrushingSessionMonitor(sessionMonitor)
            .build()

        whenever(sessionMonitor.sessionMonitorStream).thenReturn(brushingSessionMonitorStream)

        whenever(activeConnectionUseCase.onConnectionsUpdatedStream()).thenReturn(Flowable.just(connection))

        val testObserver = trigger.trigger.test().assertNotComplete().assertValueCount(1)

        brushingSessionMonitorStream.onNext(true)

        testObserver.assertValueCount(1).assertNotComplete()

        brushingSessionMonitorStream.onNext(false)

        testObserver.assertValueCount(2).assertNotComplete()

        brushingSessionMonitorStream.onNext(false)

        testObserver.assertValueCount(3).assertNotComplete()
    }

    @Test
    fun `unsubscribe from the stateStream of a connection when not active anymore`() {
        val sessionMonitor: BrushingSessionMonitor = mock()
        val brushingSessionMonitorStream = PublishProcessor.create<Boolean>()
        val connection = KLTBConnectionBuilder.createAndroidLess().withState(KLTBConnectionState.ACTIVE)
            .withBrushingSessionMonitor(sessionMonitor)
            .build()
        val stateStream = PublishProcessor.create<KLTBConnectionState>()

        whenever(connection.state().stateStream).thenReturn(stateStream)

        whenever(sessionMonitor.sessionMonitorStream).thenReturn(brushingSessionMonitorStream)

        whenever(activeConnectionUseCase.onConnectionsUpdatedStream()).thenReturn(Flowable.just(connection))

        val testObserver = trigger.trigger.test()

        stateStream.onNext(KLTBConnectionState.ACTIVE)

        testObserver.assertNotComplete().assertValueCount(1)

        assertTrue(stateStream.hasSubscribers())

        stateStream.onNext(KLTBConnectionState.ESTABLISHING)

        assertFalse(stateStream.hasSubscribers())
    }
}
