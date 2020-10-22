/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home

import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.home.tab.home.SmilesCounterAnimation.NONE
import com.kolibree.android.app.ui.home.tab.home.SmilesCounterAnimation.NO_INTERNET
import com.kolibree.android.app.ui.home.tab.home.SmilesCounterAnimation.PENDING
import com.kolibree.android.app.ui.home.tab.home.SmilesCounterAnimation.RESTART
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Error
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Idle
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Invisible
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.NoInternet
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.Pending
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayIncrease
import com.kolibree.android.app.ui.home.tab.home.smilescounter.SmilesCounterState.PlayLanding
import com.kolibree.android.app.ui.home.tab.home.smilescounter.smilesBackgroundIncreaseAnimation
import com.kolibree.android.app.ui.home.tab.home.smilescounter.smilesBackgroundLaunchAnimation
import com.kolibree.databinding.bindingadapter.LottieDelayedLoop
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class HomeViewState(
    val currentPoints: Int = 0,
    val oldPoints: Int = 0,
    val pointsLoaded: Boolean = false,
    val pulsingDotVisible: Boolean = false,
    val smilesCounterState: SmilesCounterState,
    val smilesBackgroundAnimation: LottieDelayedLoop? = null,
    val smilesCounterAnimation: SmilesCounterAnimation = NONE
) : BaseViewState {
    @IgnoredOnParcel
    val restartAnimation: Boolean = smilesCounterAnimation == RESTART

    @IgnoredOnParcel
    val pendingAnimation: Boolean = smilesCounterAnimation == PENDING

    @IgnoredOnParcel
    val noInternetAnimation: Boolean = smilesCounterAnimation == NO_INTERNET

    fun withSmilesCounterState(newState: SmilesCounterState): HomeViewState =
        when (newState) {
            Error, is Invisible -> {
                copy(
                    smilesCounterState = newState,
                    smilesCounterAnimation = NONE
                )
            }
            is PlayLanding -> {
                copy(
                    smilesCounterState = newState,
                    currentPoints = newState.points,
                    oldPoints = newState.points,
                    pointsLoaded = true,
                    smilesBackgroundAnimation = smilesBackgroundLaunchAnimation,
                    smilesCounterAnimation = NONE
                )
            }
            Pending -> {
                copy(
                    smilesCounterState = newState,
                    smilesCounterAnimation = PENDING
                )
            }
            is PlayIncrease -> {
                copy(
                    smilesCounterState = newState,
                    currentPoints = newState.finalPoints,
                    oldPoints = newState.initialPoints,
                    smilesBackgroundAnimation = smilesBackgroundIncreaseAnimation,
                    smilesCounterAnimation = RESTART
                )
            }
            is Idle -> {
                copy(
                    smilesCounterState = newState,
                    currentPoints = newState.points,
                    smilesCounterAnimation = NONE
                )
            }
            NoInternet -> {
                copy(
                    smilesBackgroundAnimation = smilesBackgroundAnimation ?: smilesBackgroundLaunchAnimation,
                    smilesCounterState = newState,
                    smilesCounterAnimation = NO_INTERNET
                )
            }
        }

    companion object {
        fun initial(
            currentPoints: Int = 0,
            pointsLoaded: Boolean = currentPoints != 0
        ) = HomeViewState(
            currentPoints = currentPoints,
            pointsLoaded = pointsLoaded,
            smilesCounterState = Idle(currentPoints)
        )
    }
}

internal enum class SmilesCounterAnimation {
    NONE,
    RESTART,
    PENDING,
    NO_INTERNET
}
