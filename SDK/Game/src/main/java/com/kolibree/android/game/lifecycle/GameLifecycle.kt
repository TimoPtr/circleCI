/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.lifecycle

import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly

/**
 * Status of a Game
 */
@Keep
enum class GameLifecycle {
    /**
     * Game has been moved to background
     *
     * If the game is brought to foreground, this event will be followed by [Foreground]
     */
    Background {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return when (newState) {
                Idle, Started, Paused, Resumed, Restarted, Finished, Background -> false
                Foreground, Terminated -> true
            }
        }
    },
    /**
     * Game has been moved from Background to Foreground
     *
     * This event will be followed by [Idle], [Paused] or [Restarted]
     */
    Foreground {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return when (newState) {
                Started, Resumed, Finished, Terminated, Background, Foreground -> false
                Idle, Paused, Restarted -> true
            }
        }
    },
    /**
     * Game has not started
     *
     * If the game is started, this event will be followed by [Started]
     * If the game exits, this event will be followed by [Terminated]
     * If the Activity goes to onStop, this event will be followed by [Background]
     */
    Idle {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return when (newState) {
                Idle, Paused, Resumed, Restarted, Finished, Foreground -> false
                Started, Background, Terminated -> true
            }
        }
    },

    /**
     * Game has started for the first time
     *
     * If the game is paused, this event will be followed by [Paused]
     * If the game timer completes, this event will be followed by [Finished]
     */
    Started {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return when (newState) {
                Idle, Started, Resumed, Restarted, Foreground, Background -> false
                Paused, Finished, Terminated -> true
            }
        }
    },

    /**
     * Game is paused
     *
     * If the game is resumed, this event will be followed by [Resumed]
     * If the game is restarted, this event will be followed by [Restarted]
     * If the user quits the game, this event will be followed by
     * - [Finished], if the brushing session lasted more than [MIN_BRUSHING_DURATION_SECONDS]
     * - [Terminated], if the brushing session lasted less than [MIN_BRUSHING_DURATION_SECONDS]
     * If the Activity goes to onStop, this event will be followed by [Background]
     */
    Paused {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return when (newState) {
                Idle, Started, Paused, Foreground -> false
                Resumed, Restarted, Finished, Background, Terminated -> true
            }
        }
    },

    /**
     * Game has resumed
     *
     * If the game is paused, this event will be followed by [Paused]
     * If the game timer completes, this event will be followed by [Finished]
     */
    Resumed {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return when (newState) {
                Idle, Started, Resumed, Restarted, Foreground, Background -> false
                Paused, Finished, Terminated -> true
            }
        }
    },

    /**
     * Game has been reset. Previous brushing session data should be ignored.
     *
     * If the user starts brushing again, this event will be followed by [Started]
     * If the game exits Brushing, this event will be followed by [Terminated]
     * If the Activity goes to onStop, this event will be followed by [Background]
     */
    Restarted {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return when (newState) {
                Idle, Paused, Resumed, Restarted, Finished, Foreground -> false
                Started, Background, Terminated -> true
            }
        }
    },

    /**
     * Game has finished successfully.
     *
     * This event will be immediately followed by [Terminated]
     */
    Finished {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return when (newState) {
                Idle, Started, Paused, Resumed, Restarted, Finished, Foreground, Background -> false
                Terminated -> true
            }
        }
    },

    /**
     * Game has terminated
     *
     * No further event will follow
     */
    Terminated {
        override fun innerValidateTransition(newState: GameLifecycle): Boolean {
            return false
        }
    };

    /**
     * Validate the we can transition from [oldState] to [newState] without breaking the GameLifecycle
     * state machine logic
     *
     * @throws AssertionError if transition is invalid
     */
    fun validateTransition(newState: GameLifecycle) {
        FailEarly.failInConditionMet(
            !innerValidateTransition(newState),
            "Can't transition from $this to $newState"
        )
    }

    /**
     * @return true if transition is valid, false otherwise
     */
    abstract fun innerValidateTransition(newState: GameLifecycle): Boolean
}
