/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.BrushingProgramToothbrushesUseCase
import com.kolibree.android.sdk.connection.brushingmode.ConfirmBrushingModeUseCase
import com.kolibree.android.sdk.connection.state.KLTBConnectionState
import com.kolibree.android.sdk.core.InternalKLTBConnection
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.ProfileBuilder.DEFAULT_ID
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import org.junit.Test

class QuizConfirmationUseCaseTest : BaseUnitTest() {
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val brushingProgramToothbrushesUseCase: BrushingProgramToothbrushesUseCase = mock()
    private val confirmUserModeUseCase: ConfirmBrushingModeUseCase = mock()
    private val timeScheduler = TestScheduler()

    private lateinit var quizConfirmationUseCase: QuizConfirmationUseCase

    /*
    tryOutBrushingModeCompletable
     */

    @Test
    fun `tryOutBrushingModeCompletable reads toothbrushes for active profile `() {
        mockCurrentProfile()
        mockToothbrushesForProfile()

        initUseCase()

        quizConfirmationUseCase.tryOutBrushingModeCompletable({}, {}).test()

        verify(brushingProgramToothbrushesUseCase).toothbrushesWithBrushingProgramSupport(DEFAULT_ID)
    }

    @Test
    fun `tryOutBrushingModeCompletable never invokes tryOutBrushingModeCompletable if profile has no toothbrushes with brushing program support`() {
        mockCurrentProfile()
        mockToothbrushesForProfile()

        spyUseCase()

        quizConfirmationUseCase.tryOutBrushingModeCompletable({}, {}).test()

        verify(quizConfirmationUseCase, never()).tryOutBrushingModeCompletable(any())
    }

    @Test
    fun `tryOutBrushingModeCompletable returns Completable error if profile has no toothbrushes with brushing program support`() {
        mockCurrentProfile()
        mockToothbrushesForProfile()

        initUseCase()

        quizConfirmationUseCase.tryOutBrushingModeCompletable({}, {}).test()
            .assertError(NoToothbrushWithBrushingProgramException)
    }

    @Test
    fun `tryOutBrushingModeCompletable never invokes tryOutBrushingModeCompletable if profile has no ACTIVE toothbrushes with brushing program support`() {
        mockCurrentProfile()

        val nonActiveConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build()
        mockToothbrushesForProfile(toothbrushes = listOf(nonActiveConnection))

        spyUseCase()

        quizConfirmationUseCase.tryOutBrushingModeCompletable({}, {}).test()

        verify(quizConfirmationUseCase, never()).tryOutBrushingModeCompletable(any())
    }

    @Test
    fun `tryOutBrushingModeCompletable returns Completable error if profile has no ACTIVE toothbrushes with brushing program support`() {
        mockCurrentProfile()

        val nonActiveConnection = KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .build()
        mockToothbrushesForProfile(toothbrushes = listOf(nonActiveConnection))

        initUseCase()

        quizConfirmationUseCase.tryOutBrushingModeCompletable({}, {}).test()
            .assertError(NoToothbrushWithBrushingProgramException)
    }

    @Test
    fun `tryOutBrushingModeCompletable invokes tryOutBrushingModeCompletable with first ACTIVE KLTBConnection for profile`() {
        mockCurrentProfile()

        val expectedConnection = realConnection(active = true)
        val nonActiveConnection = realConnection(active = false)
        mockToothbrushesForProfile(toothbrushes = listOf(expectedConnection, nonActiveConnection))

        spyUseCase()

        doReturn(CompletableSubject.create())
            .whenever(quizConfirmationUseCase)
            .tryOutBrushingModeCompletable(any())

        quizConfirmationUseCase.tryOutBrushingModeCompletable({}, {}).test()

        verify(quizConfirmationUseCase).tryOutBrushingModeCompletable(expectedConnection)
    }

