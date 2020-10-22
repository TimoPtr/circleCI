/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.android.synchronizator

import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableKey
import java.util.UUID
import kotlin.random.Random
import org.threeten.bp.ZonedDateTime

/*
Classes in this file represent a real implementation of the persistence layer of a
SynchronizableItemBundle

We need items to have a local primary key so that we can detect duplicates on insert and replace
them instead of ending with duplicate elements
 */

internal class TestSynchronizableItemDataStore(
    items: List<TestSynchronizableItem>,
    private var canHandle: Boolean
) : SynchronizableItemDataStore {
    private val _items = mutableListOf<TestSynchronizableItem>().apply { addAll(items) }

    fun items(): List<TestSynchronizableItem> = _items.toList()

    override fun updateVersion(newVersion: Int) {
        // no-op
    }

    fun insert(items: List<TestSynchronizableItem>) {
        items.forEach { insert(it) }
    }

    override fun insert(synchronizable: SynchronizableItem): TestSynchronizableItem {
        val testSynchronizableItem = synchronizable as ITestSynchronizableItem

        // replace strategy
        _items.removeIf { it.localId == testSynchronizableItem.localId }

        val item = localSynchronizableItem(
            kolibreeId = synchronizable.kolibreeId,
            createdAt = synchronizable.createdAt,
            updatedAt = synchronizable.updatedAt,
            uuid = synchronizable.uuid
        ).run {
            // simulate autogenerate PK. There's collision risk here...
            return@run if (localId == null)
                copy(
                    localId = Random.nextLong(
                        from = DEFAULT_REMOTE_ID + 1, // reduce collision risk
                        until = Long.MAX_VALUE
                    )
                )
            else
                this
        }

        _items.add(item)

        return item
    }

    override fun getByKolibreeId(kolibreeId: DataStoreId): SynchronizableItem? {
        return _items.singleOrNull { it.kolibreeId == kolibreeId }
    }

    override fun getByUuid(uuid: UUID): SynchronizableItem {
        return _items.single { it.uuid == uuid }
    }

    override fun delete(uuid: UUID) {
        _items.removeIf { it.uuid == uuid }
    }

    override fun canHandle(synchronizable: SynchronizableItem): Boolean = canHandle

    fun enableCanHandle() {
        canHandle = true
    }
}

private interface ITestSynchronizableItem : SynchronizableItem {
    val localId: DataStoreId?
}

internal data class TestSynchronizableItem(
    override val localId: DataStoreId?,
    override val kolibreeId: DataStoreId?,
    override val createdAt: ZonedDateTime,
    override val updatedAt: ZonedDateTime,
    override val uuid: UUID?
) : ITestSynchronizableItem {
    override fun withKolibreeId(kolibreeId: DataStoreId): SynchronizableItem =
        copy(kolibreeId = kolibreeId)

    override fun withUpdatedAt(updatedAt: ZonedDateTime): SynchronizableItem =
        copy(updatedAt = updatedAt)

    override fun updateFromLocalInstance(localItem: SynchronizableItem): SynchronizableItem {
        return copy(localId = (localItem as TestSynchronizableItem).localId)
    }

    override fun withUuid(uuid: UUID): SynchronizableItem = copy(uuid = uuid)
}

val DEFAULT_KEY = SynchronizableKey.CHALLENGE_CATALOG
const val DEFAULT_RETURN_VERSION = 2
const val DEFAULT_ID: DataStoreId = 30L
const val DEFAULT_REMOTE_ID: DataStoreId = 40L
