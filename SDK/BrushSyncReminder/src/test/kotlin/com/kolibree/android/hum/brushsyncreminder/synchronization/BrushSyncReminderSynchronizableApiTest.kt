/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.synchronization

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderDao
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderEntity
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.sdkws.account.AccountManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Test

class BrushSyncReminderSynchronizableApiTest : BaseUnitTest() {

    private val accountManager: AccountManager = mock()
    private val accountDatastore: AccountDatastore = mock()
    private val brushingReminderDao: BrushSyncReminderDao = mock()

    private lateinit var brushSyncReminderSynchronizableApi: BrushSyncReminderSynchronizableApi

    override fun setup() {
        super.setup()

        brushSyncReminderSynchronizableApi =
            BrushSyncReminderSynchronizableApi(
                accountManager,
                accountDatastore,
                brushingReminderDao
            )
    }

    @Test(expected = NoSuchElementException::class)
    fun `get throws if account not available`() {
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty())

        brushSyncReminderSynchronizableApi.get(1)
    }

    @Test(expected = IllegalStateException::class)
    fun `get throws if unable to sync brush sync reminder`() {
        val kolibreeId = 1L
        val account = AccountInternal()
        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(account))
        whenever(accountManager.getBrushSyncReminderEnabled(account.id, kolibreeId))
            .thenReturn(Single.error(IllegalStateException("Test exception")))

        brushSyncReminderSynchronizableApi.get(kolibreeId)
    }

    @Test
    fun `get returns synchronizable item`() {
        val kolibreeId = 1L
        val account = AccountInternal()
        val enabled = false

        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(account))
        whenever(accountManager.getBrushSyncReminderEnabled(account.id, kolibreeId))
            .thenReturn(Single.just(enabled))
        whenever(brushingReminderDao.findBy(kolibreeId))
            .thenReturn(Maybe.just(BrushSyncReminderEntity.new(kolibreeId)))

        val item = brushSyncReminderSynchronizableApi.get(kolibreeId)
        assert(item is BrushSyncReminderSynchronizableItem)
        assertEquals(kolibreeId, item.kolibreeId)
        assertEquals(enabled, (item as BrushSyncReminderSynchronizableItem).brushReminder.isEnabled)
    }

    @Test(expected = IllegalStateException::class)
    fun `create throws if item is not brush reminder`() {
        val mockItem: SynchronizableItem = mock()
        brushSyncReminderSynchronizableApi.createOrEdit(mockItem)
    }

    @Test(expected = NoSuchElementException::class)
    fun `create throws if account not available`() {
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty())

        val mockItem: BrushSyncReminderSynchronizableItem = mock()
        brushSyncReminderSynchronizableApi.createOrEdit(mockItem)
    }

    @Test(expected = IllegalStateException::class)
    fun `create throws if unable to sync brush sync reminder`() {
        val kolibreeId = 1L
        val account = AccountInternal()
        val item =
            BrushSyncReminderSynchronizableItem(
                brushReminder = BrushSyncReminderEntity.new(kolibreeId)
            )

        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(account))
        whenever(
            accountManager.setBrushSyncReminderEnabled(
                account.id,
                item.brushReminder.profileId,
                item.brushReminder.isEnabled
            )
        )
            .thenReturn(Completable.error(IllegalStateException("Test exception")))

        brushSyncReminderSynchronizableApi.createOrEdit(item)
    }

    @Test
    fun `create returns synchronizable item`() {
        val kolibreeId = 1L
        val account = AccountInternal()
        val item =
            BrushSyncReminderSynchronizableItem(
                brushReminder = BrushSyncReminderEntity.new(kolibreeId)
            )

        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(account))

        whenever(
            accountManager.setBrushSyncReminderEnabled(
                account.id,
                item.brushReminder.profileId,
                item.brushReminder.isEnabled
            )
        )
            .thenReturn(Completable.complete())

        val createdItem = brushSyncReminderSynchronizableApi.createOrEdit(item)
        assertEquals(item, createdItem)
    }
}
