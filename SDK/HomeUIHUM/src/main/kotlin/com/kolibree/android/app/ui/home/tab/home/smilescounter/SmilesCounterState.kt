/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import android.os.Parcelable
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.Parcelize

/**
 * Represents the State of the Smiles Counter animation
 *
 * Multiple factors affect which animation should be played
 * - User just opened the app
 * - User clicked on "Collect my smiles"
 * - Smiles counter is currently visible to the user
 * - There's an ongoing synchronization AND we expect points to be increased
 * - There's no internet AND we expect points to be increased
 */
@VisibleForApp
sealed class SmilesCounterState : Parcelable {

    /**
     * Smiles counter is not visible. No animation to play.
     *
     * It will be followed by
     * [Invisible] if user is awarded more points but view remains not visible
     * [PlayIncrease] if view is visible and user is awarded more points
     * [Pending] if view is visible and user expects new points to be awarded
     * [Idle] if view is visible but smiles points hasn't changed
     */
    @VisibleForApp
    @Parcelize
    object Invisible : SmilesCounterState()

    /**
     * An increase animation is pending, but the conditions aren't met yet
     *
     * At some point, it might be followed by a [PlayLanding], [PlayIncrease], [Idle] or [Error]
     */
    @VisibleForApp
    @Parcelize
    object Pending : SmilesCounterState()

    /**
     * Play increase points animation up to [finalPoints]
     */
    @VisibleForApp
    @Parcelize
    data class PlayIncrease(val initialPoints: Int, val finalPoints: Int) : SmilesCounterState()

    /**
     * Play Landing animation. Should only happen once per application session
     */
    @VisibleForApp
    @Parcelize
    data class PlayLanding(val points: Int) : SmilesCounterState()

    /**
     * Smiles counter view is visible, but there's no animation to play,
     */
    @VisibleForApp
    @Parcelize
    data class Idle(val points: Int) : SmilesCounterState()

    /**
     * There's no Connectivity
     */
    @VisibleForApp
    @Parcelize
    object NoInternet : SmilesCounterState()

    /**
     * Error state
     */
    @VisibleForApp
    @Parcelize
    object Error : SmilesCounterState()
}
