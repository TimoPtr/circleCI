/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Idle
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Invisible
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.NoInternet
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Pending
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayIncrease
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayLanding
import io.reactivex.functions.Function4
import javax.inject.Inject
import timber.log.Timber

/**
 * Function to determine [SmilesCounterState] given all factors that we need to take into account.
 *
 * Rules to play smiles counter animation are detailed
 * [here](https://kolibree.atlassian.net/wiki/spaces/PROD/pages/310116355/Smile+points+counter#Use-cases).
 */
internal interface SmilesCounterStateMerger :
    Function4<Boolean, Boolean, Int, Boolean, SmilesCounterState>

internal class SmilesCounterStateMergerImpl @Inject constructor() : SmilesCounterStateMerger {
    private var lastPointsSeenByUser: Int = 0
    private var playLandingEmitted: Boolean = false
    private var lastEmittedState: SmilesCounterState? = null

    override fun apply(
        isCounterVisible: Boolean,
        syncPending: Boolean,
        nbOfPoints: Int,
        hasConnectivity: Boolean
    ): SmilesCounterState {
        logState(isCounterVisible, syncPending, nbOfPoints, hasConnectivity)

        return when {
            !hasConnectivity -> NoInternet
            !playLandingEmitted -> playLandingState(nbOfPoints)
            !isCounterVisible -> Invisible
            syncPending -> Pending
            else -> stateFromCounterVisible(nbOfPoints)
        }.also { emittedState ->
            lastEmittedState = emittedState
        }
    }

    private fun playLandingState(nbOfPoints: Int) = PlayLanding(points = nbOfPoints).also {
        lastPointsSeenByUser = nbOfPoints
        playLandingEmitted = true
    }

    private fun logState(
        isCounterVisible: Boolean,
        syncPending: Boolean,
        nbOfPoints: Int,
        hasConnectivity: Boolean
    ) {
        Timber.tag("SmilesState")
            .d(
                "isCounterVisible: %s, syncPending: %s, nbOfPoints: %s, hasConnectivity: %s",
                isCounterVisible,
                syncPending,
                nbOfPoints,
                hasConnectivity
            )
    }

    /**
     * @return
     * - PlayIncrease if nb of points has increased since user last saw the counter
     * - null if user never saw counter before or points haven't increased since he last saw the
     * counter
     */
    private fun stateFromCounterVisible(nbOfPoints: Int): SmilesCounterState {
        val smilesCounterState =
            if (shouldPlayAnimation(nbOfPoints)) {
                PlayIncrease(lastPointsSeenByUser, nbOfPoints)
            } else {
                Idle(nbOfPoints)
            }

        return smilesCounterState.also { lastPointsSeenByUser = nbOfPoints }
    }

    /**
     * @return `true` if the number of points is greater than the last points seen or
     * if the pending state should play the animation. `false` otherwise
     */
    private fun shouldPlayAnimation(nbOfPoints: Int): Boolean {
        return nbOfPoints > lastPointsSeenByUser
    }
}
