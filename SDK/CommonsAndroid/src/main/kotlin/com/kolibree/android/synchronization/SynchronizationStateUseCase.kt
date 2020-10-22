/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronization

import androidx.annotation.Keep
import com.kolibree.android.clock.TrustedClock
import io.reactivex.Observable
import org.threeten.bp.Instant

/**
 * UseCase to notify [SynchronizationState]
 */
@Keep
interface SynchronizationStateUseCase {
    /**
     * State machine that represents Synchronization events history, emitting events and the
     * [Instant] at which they took place
     *
     * On subscription, it replays at most [MAX_ITEMS_REPLAYED]
     *
     * This observable does not work on any Scheduler by default
     *
     * The first [SynchronizationState] emitted will always be [SynchronizationState.None]
     *
     * See [SynchronizationState] details for State Machine behaviour
     *
     * @return [Observable]<[SynchronizationState]> that will emit current [SynchronizationState]
     * followed by any changes in the synchronization state.
     */
    val onceAndStream: Observable<SynchronizationState>
}

@Keep
const val MAX_ITEMS_REPLAYED = 6

/**
 * Each state represents a Sync state at an Instant.
 */
@Keep
sealed class SynchronizationState(val timestamp: Instant = TrustedClock.getNowInstant()) {
    /**
     * The app has never synced
     *
     * It's always the first SynchronizationState and can only be present once per application
     * session
     *
     * It can be followed by [Ongoing]
     */
    object None : SynchronizationState()

    /**
     * Last sync succeeded, and there's no ongoing sync
     *
     * It can either be a terminal state or be followed by [Ongoing]
     */
    class Success : SynchronizationState() {
        override fun toString(): String = "Success[timestamp=$timestamp]"
    }

    /**
     * Last sync failed, and there's no ongoing sync
     *
     * It can either be a terminal state or be followed by [Ongoing]
     */
    class Failure : SynchronizationState() {
        override fun toString(): String = "Failure[timestamp=$timestamp]"
    }

    /**
     * Sync is ongoing
     *
     * It will be followed by [Success] or [Failure]
     */
    class Ongoing : SynchronizationState() {
        override fun toString(): String = "Ongoing[timestamp=$timestamp]"
    }
}
