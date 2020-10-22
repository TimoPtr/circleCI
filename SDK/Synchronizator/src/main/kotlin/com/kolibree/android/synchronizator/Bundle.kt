package com.kolibree.android.synchronizator

import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import com.kolibree.android.synchronizator.network.SynchronizeAccountResponse

/**
 * A Bundle holds the information needed to
 * 1. Build a graph of dependencies amongst classes so that a Collection<Bundle> can create an Acyclic Graph
 * 2. Provide a SynchronizeAccountKey to dynamically build a SynchronizeAccountRequest
 *
 * Equals operation relies on SynchronizeAccountKeyBuilder.key
 *
 * Instances should be stateless
 */
@VisibleForApp
sealed class Bundle {
    abstract val synchronizeAccountKeyBuilder: SynchronizeAccountKeyBuilder

    /**
     * Dynamically builds a SynchronizeAccountKey for the Bundle at the current instant
     *
     * Subsequent calls can return different instances
     */
    internal fun synchronizeAccountKey(): SynchronizeAccountKey =
        synchronizeAccountKeyBuilder.build()

    internal abstract fun accept(
        visitor: BundleConsumableVisitor,
        syncAccountResponse: SynchronizeAccountResponse
    ): BundleConsumable?

    internal fun key(): SynchronizableKey = synchronizeAccountKeyBuilder.key

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Bundle) return false

        if (synchronizeAccountKeyBuilder != other.synchronizeAccountKeyBuilder) return false

        return true
    }

    override fun hashCode(): Int {
        return synchronizeAccountKeyBuilder.hashCode()
    }
}

/**
 * Bundle to be used by items that can be created/updated/deleted by both the backend and the client(s).
 *
 * Since there might be data inconsistencies, it needs to provide a way to resolve conflicts
 *
 * For example, Brushings or Profile
 */
@Keep
data class SynchronizableItemBundle
constructor(
    val api: SynchronizableItemApi,
    val dataStore: SynchronizableItemDataStore,
    val conflictStrategy: ConflictStrategy,
    override val synchronizeAccountKeyBuilder: SynchronizeAccountKeyBuilder
) : Bundle() {
    override fun accept(
        visitor: BundleConsumableVisitor,
        syncAccountResponse: SynchronizeAccountResponse
    ) = visitor.visit(syncAccountResponse, this)

    /**
     * Helper to identify if a Bundle can handle a given instance of [SynchronizableItem]
     *
     * @return true if this Bundle can handle [synchronizable], false otherwise
     */
    fun canHandle(synchronizable: SynchronizableItem): Boolean = dataStore.canHandle(synchronizable)
}

/**
 * Bundle to be used by items that can only be modified by the backend and are not specific to any profile
 *
 * For example, Challenges catalog for rewards
 */
@VisibleForApp
data class SynchronizableCatalogBundle
constructor(
    val api: SynchronizableCatalogApi,
    val dataStore: SynchronizableCatalogDataStore,
    override val synchronizeAccountKeyBuilder: SynchronizeAccountKeyBuilder
) : Bundle() {
    override fun accept(
        visitor: BundleConsumableVisitor,
        syncAccountResponse: SynchronizeAccountResponse
    ) = visitor.visit(syncAccountResponse, this)
}

/**
 * Bundle to be used by items that can only be modified by the backend and are specific to a profile
 *
 * For example, ChallengeProgress, where profiles have a different state on each Challenge
 */
@VisibleForApp
data class SynchronizableReadOnlyBundle
constructor(
    val api: SynchronizableReadOnlyApi,
    val dataStore: SynchronizableReadOnlyDataStore,
    override val synchronizeAccountKeyBuilder: SynchronizeAccountKeyBuilder
) : Bundle() {
    override fun accept(
        visitor: BundleConsumableVisitor,
        syncAccountResponse: SynchronizeAccountResponse
    ) = visitor.visit(syncAccountResponse, this)
}
