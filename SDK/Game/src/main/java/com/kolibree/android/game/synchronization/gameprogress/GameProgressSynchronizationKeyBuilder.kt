/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress

import com.kolibree.android.game.synchronization.GameSynchronizedVersions
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class GameProgressSynchronizationKeyBuilder @Inject constructor(
    private val gamesSynchronizedVersions: GameSynchronizedVersions
) : SynchronizeAccountKeyBuilder(SynchronizableKey.GAME_PROGRESS) {
    override fun version(): Int = gamesSynchronizedVersions.gameProgressVersion()
}
