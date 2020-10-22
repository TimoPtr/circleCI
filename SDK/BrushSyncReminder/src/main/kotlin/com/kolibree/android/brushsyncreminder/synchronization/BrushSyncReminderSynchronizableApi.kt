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
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderDao
import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderEntity
import com.kolibree.android.synchronizator.SynchronizableItemApi
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.sdkws.account.AccountManager
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

internal class BrushSyncReminderSynchronizableApi @Inject constructor(
    private val accountManager: AccountManager,
    private val accountDatastore: AccountDatastore,
    private val brushingReminderDao: BrushSyncReminderDao
) : SynchronizableItemApi {

    override fun get(kolibreeId: Long): SynchronizableItem {
        return accountDatastore
            .getAccountMaybe()
            .getBrushSyncReminder(kolibreeId)
            .mapToSychronizableItem(kolibreeId)
            .blockingGet()
    }

    private fun Maybe<AccountInternal>.getBrushSyncReminder(
        kolibreeId: Long
    ): Single<Boolean> {
        return flatMapSingle { account ->
            accountManager.getBrushSyncReminderEnabled(
                accountId = account.id,
                profileId = kolibreeId
            )
        }
    }

    private fun Single<Boolean>.mapToSychronizableItem(
        kolibreeId: Long
    ): Single<BrushSyncReminderSynchronizableItem> {
        return flatMap { reminderEnabled ->
            brushingReminderDao
                .findBy(kolibreeId)
                .toSingle(BrushSyncReminderEntity.new(profileId = kolibreeId))
                .map { entity ->
                    BrushSyncReminderSynchronizableItem(
                        brushReminder = entity.copy(isEnabled = reminderEnabled)
                    )
                }
        }
    }

    override fun createOrEdit(synchronizable: SynchronizableItem): SynchronizableItem {
        return when (synchronizable) {
            is BrushSyncReminderSynchronizableItem -> createOrEditInternal(synchronizable)
            else -> error("Cannot create/edit $synchronizable")
        }
    }

    private fun createOrEditInternal(item: BrushSyncReminderSynchronizableItem): BrushSyncReminderSynchronizableItem {
        return accountDatastore
            .getAccountMaybe()
            .flatMapSingle { account ->
                accountManager
                    .setBrushSyncReminderEnabled(
                        accountId = account.id,
                        profileId = item.brushReminder.profileId,
                        enabled = item.brushReminder.isEnabled
                    )
                    .toSingleDefault(item)
            }
            .blockingGet()
    }
}
