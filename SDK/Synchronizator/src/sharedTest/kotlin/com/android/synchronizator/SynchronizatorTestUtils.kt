package com.android.synchronizator

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.synchronizator.CatalogBundleConsumable
import com.kolibree.android.synchronizator.ConflictResolution
import com.kolibree.android.synchronizator.ConflictStrategy
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.ItemBundleConsumable
import com.kolibree.android.synchronizator.ReadOnlyBundleConsumable
import com.kolibree.android.synchronizator.SynchronizableCatalogApi
import com.kolibree.android.synchronizator.SynchronizableCatalogBundle
import com.kolibree.android.synchronizator.SynchronizableCatalogDataStore
import com.kolibree.android.synchronizator.SynchronizableItemApi
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import com.kolibree.android.synchronizator.models.UploadStatus
import com.kolibree.android.synchronizator.network.Consumable
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import org.threeten.bp.ZonedDateTime

fun synchronizableCatalogBundle(
    api: SynchronizableCatalogApi = mock(),
    dataStore: SynchronizableCatalogDataStore = mock(),
    synchronizeAccountKeyBuilder: SynchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder()
) = SynchronizableCatalogBundle(
    api = api,
    dataStore = dataStore,
    synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder
)

fun synchronizableReadOnlyBundle(
    api: SynchronizableReadOnlyApi = mock(),
    dataStore: SynchronizableReadOnlyDataStore = mock(),
    synchronizeAccountKeyBuilder: SynchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder()
) = SynchronizableReadOnlyBundle(
    api = api,
    dataStore = dataStore,
    synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder
)

fun synchronizableItemBundle(
    api: SynchronizableItemApi = mock(),
    dataStore: SynchronizableItemDataStore = mock(),
    conflictStrategy: ConflictStrategy = mock(),
    synchronizeAccountKeyBuilder: SynchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder()
) = SynchronizableItemBundle(
    api = api,
    dataStore = dataStore,
    conflictStrategy = conflictStrategy,
    synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder
)

internal fun testItemDatastore(
    items: List<TestSynchronizableItem> = listOf(),
    canHandle: Boolean = false
): TestSynchronizableItemDataStore = TestSynchronizableItemDataStore(items, canHandle)

fun mockedItemDatastore(
    canHandle: Boolean = false
): SynchronizableItemDataStore = mock<SynchronizableItemDataStore>().apply {
    whenever(canHandle(any())).thenReturn(canHandle)
}

fun synchronizableItem(
    testLocalId: DataStoreId? = DEFAULT_ID,
    kolibreeId: DataStoreId? = null,
    createdAt: ZonedDateTime = TrustedClock.getNowZonedDateTimeUTC(),
    updatedAt: ZonedDateTime = createdAt,
    uuid: UUID? = null
): SynchronizableItem {
    return TestSynchronizableItem(
        localId = testLocalId,
        kolibreeId = kolibreeId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        uuid = uuid
    )
}

internal fun localSynchronizableItem(
    testLocalId: DataStoreId = DEFAULT_ID,
    kolibreeId: DataStoreId? = null,
    createdAt: ZonedDateTime = TrustedClock.getNowZonedDateTimeUTC(),
    updatedAt: ZonedDateTime = createdAt,
    uuid: UUID? = null
): TestSynchronizableItem = synchronizableItem(
    testLocalId = testLocalId,
    kolibreeId = kolibreeId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    uuid = uuid
) as TestSynchronizableItem

internal fun remoteSynchronizableItem(
    testLocalId: DataStoreId? = null,
    kolibreeId: DataStoreId = DEFAULT_REMOTE_ID,
    createdAt: ZonedDateTime = TrustedClock.getNowZonedDateTimeUTC(),
    updatedAt: ZonedDateTime = createdAt,
    uuid: UUID? = null
): TestSynchronizableItem = synchronizableItem(
    testLocalId = testLocalId,
    kolibreeId = kolibreeId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    uuid = uuid
) as TestSynchronizableItem

internal fun itemBundleConsumable(
    synchronizableItemBundle: SynchronizableItemBundle = synchronizableItemBundle(),
    itemConsumable: Consumable = itemConsumable()
) = ItemBundleConsumable(synchronizableItemBundle, itemConsumable)

internal fun itemConsumable(
    version: Int = DEFAULT_RETURN_VERSION,
    updatedIds: List<Long> = listOf(),
    deletedIds: List<Long> = listOf()
) = Consumable(version, updatedIds, deletedIds)

internal fun readOnlyBundleConsumable(
    synchronizableReadOnlyBundle: SynchronizableReadOnlyBundle = synchronizableReadOnlyBundle(),
    readOnlyConsumable: Consumable = readOnlyConsumable()
) = ReadOnlyBundleConsumable(synchronizableReadOnlyBundle, readOnlyConsumable)

internal fun readOnlyConsumable(
    version: Int = DEFAULT_RETURN_VERSION,
    updatedIds: List<Long> = listOf()
) = Consumable(version, updatedIds)

internal fun catalogBundleConsumable(
    synchronizableCatalogBundle: SynchronizableCatalogBundle = synchronizableCatalogBundle(),
    catalogConsumable: Consumable = catalogConsumable()
) = CatalogBundleConsumable(synchronizableCatalogBundle, catalogConsumable)

internal fun catalogConsumable(version: Int = DEFAULT_RETURN_VERSION) =
    Consumable(version)

fun synchronizeAccountKeyBuilder(
    key: SynchronizableKey = DEFAULT_KEY,
    returnVersion: Int = DEFAULT_RETURN_VERSION
) =
    object : SynchronizeAccountKeyBuilder(key) {
        override fun version(): Int = returnVersion
    }

internal fun synchronizableTrackingEntity(
    bundleKey: SynchronizableKey = DEFAULT_KEY,
    uuid: UUID = UUID.randomUUID(),
    uploadStatus: UploadStatus = UploadStatus.COMPLETED,
    isDeletedLocally: Boolean = false
): SynchronizableTrackingEntity {
    return SynchronizableTrackingEntity(
        bundleKey = bundleKey,
        uuid = uuid,
        uploadStatus = uploadStatus,
        isDeletedLocally = isDeletedLocally
    )
}

internal fun synchronizableItemWrapper(
    entity: SynchronizableTrackingEntity = synchronizableTrackingEntity(),
    item: SynchronizableItem = synchronizableItem(uuid = entity.uuid)
): SynchronizableItemWrapper = SynchronizableItemWrapper(
    synchronizableItem = item,
    trackingEntity = entity
)

internal fun mockConflictResolutionToReturnRemote(bundle: SynchronizableItemBundle) {
    whenever(bundle.conflictStrategy.resolve(any(), any()))
        .thenAnswer {
            val item = it.getArgument<SynchronizableItem?>(0)
            val remoteItem = it.getArgument<SynchronizableItem>(1)

            ConflictResolution(
                remoteSynchronizable = remoteItem,
                localSynchronizable = item,
                resolvedSynchronizable = remoteItem

            )
        }
}
