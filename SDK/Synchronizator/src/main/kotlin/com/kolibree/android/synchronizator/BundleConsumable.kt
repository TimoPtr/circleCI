package com.kolibree.android.synchronizator

import com.kolibree.android.synchronizator.network.Consumable

/**
 * Represents a combination of a Bundle + a Consumable from a SynchronizeAccountResponse.
 *
 * Instances of this class should be scoped to a synchronization operation and never be kept or used outside of this
 * scope.
 */
internal sealed class BundleConsumable

internal data class ItemBundleConsumable(
    val itemBundle: SynchronizableItemBundle,
    val itemConsumable: Consumable
) : BundleConsumable()

internal data class ReadOnlyBundleConsumable(
    val readOnlyBundle: SynchronizableReadOnlyBundle,
    val readOnlyConsumable: Consumable
) : BundleConsumable()

internal data class CatalogBundleConsumable(
    val catalogBundle: SynchronizableCatalogBundle,
    val catalogConsumable: Consumable
) : BundleConsumable()
