/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.smilescounter

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmileCounterChangedUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Error
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Pending
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterStateMerger
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterStateProvider
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterStateProviderImpl
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterVisibilityUseCase
import com.kolibree.android.app.ui.home.tab.home.smilescounter.UserExpectsSmilesUseCase
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.rewards.SmilesUseCase
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SmilesCounterStateProviderTest : BaseUnitTest() {
    private val smilesCounterVisibilityUseCase: SmilesCounterVisibilityUseCase = mock()
    private val userExpectsSmilesUseCase: UserExpectsSmilesUseCase = mock()
    private val smilesUseCase: SmilesUseCase = mock()
    private val networkChecker: NetworkChecker = mock()
    private val smilesCounterStateMerger = FakeSmilesCounterStateMerger()
    private val smileCounterChangedUseCase: SmileCounterChangedUseCase = mock()

    private val smilesProcessor = PublishProcessor.create<Int>()
    private val userExpectsSmilesSubject = PublishSubject.create<Boolean>()
    private val smilesCounterVisibilitySubject = PublishSubject.create<Boolean>()
    private val networkStateSubject = PublishSubject.create<Boolean>()

    private val debounceScheduler = TestScheduler()

    private lateinit var provider: SmilesCounterStateProvider

    override fun setup() {
        super.setup()

        whenever(smilesUseCase.smilesAmountStream())
            .thenReturn(smilesProcessor)

        whenever(userExpectsSmilesUseCase.onceAndStream)
            .thenReturn(userExpectsSmilesSubject)

        whenever(smilesCounterVisibilityUseCase.onceAndStream)
            .thenReturn(smilesCounterVisibilitySubject)

        whenever(networkChecker.connectivityStateObservable())
            .thenReturn(networkStateSubject)

        provider =
            SmilesCounterStateProviderImpl(
                smilesCounterVisibilityUseCase = smilesCounterVisibilityUseCase,
                userExpectsSmilesUseCase = userExpectsSmilesUseCase,
                smilesCounterStateMerger = smilesCounterStateMerger,
                debounceScheduler = debounceScheduler,
                smilesUseCase = smilesUseCase,
                networkChecker = networkChecker,
                smileCounterChangedUseCase = smileCounterChangedUseCase
            )
    }

    @Test
    fun `multiple observers share subscription`() {
        assertEquals(
            provider.smilesStateObservable,
            provider.smilesStateObservable
        )
    }

    @Test
    fun `subscribing sends parameters from each observable to merger only after each one emits at least one item`() {
        val expectedResult = Pending
        smilesCounterStateMerger.stateOutput = expectedResult

        val observer = provider.smilesStateObservable.test().assertEmpty()

        val expectedPoints = 543
        smilesProcessor.onNext(expectedPoints)

        observer.assertEmpty()

        val expectedUserExpects = true
        userExpectsSmilesSubject.onNext(expectedUserExpects)

        observer.assertEmpty()

        val expectedCounterVisibility = true
        smilesCounterVisibilitySubject.onNext(expectedCounterVisibility)

        observer.assertEmpty()

        val expectedNetworkState = true
        networkStateSubject.onNext(expectedNetworkState)

        observer.assertValue(expectedResult).assertNotComplete()
    }

    @Test
    fun `when smilesPoints are emitted too often, we only take into account the last emitted value`() {
        val observer = provider.smilesStateObservable.test().assertEmpty()

        val expectedPoints = 543

        userExpectsSmilesSubject.onNext(true)
        smilesCounterVisibilitySubject.onNext(true)
        networkStateSubject.onNext(false)
        smilesCounterStateMerger.stateOutput = Error

        val firstExpectedPoints = 56
        smilesProcessor.onNext(firstExpectedPoints)

        observer.assertValue(Error).assertNotComplete()

        var expectedParameters = MergerParameters(
            isCounterVisible = true,
            syncPending = true,
            hasConnectivity = false,
            nbOfPoints = firstExpectedPoints
        )

        assertEquals(expectedParameters, smilesCounterStateMerger.parametersReceived.single())

        advanceTimeForSmilesToBeEmitted(50)

        smilesProcessor.onNext(89)

        advanceTimeForSmilesToBeEmitted(100)

        smilesProcessor.onNext(117)

        advanceTimeForSmilesToBeEmitted(100)

        smilesProcessor.onNext(expectedPoints)

        advanceTimeForSmilesToBeEmitted()

        observer.assertValue(Error).assertNotComplete()

        expectedParameters = MergerParameters(
            isCounterVisible = true,
            syncPending = true,
            hasConnectivity = false,
            nbOfPoints = expectedPoints
        )

        assertEquals(2, smilesCounterStateMerger.parametersReceived.size)
        assertEquals(expectedParameters, smilesCounterStateMerger.parametersReceived.last())
    }

    @Test
    fun `observable emits a new item as soon as it receives a new item and as long as the emitted item is different than the previous one`() {
        val observer = provider.smilesStateObservable.test().assertEmpty()

        userExpectsSmilesSubject.onNext(true)
        smilesCounterVisibilitySubject.onNext(true)
        networkStateSubject.onNext(false)
        smilesCounterStateMerger.stateOutput = Error

        smilesProcessor.onNext(44)

        advanceTimeForSmilesToBeEmitted()

        observer.assertValueCount(1)

        val expectedState = Pending
        smilesCounterStateMerger.stateOutput = expectedState

        smilesCounterVisibilitySubject.onNext(false)

        observer.assertValues(Error, expectedState)
    }

    @Test
    fun `observable does not emit consecutive duplicate values`() {
        val observer = provider.smilesStateObservable.test().assertEmpty()

        userExpectsSmilesSubject.onNext(true)
        smilesCounterVisibilitySubject.onNext(true)
        networkStateSubject.onNext(true)
        smilesCounterStateMerger.stateOutput = Error

        smilesProcessor.onNext(44)

        advanceTimeForSmilesToBeEmitted()

        observer.assertValueCount(1)

        smilesCounterVisibilitySubject.onNext(false)

        observer.assertValueCount(1)
    }

    @Test
    fun `observable dispatch the emitted values to smileCounterChangedUseCase`() {
        provider.smilesStateObservable.test()

        userExpectsSmilesSubject.onNext(true)
        smilesCounterVisibilitySubject.onNext(true)
        networkStateSubject.onNext(true)
        smilesCounterStateMerger.stateOutput = Error
        smilesProcessor.onNext(44)

        advanceTimeForSmilesToBeEmitted()

        verify(smileCounterChangedUseCase).onSmileCounterChanged(Error)

        smilesCounterStateMerger.stateOutput = Pending
        smilesCounterVisibilitySubject.onNext(false)

        verify(smileCounterChangedUseCase).onSmileCounterChanged(Pending)
    }

    /*
    Utils
     */
    private fun advanceTimeForSmilesToBeEmitted(millisToAdvance: Long = 500) {
        debounceScheduler.advanceTimeBy(millisToAdvance, TimeUnit.MILLISECONDS)
    }
}

private class FakeSmilesCounterStateMerger : SmilesCounterStateMerger {
    lateinit var stateOutput: SmilesCounterState

    val parametersReceived = mutableListOf<MergerParameters>()

    override fun apply(
        isCounterVisible: Boolean,
        syncPending: Boolean,
        nbOfPoints: Int,
        hasConnectivity: Boolean
    ): SmilesCounterState {
        parametersReceived.add(
            MergerParameters(
                isCounterVisible = isCounterVisible,
                syncPending = syncPending,
                nbOfPoints = nbOfPoints,
                hasConnectivity = hasConnectivity
            )
        )

        return stateOutput
    }
}

private data class MergerParameters(
    val isCounterVisible: Boolean,
    val syncPending: Boolean,
    val hasConnectivity: Boolean,
    val nbOfPoints: Int
)
