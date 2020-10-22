package com.kolibree.android.synchronizator

import androidx.annotation.VisibleForTesting
import com.kolibree.android.synchronizator.network.SynchronizeAccountResponse
import javax.inject.Inject

/**
 * Implementation of Visitor pattern
 */
internal interface BundleConsumableVisitor {
    fun visit(
        syncAccountResponse: SynchronizeAccountResponse,
        bundle: SynchronizableItemBundle
    ): BundleConsumable?

    fun visit(
        syncAccountResponse: SynchronizeAccountResponse,
        bundle: SynchronizableReadOnlyBundle
    ): BundleConsumable?

    fun visit(
        syncAccountResponse: SynchronizeAccountResponse,
        bundle: SynchronizableCatalogBundle
    ): BundleConsumable?
}

internal class BundleConsumableVisitorImpl @Inject constructor() : BundleConsumableVisitor {
    override fun visit(
        syncAccountResponse: SynchronizeAccountResponse,
        bundle: SynchronizableItemBundle
    ): BundleConsumable? {
        return consumableForBundle(syncAccountResponse, bundle)?.let { consumable ->
            bundle.dataStore.updateVersion(consumable.version)

            ItemBundleConsumable(
                bundle,
                consumable
            )
        }
    }

    override fun visit(
        syncAccountResponse: SynchronizeAccountResponse,
        bundle: SynchronizableReadOnlyBundle
    ): BundleConsumable? {
        return consumableForBundle(syncAccountResponse, bundle)?.let { consumable ->
            bundle.dataStore.updateVersion(consumable.version)

            ReadOnlyBundleConsumable(
                bundle,
                consumable
            )
        }
    }

    override fun visit(
        syncAccountResponse: SynchronizeAccountResponse,
        bundle: SynchronizableCatalogBundle
    ): BundleConsumable? {
        return consumableForBundle(syncAccountResponse, bundle)?.let { consumable ->
            bundle.dataStore.updateVersion(consumable.version)

            CatalogBundleConsumable(
                bundle,
                consumable
            )
        }
    }

    @VisibleForTesting
    fun consumableForBundle(syncAccountResponse: SynchronizeAccountResponse, bundle: Bundle) =
        syncAccountResponse.typeConsumables[bundle.synchronizeAccountKeyBuilder.key]
}
