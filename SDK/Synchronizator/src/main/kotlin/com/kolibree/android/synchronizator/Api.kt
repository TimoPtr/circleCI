package com.kolibree.android.synchronizator

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly

/**
 * Remote API operations needed by a SynchronizableItemBundle
 */
@VisibleForApp
interface SynchronizableItemApi {
    /**
     * @return [SynchronizableItem] with id [kolibreeId] from the backend
     */
    fun get(kolibreeId: Long): SynchronizableItem

    /**
     * PUT or POST request to create or edit [synchronizable] in the backend
     *
     * @return [SynchronizableItem] on success
     */
    fun createOrEdit(synchronizable: SynchronizableItem): SynchronizableItem
}

/**
 * Remote API operations needed by a SynchronizableReadOnlyBundle
 */
@VisibleForApp
interface SynchronizableReadOnlyApi {
    fun get(id: Long): SynchronizableReadOnly
}

/**
 * Remote API operations needed by a SynchronizableCatalogBundle
 */
@VisibleForApp
interface SynchronizableCatalogApi {
    fun get(): SynchronizableCatalog
}

internal fun SynchronizableItemApi.createOrEdit(wrapper: SynchronizableItemWrapper) =
    createOrEdit(wrapper.synchronizableItem)
