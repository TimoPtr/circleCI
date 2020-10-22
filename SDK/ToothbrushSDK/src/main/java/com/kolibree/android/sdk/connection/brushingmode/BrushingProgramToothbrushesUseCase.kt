/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

@Keep
class BrushingProgramToothbrushesUseCase @Inject constructor(
    private val toothbrushRepository: ToothbrushRepository,
    private val connectionProvider: KLTBConnectionProvider
) {
    /**
     * @return Single that will emit the [List<[KLTBConnection]>] associated to [profileId] that
     * support Brushing Program. Empty if there's no toothbrush that meets the requirements or if
     * the Profile doesn't have any Toothbrush associated
     */
    fun toothbrushesWithBrushingProgramSupport(profileId: Long): Single<List<KLTBConnection>> =
        profileToothbrushesWithBrushingProgram(profileId)
            .flattenAsObservable { it }
            .flatMap {
                connectionProvider.getKLTBConnectionSingle(it.mac)
                    .toObservable()
                    .doOnError(Timber::e)
                    .onErrorResumeNext(ObservableSource { Observable.empty<KLTBConnection>() })
            }
            .toList()

    @VisibleForTesting
    internal fun profileToothbrushesWithBrushingProgram(profileId: Long) =
        toothbrushRepository
            .listAll()
            .map { list ->
                list.filter { it.profileId == profileId && it.model.supportsVibrationSpeedUpdate() }
            }
}
