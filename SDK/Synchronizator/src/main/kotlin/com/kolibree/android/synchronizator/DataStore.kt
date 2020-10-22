package com.kolibree.android.synchronizator

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import java.util.UUID

typealias DataStoreId = Long

/**
 * Datastore operations needed by a SynchronizableItemBundle
 */
@VisibleForApp
interface SynchronizableItemDataStore {
    /**
     * Inserts [synchronizable] to local storage and returns a [SynchronizableItem]
     *
     * Descendants are expected to implement their duplicate detection mechanisms. For
     * example, keeping a local Primary Key that this library can never modify
     */
    fun insert(synchronizable: SynchronizableItem): SynchronizableItem

    fun getByKolibreeId(kolibreeId: DataStoreId): SynchronizableItem?
    fun getByUuid(uuid: UUID): SynchronizableItem

    fun delete(uuid: UUID)
    fun updateVersion(newVersion: Int)

    /**
     * @return true if this SynchronizableItemDataStore can handle [synchronizable], false otherwise
     */
    fun canHandle(synchronizable: SynchronizableItem): Boolean
}

/**
 * Datastore operations needed by a SynchronizableReadOnlyBundle
 */
@VisibleForApp
interface SynchronizableReadOnlyDataStore {
    /**
     * Deletes all previous data from the datastore and inserts the new SynchronizableReadOnly
     */
    fun replace(synchronizable: SynchronizableReadOnly)
    fun updateVersion(newVersion: Int)
}

/**
 * Datastore operations needed by a SynchronizableCatalogBundle
 */
@VisibleForApp
interface SynchronizableCatalogDataStore {
    /**
     * Deletes all previous data from the datastore and inserts the new SynchronizableCatalog
     */
    fun replace(catalog: SynchronizableCatalog)
    fun updateVersion(newVersion: Int)
}

internal fun SynchronizableItemDataStore.insert(
    wrapper: SynchronizableItemWrapper
): SynchronizableItem {
    return insert(wrapper.synchronizableItem)
}
