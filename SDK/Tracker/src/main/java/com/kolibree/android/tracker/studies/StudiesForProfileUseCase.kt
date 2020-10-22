/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.studies

import androidx.annotation.VisibleForTesting
import com.kolibree.account.utils.ToothbrushesForProfileUseCase
import io.reactivex.Single
import javax.inject.Inject

internal interface StudiesForProfileUseCase {

    fun provide(profileId: Long): Single<String>
}

internal class StudiesForProfileUseCaseImpl @Inject constructor(
    private val studiesRepository: StudiesRepository,
    private val toothbrushesForProfileUseCase: ToothbrushesForProfileUseCase
) : StudiesForProfileUseCase {

    override fun provide(profileId: Long) =
        toothbrushesForProfileUseCase
            .profileToothbrushesOnceAndStream(profileId)
            .elementAt(0, listOf())
            .flattenAsObservable { it }
            .map { studiesRepository.getStudy(it.toothbrush().mac) }
            .filter { it != NO_STUDY }
            .toList()
            .map { studies -> studies.joinToString(separator = STUDY_SEPARATOR) { it } }
            .onErrorReturn { "" }

    companion object {

        @VisibleForTesting
        const val STUDY_SEPARATOR = ";"
    }
}
