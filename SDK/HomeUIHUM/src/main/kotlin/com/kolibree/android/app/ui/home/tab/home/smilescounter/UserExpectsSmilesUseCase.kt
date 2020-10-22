/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.runOnMainThread
import com.kolibree.android.synchronization.SynchronizationState
import com.kolibree.android.synchronization.SynchronizationState.Success
import com.kolibree.android.synchronization.SynchronizationStateUseCase
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import timber.log.Timber

/**
 * UseCase to flag if user expects smiles to be updated
 *
 * We consider that a user expects smiles to be updated when
 * 1) He completed an activity at t=0 after which he expects smiles points to be awarded
 * 2) There hasn't been a Success sync with the backend after t=0
 *
 * Examples of activities after a user might expect smiles
 * - Successful brushing session
 * - Completed brushing streak
 * - Question of the day
 * - Login
 * - App launched
 */
@AppScope
@VisibleForApp
class UserExpectsSmilesUseCase @Inject constructor(
    synchronizationStateUseCase: SynchronizationStateUseCase,
    @SingleThreadScheduler private val delayScheduler: Scheduler
) {
    @VisibleForTesting
    val userExpectsSmilesRelay = BehaviorRelay.createDefault(TrustedClock.getNowInstant())

    /**
     * Observable of Success sync events
     */
    private val successSyncsStream =
        synchronizationStateUseCase.onceAndStream
            .doOnNext { Timber.tag("SmilesState").d("Sync event: $it") }
            .filter { it is Success }

    /**
     * Emits true when user expects smiles to be awarded, false when he doesn't.
     *
     * It can emit duplicate consecutive values
     *
     * Starts emission with a true.
     *
     * Event with true will be automatically changed to false after [EXPECT_SMILE_POINTS_TIMEOUT].
     * This prevents infinite points calculation.
     *
     * Each event will restart timeout.
     */
    val onceAndStream: Observable<Boolean> = Observable.combineLatest(
        userExpectsSmilesRelay.distinctUntilChanged(),
        /*
        We want the stream to emit a value even if we never received a real Success event

        Consider the scenario where the app is used without internet. There'll never be a Success
        but we still want to emit values
         */
        successSyncsStream,
        BiFunction<Instant, SynchronizationState, Boolean> { userInstant, syncSuccess ->
            userInstant.isAfter(syncSuccess.timestamp)
        }
    )
        .startWith(true)
        .withTimeout()

    /**
     * If smile points are expected schedules delayed action.
     * Otherwise does nothing.
     *
     * Delayed action is cancelled each time there is a new item.
     * If there are no items in given [EXPECT_SMILE_POINTS_TIMEOUT],
     * delayed action will emit false and print exception.
     */
    private fun Observable<Boolean>.withTimeout(): Observable<Boolean> {
        return switchMap { expectSmilePoints ->
            if (!expectSmilePoints) {
                // If smile  are not expected we can just pass the event down
                Observable.just(false)
            } else {
                // If points are expected the event is passed down immediately (inside startWith)
                // and then delayed action is scheduled.
                //
                // Each new item will cancel previous delayed action.
                // If event doesn't come before timeout, action will emit false and print exception.
                Observable
                    .fromCallable {
                        Timber.i(
                            "Waiting for points to be awarded took more than ${EXPECT_SMILE_POINTS_TIMEOUT.seconds}s!"
                        )
                        false
                    }
                    .delay(EXPECT_SMILE_POINTS_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS, delayScheduler)
                    .startWith(true)
            }
        }
    }

    /**
     * Flags that a user expects smiles to be awarded after [instant] (t0)
     *
     * [instant] should be part of the model created from user action
     * - Brushing session creation date
     * - Brushing Streak completion date
     * - Question of the day answer time
     *
     * It should NOT be the time at which user clicked "Collect my smiles"
     * If you do that, we might have an infinite Pending animation
     * - t0 = Send request to backend
     * - t1 = Sync completes successfully and points are awarded
     * - t2 = User exits screen and we invoke onUserExpectsPoints
     *
     * To determine if a sync is pending, we expect a successful sync after t2. And that won’t happen
     * We need to pass t0 as instant
     *
     * Internally, this operation posts a Runnable on main thread handler
     */
    fun onUserExpectsPoints(instant: Instant) {
        Runnable {
            userExpectsSmilesRelay.accept(instant)
        }.runOnMainThread()
    }
}

@SuppressWarnings("MagicNumber")
internal val EXPECT_SMILE_POINTS_TIMEOUT = Duration.ofSeconds(5)
