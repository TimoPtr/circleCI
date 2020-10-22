/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadReplacedDateWriter
import com.kolibree.android.app.ui.brushhead.sync.brushHeadInfo
import com.kolibree.android.app.ui.pairing.usecases.ConfirmConnectionUseCase
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadWorkerDateConfigurator
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadWorkerDateConfigurator.Payload
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.wrapper.ToothbrushFacade
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.pairing.assistant.PairingAssistant
import com.kolibree.pairing.session.PairingSession
import com.kolibree.pairing.usecases.UpdateToothbrushUseCase
import com.kolibree.sdkws.api.response.UpdateToothbrushResponse
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ConfirmConnectionUseCaseTest : BaseUnitTest() {
    private val pairingFlowSharedFacade: PairingFlowSharedFacade = mock()
    private val pairingAssistant: PairingAssistant = mock()
    private val updateToothbrushUseCase: UpdateToothbrushUseCase = mock()
    private val brushHeadReplacedDateWriter: BrushHeadReplacedDateWriter = mock()
    private val brushHeadWorkerDateConfigurator: SyncBrushHeadWorkerDateConfigurator = mock()

    private val useCase =
        ConfirmConnectionUseCase(
            pairingFlowSharedFacade = pairingFlowSharedFacade,
            pairingAssistant = pairingAssistant,
            updateToothbrushUseCase = updateToothbrushUseCase,
            brushHeadReplacedDateWriter = brushHeadReplacedDateWriter,
            brushHeadWorkerDateConfigurator = brushHeadWorkerDateConfigurator
        )

    override fun setup() {
        super.setup()

        whenever(brushHeadReplacedDateWriter.writeReplacedDateNow(any()))
            .thenReturn(Single.just(brushHeadInfo()))
    }

    @Test
    fun `confirm associates blinking connection to profile`() {
        val connection = mockBlinkingConnection()

        useCase.confirm(failOnMissingConnection = true).test()

        verify(pairingAssistant).pair(
            connection.toothbrush().mac,
            connection.toothbrush().model,
            connection.toothbrush().getName()
        )
    }

    @Test
    fun `when connection is paired head brush replaced date is updated`() {
        val connection = mockBlinkingConnection()

        val pairSubject = mockPairToothbrush(connection)
        useCase.confirm(failOnMissingConnection = true).test()
        pairSubject.onComplete()

        verify(brushHeadReplacedDateWriter).writeReplacedDateNow(KLTBConnectionBuilder.DEFAULT_MAC)
    }

    @Test
    fun `when connection is paired head brush configurator should be called`() {
        val connection = mockBlinkingConnection()

        val pairSubject = mockPairToothbrush(connection)
        useCase.confirm(failOnMissingConnection = true).test()
        pairSubject.onComplete()

        verify(brushHeadWorkerDateConfigurator).configure(
            Payload(KLTBConnectionBuilder.DEFAULT_MAC, KLTBConnectionBuilder.DEFAULT_SERIAL)
        )
    }

    @Test
    fun `confirm never invokes success`() {
        val connection = mockBlinkingConnection()

        val pairSubject = mockPairToothbrush(connection)

        val updateToothbrushSubject = SingleSubject.create<UpdateToothbrushResponse>()
        whenever(updateToothbrushUseCase.updateToothbrush(connection))
            .thenReturn(updateToothbrushSubject)

        val observer = useCase.confirm(failOnMissingConnection = true).test().assertNotComplete()

        pairSubject.onComplete()

        observer.assertNotComplete()

        updateToothbrushSubject.onSuccess(mock())

        observer.assertComplete()

        verify(pairingFlowSharedFacade, never()).onPairingFlowSuccess()
    }

    @Test
    fun `when connection is not on bootloader, confirm invokes update toothbrush use case after pairing the connection`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBootloader(false)
            .build()

        mockBlinkingConnection(connection)

        val pairSubject = mockPairToothbrush(connection)

        val updateToothbrushSubject = SingleSubject.create<UpdateToothbrushResponse>()
        whenever(updateToothbrushUseCase.updateToothbrush(connection))
            .thenReturn(updateToothbrushSubject)

        val observer = useCase.confirm(failOnMissingConnection = true).test()

        assertFalse(updateToothbrushSubject.hasObservers())

        pairSubject.onComplete()

        assertTrue(updateToothbrushSubject.hasObservers())

        observer.assertNotComplete()

        updateToothbrushSubject.onSuccess(mock())

        observer.assertComplete()
    }

    @Test
    fun `when connection is on bootloader, confirm never invokes update toothbrush and completes normally`() {
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBootloader(true)
            .build()

        mockBlinkingConnection(connection)

        val pairSubject = mockPairToothbrush(connection)

        val updateToothbrushSubject = SingleSubject.create<UpdateToothbrushResponse>()
        whenever(updateToothbrushUseCase.updateToothbrush(connection))
            .thenReturn(updateToothbrushSubject)

        val observer = useCase.confirm(failOnMissingConnection = true).test()

        pairSubject.onComplete()

        assertFalse(updateToothbrushSubject.hasObservers())

        observer.assertComplete()
    }

    /*
    No blinking connection stored
     */

    @Test(expected = AssertionError::class)
    fun `confirm throws AssertionError if there's no connection and failOnMissingConnection is true`() {
        assertNull(pairingFlowSharedFacade.blinkingConnection())

        useCase.confirm(failOnMissingConnection = true).test().assertComplete().assertNoErrors()
    }

    @Test
    fun `confirm never throws AssertionError if there's no connection and failOnMissingConnection is false`() {
        assertNull(pairingFlowSharedFacade.blinkingConnection())

        useCase.confirm(failOnMissingConnection = false).test().assertComplete()
    }

    @Test
    fun `confirm doesn't emit backend errors from update toothbrush calls`() {
        whenever(updateToothbrushUseCase.updateToothbrush(any()))
            .thenReturn(Single.error(Exception()))

        mockPairToothbrush(KLTBConnectionBuilder.createAndroidLess().build()).onComplete()
        mockBlinkingConnection()

        useCase.confirm(failOnMissingConnection = true).test().assertComplete()
    }

    /*
    Utils
     */

    private fun mockBlinkingConnection(
        connection: KLTBConnection = KLTBConnectionBuilder.createAndroidLess().build()
    ): KLTBConnection {
        whenever(pairingFlowSharedFacade.blinkingConnection()).thenReturn(connection)
        return connection
    }

    private fun mockPairToothbrush(
        connection: KLTBConnection
    ): CompletableSubject {
        val pairSubject = CompletableSubject.create()
        val mockedPairingSession: PairingSession = mockPairingSession()
        whenever(
            pairingAssistant.pair(
                connection.toothbrush().mac,
                connection.toothbrush().model,
                connection.toothbrush().getName()
            )
        ).thenReturn(pairSubject.andThen(Single.just(mockedPairingSession)))

        return pairSubject
    }

    private fun mockPairingSession(): PairingSession {
        val pairingSession: PairingSession = mock()
        val toothbrushFacade: ToothbrushFacade = mock()
        whenever(toothbrushFacade.getMac()).thenReturn(KLTBConnectionBuilder.DEFAULT_MAC)
        whenever(toothbrushFacade.getSerialNumber()).thenReturn(KLTBConnectionBuilder.DEFAULT_SERIAL)
        whenever(pairingSession.toothbrush()).thenReturn(toothbrushFacade)
        return pairingSession
    }
}
