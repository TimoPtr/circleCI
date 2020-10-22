package com.kolibree.android.synchronizator.models

import androidx.annotation.Keep
import com.kolibree.android.synchronizator.DataStoreId
import java.util.UUID
import org.threeten.bp.ZonedDateTime

/**
 * Represents an item that can be synchronized and persisted
 */
@Keep
interface SynchronizableItem {
    val kolibreeId: DataStoreId?

    /**
     * Persistent [UUID] that, once set, should never change
     *
     * @return [UUID] that uniquely identifies the [SynchronizableItem]
     */
    val uuid: UUID?

    val createdAt: ZonedDateTime
    val updatedAt: ZonedDateTime

    /**
     * Don't touch [uuid] outside of Synchronizator module
     */
    fun withUuid(uuid: UUID): SynchronizableItem

    fun withKolibreeId(kolibreeId: DataStoreId): SynchronizableItem
    fun withUpdatedAt(updatedAt: ZonedDateTime): SynchronizableItem

    /**
     * Invoked on a remote [SynchronizableItem] that's about to replace a local instance
     *
     * This gives clients the chance to propagate any local data that Synchronization library is
     * unaware of
     *
     * A perfect example of this is assigning the local Primary Key so that the database knows to
     * replace rather than to insert
     *
    ```
    override fun updateFromLocalInstance(localItem: SynchronizableItem): SynchronizableItem {
    return copy(localId = (localItem as TestSynchronizableItem).localId)
    }
    ```
     *
     * [localItem] is guaranteed to have a non-null uuid
     */
    fun updateFromLocalInstance(localItem: SynchronizableItem): SynchronizableItem
}
