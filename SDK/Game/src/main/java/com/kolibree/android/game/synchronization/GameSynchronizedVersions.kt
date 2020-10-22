/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kolibree.android.persistence.BasePreferencesImpl
import javax.inject.Inject

/**
 * Stores information about the games (progress, ...) model versions this app holds
 */
internal class GameSynchronizedVersions @Inject constructor(context: Context) : BasePreferencesImpl(context) {

    @VisibleForTesting
    companion object {
        const val KEY_GAME_PROGRESS = "game_progress"
    }

    fun gameProgressVersion() = prefs.getInt(KEY_GAME_PROGRESS, 0)

    fun setGameProgressVersion(newVersion: Int) = prefsEditor.putInt(KEY_GAME_PROGRESS, newVersion).apply()

    // There is no synchronization on the backend side for the short task so the version will never change
    fun shortTaskVersion() = 0
}
