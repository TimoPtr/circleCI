/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import com.kolibree.account.utils.ToothbrushesForProfileUseCase
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.brushhead.sync.model.BrushHeadInformationSet
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import io.reactivex.Single
import javax.inject.Inject

internal class BrushHeadStatusSynchronizableApi @Inject constructor(
    private val brushHeadRepository: BrushHeadRepository,
    private val toothbrushesForProfileUseCase: ToothbrushesForProfileUseCase
) : SynchronizableReadOnlyApi {

    /**
     * Refresh profiles' toothbrushes brush head information
     *
     * This endpoint’s data is quite different from others
     *
     * Consider this scenario
     * 1. User logs in
     * 2. Pairs toothbrush with mac AA:BB. We associate the profile to the toothbrush
     * 3. Logs out. We forget the toothbrush but we don’t tell the backend.
     * 4. Logs in again. Sends `/synchronize?brush_head_usage=0', and we receive
     *
    ```
    "brush_head_usage": {
    "version": 2,
    "updated_ids": [<profileId>]
    }```
     * When processing this response, user has 0 toothbrushes, thus there’s no backend call to
     * perform.
     */
    override fun get(id: Long): SynchronizableReadOnly {
        return readProfileToothbrushes(id).toBrushHeadReplacedDates()
    }

    private fun readProfileToothbrushes(profileId: Long): Single<List<AccountToothbrush>> {
        return toothbrushesForProfileUseCase.profileAccountToothbrushesOnceAndStream(profileId)
            .take(1)
            .singleOrError()
    }

    private fun Single<List<AccountToothbrush>>.toBrushHeadReplacedDates(): BrushHeadInformationSet {
        return flattenAsObservable { it }
            .flatMapSingle { toothbrush ->
                brushHeadRepository.getBrushHeadInformationFromApi(
                    serialNumber = toothbrush.serial,
                    mac = toothbrush.mac
                )
            }
            .toList()
            .map { BrushHeadInformationSet(data = it.toSet()) }
            .blockingGet()
    }
}
