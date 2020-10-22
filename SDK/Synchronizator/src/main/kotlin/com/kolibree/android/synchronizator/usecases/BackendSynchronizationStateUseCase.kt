/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.usecases

import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.synchronization.MAX_ITEMS_REPLAYED
import com.kolibree.android.synchronization.SynchronizationState
import com.kolibree.android.synchronization.SynchronizationState.Failure
import com.kolibree.android.synchronization.SynchronizationState.None
import com.kolibree.android.synchronization.SynchronizationState.Ongoing
import com.kolibree.android.synchronization.SynchronizationState.Success
import com.kolibree.android.synchronization.SynchronizationStateUseCase
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Emits state of ongoing synchronizations with backend
 */
@AppScope
internal class BackendSynchronizationStateUseCase
@Inject constructor() : SynchronizationStateUseCase {
    private val stateRelay = BehaviorRelay.createDefault<SynchronizationState>(None)

    override val onceAndStream: Observable<SynchronizationState> = stateRelay
        .scan { previous: SynchronizationState, current: SynchronizationState ->
            validateSynchronizationStateMachine(
                previous = previous,
                current = current
            )

            current
        }
        .replay(MAX_ITEMS_REPLAYED)
        .autoConnect()

    private fun validateSynchronizationStateMachine(
        previous: SynchronizationState,
        current: SynchronizationState
    ) {
        when (previous) {
            None, is Success, is Failure -> FailEarly.failInConditionMet(
                current !is Ongoing,
                "Invalid state after $previous. Expected Ongoing, was $current"
            )
            is Ongoing -> FailEarly.failInConditionMet(
                current !is Success && current !is Failure,
                "Invalid state after $previous. Expected Success or Failure, was $current"
            )
        }
    }

    fun onSyncStarted() = stateRelay.accept(Ongoing())

    fun onSyncSuccess() = stateRelay.accept(Success())

    fun onSyncFailed() = stateRelay.accept(Failure())
}
