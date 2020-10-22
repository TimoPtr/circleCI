package com.kolibree.android.synchronizator

import androidx.annotation.Keep
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper

/**
 * A strategy to resolve conflicts amongst local and remote versions of the same item
 */
@Keep
interface ConflictStrategy {
    /**
     * Given two Synchronizable items, one coming from a remote server and another one in our local system,
     * returns a [ConflictResolution] that can be used to determine the next action
     */
    fun resolve(
        localSynchronizable: SynchronizableItem?,
        remoteSynchronizable: SynchronizableItem
    ): ConflictResolution
}

internal fun ConflictStrategy.resolve(
    wrapper: SynchronizableItemWrapper,
    remoteSynchronizable: SynchronizableItem
): ConflictResolution = resolve(wrapper.synchronizableItem, remoteSynchronizable)

/**
 * Resolution of a conflict between the local and the remote instance of the same [SynchronizableItem]
 */
@Keep
data class ConflictResolution(
    /**
     * Local copy of the [SynchronizableItem] with the same kolibreeId as [remoteSynchronizable]
     *
     * It will be null if there's no [SynchronizableItem] in the datastore with the same kolibreeId
     */
    val localSynchronizable: SynchronizableItem?,
    val remoteSynchronizable: SynchronizableItem,

    /**
     * [SynchronizableItem] resolved from the conflict between [localSynchronizable] and [remoteSynchronizable]
     *
     * It can be equal to [localSynchronizable], [remoteSynchronizable] or a merge of the two. The
     * latter would happen on a three way merge, which we don't support yet.
     */
    private val resolvedSynchronizable: SynchronizableItem?
) {
    /**
     * At this point, there are two options
     * 1) [resolvedSynchronizable] is equal to [localSynchronizable], thus we don't want to replace
     * the local instance
     * 2) [resolvedSynchronizable] is different than [localSynchronizable], thus we want to replace
     * the local copy
     *
     * In this last case, we want to make sure we replace the local instance, never to have a new
     * insert. [withSanitizedLocalData] provides a mechanism to ensure necessary local data to
     * detect duplicates is kept
     *
     * @return [SynchronizableItem] to insert to local dataStore. null if we shouldn't insert
     */
    fun synchronizableForDatastore(): SynchronizableItem? {
        return if (resolvedSynchronizable == localSynchronizable) {
            null
        } else {
            resolvedSynchronizable.withSanitizedLocalData()
        }
    }

    /**
     * @return [SynchronizableItem] to perform an addEdit operation on the backend. null if we
     * shouldn't update the backend
     */
    fun synchronizableForBackend(): SynchronizableItem? {
        return if (resolvedSynchronizable == remoteSynchronizable) {
            null
        } else {
            resolvedSynchronizable
        }
    }

    /**
     * If both [this] and [localSynchronizable] are not null, return a copy of [this] with enough
     * local data for [SynchronizableItemDataStore] to detect if it's a duplicate of a local instance
     *
     * @return [SynchronizableItem]?
     * - null if [this] is null
     * - [this] with enough data to detect if it's a duplicate of a local instance
     */
    private fun SynchronizableItem?.withSanitizedLocalData(): SynchronizableItem? {
        return localSynchronizable?.let { nonNullLocalItem ->
            withLocalUuid()?.updateFromLocalInstance(nonNullLocalItem)
        } ?: this
    }

    /**
     * If both [this] and [localSynchronizable] are not null, return a copy of [this] with non-null
     * uuid
     *
     * @return [SynchronizableItem]?
     * - null if [this] is null.
     * - [SynchronizableItem] with null uuid if [localSynchronizable] is null or its uuid is null
     * - [SynchronizableItem] with [localSynchronizable]'s non-null uuid
     */
    private fun SynchronizableItem?.withLocalUuid(): SynchronizableItem? {
        if (this == null) return this

        val localUuid = localSynchronizable?.uuid

        return if (localUuid != null) {
            withUuid(localUuid)
        } else {
            this
        }
    }
}
