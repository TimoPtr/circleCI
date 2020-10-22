package com.kolibree.android.game.synchronization

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.game.synchronization.gameprogress.GameProgressSynchronizableCreator
import com.kolibree.android.game.synchronization.shorttask.ShortTaskSynchronizableCreator
import com.kolibree.android.synchronizator.SynchronizationBundles
import javax.inject.Inject

@VisibleForApp
class GameSynchronizationRegistrar @Inject constructor() {

    @Inject
    internal lateinit var gameProgressSynchronizableCreator: GameProgressSynchronizableCreator

    @Inject
    internal lateinit var shortTaskSynchronizableCreator: ShortTaskSynchronizableCreator

    fun register() {
        SynchronizationBundles.register(gameProgressSynchronizableCreator.create())
        SynchronizationBundles.register(shortTaskSynchronizableCreator.create())
    }
}
