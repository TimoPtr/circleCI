/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class NightsWatchMacsToScanProvider
@Inject constructor(private val toothbrushRepository: ToothbrushRepository) {
    fun provide(): List<String> {
        return toothbrushRepository.listAll()
            .subscribeOn(Schedulers.io())
            .blockingGet()
            .map { it.mac }
    }
}
