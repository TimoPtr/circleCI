/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations.usecases

import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.data.usecases.DeleteByUuidUseCase
import javax.inject.Inject

/**
 * UseCase to process KolibreeIds that have been removed remotely and might still exist locally in
 * the context of a Synchronize operation
 *
 * https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755460/Synchronization+support
 */
internal class DeleteByKolibreeIdUseCase
@Inject constructor(
    private val deleteByUuidUseCase: DeleteByUuidUseCase
) {
    fun delete(kolibreeId: DataStoreId, bundle: SynchronizableItemBundle) {
        bundle.run {
            dataStore.getByKolibreeId(kolibreeId)?.let { localItem ->
                localItem.uuid?.let { uuid ->
                    deleteByUuidUseCase.delete(uuid, bundle)
                } ?: FailEarly.fail("UUID shouldn't be null $localItem")
            }
        }
    }
}
