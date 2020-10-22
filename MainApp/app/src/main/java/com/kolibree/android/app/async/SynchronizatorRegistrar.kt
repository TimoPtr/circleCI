package com.kolibree.android.app.async

import com.kolibree.android.game.synchronization.GameSynchronizationRegistrar
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.models.BundleCreatorSet
import javax.inject.Inject

internal class SynchronizatorRegistrar
@Inject constructor(
    private val gameSynchronizationRegistrar: GameSynchronizationRegistrar,
    private val bundleCreators: BundleCreatorSet
) {
    fun registerBundles() {
        gameSynchronizationRegistrar.register()

        bundleCreators.forEach { SynchronizationBundles.register(it.create()) }
    }
}
