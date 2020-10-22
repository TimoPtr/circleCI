/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import androidx.annotation.IntDef
import androidx.annotation.Keep

/**
 * Created by miguelaragues on 16/10/17.
 */
@Keep
interface GameViewState<T : GameViewState<T>> {

    val actionId: Int

    fun withActionId(@ActionId actionId: Int): T

    @kotlin.annotation.Retention
    @IntDef(ACTION_NONE,
        ACTION_ON_DATA_SAVED,
        ACTION_ERROR_SOMETHING_WENT_WRONG,
        ACTION_PLAY_TRANSITION_SOUNDS,
        ACTION_CANCEL,
        ACTION_LOST_CONNECTION)
    annotation class ActionId

    companion object {

        const val ACTION_NONE = 0
        const val ACTION_ON_DATA_SAVED = 2
        const val ACTION_ERROR_SOMETHING_WENT_WRONG = 3
        const val ACTION_CANCEL = 4
        const val ACTION_OPEN_SETTINGS = 5
        const val ACTION_PLAY_TRANSITION_SOUNDS = 6
        const val ACTION_LOST_CONNECTION = 7
    }
}
