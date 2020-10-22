/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.synchronization.SynchronizationState
import com.kolibree.android.synchronization.SynchronizationState.Failure
import com.kolibree.android.synchronization.SynchronizationState.None
import com.kolibree.android.synchronization.SynchronizationState.Ongoing
import com.kolibree.android.synchronization.SynchronizationState.Success
import com.kolibree.android.synchronization.SynchronizationStateUseCase
import com.kolibree.android.test.extensions.withFixedInstant
import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.Instant

class UserExpectsSmilesUseCaseTest : BaseUnitTest() {
    private val ongoingSynchronizationUseCase = FakeSynchronizationStateUseCase()
    private lateinit var useCase: UserExpectsSmilesUseCase

    private lateinit var initialUserInstant: Instant

    private lateinit var testScheduler: TestScheduler

    override fun setup() {
        super.setup()

        testScheduler = TestScheduler()

        withFixedInstant {
            useCase = UserExpectsSmilesUseCase(ongoingSynchronizationUseCase, testScheduler)
            initialUserInstant = TrustedClock.getNowInstant()
        }
    }

    @Test
    fun `useCase emits true as initial item`() {
        useCase.onceAndStream.test().assertValues(true)
    }

    @Test
    fun `useCase subscribes to ongoingSynchronizationUseCase`() {
        useCase.onceAndStream.test()

        assertTrue(ongoingSynchronizationUseCase.relay.hasObservers())
    }

    @Test
    fun `useCase doesn't emit a value for None, Failure or Ongoing`() {
        val observer = useCase.onceAndStream.test().assertValues(true)

        listOf(None, Failure(), Ongoing()).forEach { syncState ->
            ongoingSynchronizationUseCase.relay.accept(syncState)
        }

        observer.assertValues(true)
    }

    @Test
    fun `useCase emits true for Success when timestamp is before userInstant`() {
        val observer = useCase.onceAndStream.test().assertValues(true)

        withFixedInstant(
            fixedInstant = initialUserInstant.minusMillis(1)
        ) {
            ongoingSynchronizationUseCase.relay.accept(Success())
        }

        observer.assertValues(true, true)
    }

    @Test
    fun `useCase emits false for Success when timestamp is after userInstant`() {
        val observer = useCase.onceAndStream.test().assertValues(true)

        withFixedInstant(
            fixedInstant = initialUserInstant.plusMillis(1)
        ) {
            ongoingSynchronizationUseCase.relay.accept(Success())
        }

        observer.assertValues(true, false)
    }

    @Test
    fun `when user expects a sync and there's no Success, useCase doesn't emit true`() {
        val observer = useCase.onceAndStream.test().assertValues(true)

        withFixedInstant(
            fixedInstant = initialUserInstant.plusSeconds(1)
        ) {
            emitInstant()
        }

        observer.assertValues(true)
    }

    @Test
    fun `useCase emits true when user expects a sync at t=5 and last Success sync is at t=1`() {
        withFixedInstant(
            fixedInstant = initialUserInstant.plusSeconds(1)
        ) {
            ongoingSynchronizationUseCase.relay.accept(Success())
        }

        val observer = useCase.onceAndStream.test().assertValues(true, false)

        withFixedInstant(
            fixedInstant = initialUserInstant.plusSeconds(5)
        ) {
            emitInstant()
        }

        observer.assertValues(true, false, true)
    }

    @Test
    fun `useCase emits false when user expects a sync at t=1 and last Success sync is at t=5`() {
        withFixedInstant(
            fixedInstant = initialUserInstant.plusSeconds(5)
        ) {
            ongoingSynchronizationUseCase.relay.accept(Success())
        }

        val observer = useCase.onceAndStream.test().assertValues(true, false)

        withFixedInstant(
            fixedInstant = initialUserInstant.plusSeconds(1)
        ) {
            emitInstant()
        }

        observer.assertValues(true, false, false)
    }

    @Test
    fun `useCase emits false after given timeout when true was previously emitted`() {
        val observer = useCase.onceAndStream.test().assertValues(true)

        withFixedInstant(
            fixedInstant = initialUserInstant.minusMillis(1)
        ) {
            ongoingSynchronizationUseCase.relay.accept(Success())
        }

        observer.assertValues(true, true)

        testScheduler.advanceTimeBy(EXPECT_SMILE_POINTS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)

        observer.assertValues(true, true, false)
    }

    @Test
    fun `useCase doesn't emit false after given timeout when false was previously emitted`() {
        val observer = useCase.onceAndStream.test().assertValues(true)

        withFixedInstant(
            fixedInstant = initialUserInstant.plusMillis(1)
        ) {
            ongoingSynchronizationUseCase.relay.accept(Success())
        }

        observer.assertValues(true, false)

        testScheduler.advanceTimeBy(EXPECT_SMILE_POINTS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)

        observer.assertValues(true, false)
    }

    private fun emitInstant() {
        useCase.userExpectsSmilesRelay.accept(TrustedClock.getNowInstant())
    }
}

private class FakeSynchronizationStateUseCase : SynchronizationStateUseCase {
    val relay = BehaviorRelay.create<SynchronizationState>()

    override val onceAndStream: Observable<SynchronizationState> = relay
}
