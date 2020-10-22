/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.models.SynchronizableItem
import timber.log.Timber

@Keep
object SynchronizationBundles {
    private val privateBundles: MutableSet<Bundle> = mutableSetOf()

    internal val bundles: Set<Bundle>
        get() = privateBundles.toSet()

    fun register(bundle: Bundle) {
        if (privateBundles.none {
                it.synchronizeAccountKey().key == bundle.synchronizeAccountKey().key
            }) {
            privateBundles.add(bundle)
        } else {
            Timber.w(
                "Not adding bundle with key %s",
                bundle.synchronizeAccountKey().key
            )
        }
    }

    @VisibleForTesting
    internal fun clear() {
        privateBundles.clear()
    }

    /**
     * @return the single SynchronizableItemBundle
     */
    internal fun bundleForSynchronizableItem(synchronizable: SynchronizableItem): SynchronizableItemBundle? {
        val bundleForSynchronizable = bundles
            .filterIsInstance(SynchronizableItemBundle::class.java)
            .singleOrNull { it.canHandle(synchronizable) }

        FailEarly.failInConditionMet(
            bundleForSynchronizable == null,
            "No bundle registered for $synchronizable ($bundles)"
        )

        return bundleForSynchronizable
    }

    /**
     * @return the single SynchronizableItemBundle
     */
    internal fun bundleForTrackingEntity(synchronizableTrackingEntity: SynchronizableTrackingEntity):
        SynchronizableItemBundle? {
        val bundleForSynchronizable = bundles
            .filterIsInstance(SynchronizableItemBundle::class.java)
            .singleOrNull { it.synchronizeAccountKey().key == synchronizableTrackingEntity.bundleKey }

        FailEarly.failInConditionMet(
            bundleForSynchronizable == null,
            "No bundle registered for $synchronizableTrackingEntity ($bundles)"
        )

        return bundleForSynchronizable
    }
}