    @Test
    fun `tryOutBrushingModeCompletable returns Completable by tryOutBrushingModeCompletable if user has ACTIVE toothbrushes`() {
        mockCurrentProfile()

        mockToothbrushesForProfile(toothbrushes = listOf(realConnection(active = true)))

        spyUseCase()

        val completableSubject = CompletableSubject.create()
        doReturn(completableSubject)
            .whenever(quizConfirmationUseCase)
            .tryOutBrushingModeCompletable(any())

        val observer =
            quizConfirmationUseCase.tryOutBrushingModeCompletable({}, {}).test().assertNotComplete()

        completableSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `tryOutBrushingModeCompletable invokes onSubscribeBlock before toothbrushesWithBrushingProgramSupport`() {
        mockCurrentProfile()

        mockToothbrushesForProfile(toothbrushes = listOf(realConnection()))

        spyUseCase()

        doReturn(CompletableSubject.create())
            .whenever(quizConfirmationUseCase)
            .tryOutBrushingModeCompletable(any())

        val onSubscribeCallback = mock<DummyCallback>()

        quizConfirmationUseCase.tryOutBrushingModeCompletable({ onSubscribeCallback.invoke() }, {})
            .test()

        inOrder(onSubscribeCallback, brushingProgramToothbrushesUseCase) {
            verify(onSubscribeCallback).invoke()
            verify(brushingProgramToothbrushesUseCase).toothbrushesWithBrushingProgramSupport(
                DEFAULT_ID
            )
        }
    }

    @Test
    fun `tryOutBrushingModeCompletable invokes doFinally block after tryOutBrushingModeCompletable`() {
        mockCurrentProfile()

        val connection = realConnection(active = true)
        mockToothbrushesForProfile(toothbrushes = listOf(connection))

        spyUseCase()

        doReturn(Completable.complete())
            .whenever(quizConfirmationUseCase)
            .tryOutBrushingModeCompletable(any())

        val doFinallyCallback = mock<DummyCallback>()

        quizConfirmationUseCase.tryOutBrushingModeCompletable({ }, { doFinallyCallback.invoke() })
            .test()

        inOrder(doFinallyCallback, quizConfirmationUseCase) {
            verify(quizConfirmationUseCase).tryOutBrushingModeCompletable(connection)
            verify(doFinallyCallback).invoke()
        }
    }

    @Test
    fun `tryOutBrushingModeCompletable sets selected brushing mode`() {
        val initialBrushingMode = BrushingMode.Regular
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBrushingMode(listOf(), initialBrushingMode)
            .withSupportVibrationCommands()
            .build()

        val selectedBrushingMode = BrushingMode.Slow
        initUseCase(brushingMode = selectedBrushingMode)

        quizConfirmationUseCase.tryOutBrushingModeCompletable(connection).test()

        inOrder(connection.brushingMode(), connection.vibrator()) {
            verify(connection.brushingMode()).set(selectedBrushingMode)
            verify(connection.vibrator()).on()
        }

        verify(connection.vibrator(), never()).off()
    }

    @Test
    fun `tryOutBrushingModeCompletable saves old brushing mode`() {
        val initialBrushingMode = BrushingMode.Regular
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBrushingMode(listOf(), initialBrushingMode)
            .withSupportVibrationCommands()
            .build()

        val selectedBrushingMode = BrushingMode.Slow
        initUseCase(brushingMode = selectedBrushingMode)

        quizConfirmationUseCase.oldBrushingMode.set(initialBrushingMode)
        quizConfirmationUseCase
            .tryOutBrushingModeCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()

        assertEquals(initialBrushingMode, quizConfirmationUseCase.oldBrushingMode.get())
    }

    @Test
    fun `tryOutBrushingModeCompletable called twice only saves old brushing mode once`() {
        val initialBrushingMode = BrushingMode.Regular
        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withBrushingMode(listOf(), initialBrushingMode)
            .withSupportVibrationCommands()
            .build()

        val selectedBrushingMode = BrushingMode.Slow
        initUseCase(brushingMode = selectedBrushingMode)

        quizConfirmationUseCase.oldBrushingMode.set(initialBrushingMode)

        // First call
        quizConfirmationUseCase
            .tryOutBrushingModeCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()

        assertEquals(initialBrushingMode, quizConfirmationUseCase.oldBrushingMode.get())

        // Second call
        quizConfirmationUseCase
            .tryOutBrushingModeCompletable(connection)
            .test()
            .assertComplete()
            .assertNoErrors()

        // oldBrushingMode did not become "Slow"
        assertEquals(initialBrushingMode, quizConfirmationUseCase.oldBrushingMode.get())
    }

    @Test
    fun `tryOutBrushingModeCompletable gets terminated after timeout`() {
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.never())

        initUseCase()

        val testObserver = quizConfirmationUseCase.tryOutBrushingModeCompletable({}, {}).test()
        testObserver.assertNotTerminated()

        timeScheduler.advanceTimeBy(6, TimeUnit.SECONDS)
        testObserver.assertTerminated()
    }

    /*
    maybeRevertBrushingModeCompletable
     */

    @Test
    fun `maybeRevertBrushingModeCompletable does nothing if oldBrushingMode is null`() {
        initUseCase()

        quizConfirmationUseCase
            .maybeRevertBrushingModeCompletable({}, {})
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(confirmUserModeUseCase, never()).confirmBrushingModeCompletable(any(), any())
    }

    @Test
    fun `maybeRevertBrushingModeCompletable calls confirmBrushingModeCompletable if not null`() {
        val oldBrushingMode = BrushingMode.Strong
        val profileId = 1986L
        whenever(confirmUserModeUseCase.confirmBrushingModeCompletable(any(), any()))
            .thenReturn(Completable.complete())
        mockCurrentProfile(ProfileBuilder.create().withId(profileId).build())
        initUseCase(oldBrushingMode)

        quizConfirmationUseCase.oldBrushingMode.set(oldBrushingMode)
        quizConfirmationUseCase
            .confirmBrushingModeCompletable({}, {})
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertNoValues()

        verify(confirmUserModeUseCase).confirmBrushingModeCompletable(profileId, oldBrushingMode)
    }

    /*
    confirmBrushingModeCompletable
     */

    @Test
    fun `confirmBrushingModeCompletable invokes confirmBrushingModeCompletable`() {
        mockCurrentProfile()

        val selectedBrushingMode = BrushingMode.Slow
        initUseCase(brushingMode = selectedBrushingMode)

        quizConfirmationUseCase.confirmBrushingModeCompletable({}, {}).test()

        verify(confirmUserModeUseCase).confirmBrushingModeCompletable(
            DEFAULT_ID,
            selectedBrushingMode
        )
    }

    @Test
    fun `confirmBrushingModeCompletable invokes onSubscribeBlock before confirmBrushingModeCompletable`() {
        mockCurrentProfile()

        val onSubscribeCallback = mock<DummyCallback>()

        initUseCase()

        quizConfirmationUseCase.confirmBrushingModeCompletable({ onSubscribeCallback.invoke() }, {})
            .test()

        inOrder(onSubscribeCallback, confirmUserModeUseCase) {
            verify(onSubscribeCallback).invoke()
            verify(confirmUserModeUseCase).confirmBrushingModeCompletable(
                DEFAULT_ID,
                defaultBrushingMode
            )
        }
    }

    @Test
    fun `confirmBrushingModeCompletable invokes doFinally block after confirmBrushingModeCompletable`() {
        mockCurrentProfile()

        val doFinallyCallback = mock<DummyCallback>()

        initUseCase()

        quizConfirmationUseCase.confirmBrushingModeCompletable({ }, { doFinallyCallback.invoke() })
            .test()

        inOrder(doFinallyCallback, confirmUserModeUseCase) {
            verify(confirmUserModeUseCase).confirmBrushingModeCompletable(
                DEFAULT_ID,
                defaultBrushingMode
            )
            verify(doFinallyCallback).invoke()
        }
    }

    /*
    UTILS
     */

    private interface DummyCallback : () -> Unit

    private fun initUseCase(brushingMode: BrushingMode = defaultBrushingMode) {
        quizConfirmationUseCase = QuizConfirmationUseCase(
            brushingMode,
            currentProfileProvider,
            brushingProgramToothbrushesUseCase,
            confirmUserModeUseCase,
            timeScheduler
        )
    }

    private fun spyUseCase(brushingMode: BrushingMode = defaultBrushingMode) {
        initUseCase(brushingMode)

        quizConfirmationUseCase = spy(quizConfirmationUseCase)
    }

    companion object {
        internal fun mockToAvoidCrash(confirmCompletable: Completable = Completable.complete()): QuizConfirmationUseCase {
            val test = QuizConfirmationUseCaseTest()

            test.mockCurrentProfile()
            test.mockToothbrushesForProfile()
            test.mockConfirm(completable = confirmCompletable)

            test.initUseCase()

            return test.quizConfirmationUseCase
        }
    }

    private fun mockCurrentProfile(profile: Profile = ProfileBuilder.create().build()) {
        whenever(currentProfileProvider.currentProfileSingle()).thenReturn(Single.just(profile))
    }

    fun mockToothbrushesForProfile(
        toothbrushes: List<KLTBConnection> = listOf(),
        profileId: Long = DEFAULT_ID
    ) {
        whenever(brushingProgramToothbrushesUseCase.toothbrushesWithBrushingProgramSupport(profileId))
            .thenReturn(Single.just(toothbrushes))
    }

    internal fun mockConfirm(
        profileId: Long = DEFAULT_ID,
        brushingMode: BrushingMode = defaultBrushingMode,
        completable: Completable = Completable.complete()
    ) {
        whenever(confirmUserModeUseCase.confirmBrushingModeCompletable(profileId, brushingMode))
            .thenReturn(completable)
    }

    private fun realConnection(active: Boolean = false): InternalKLTBConnection {
        return KLTBConnectionBuilder.createAndroidLess()
            .withState(if (active) KLTBConnectionState.ACTIVE else KLTBConnectionState.TERMINATED)
            .build()
    }
}

internal val defaultBrushingMode = BrushingMode.Strong
