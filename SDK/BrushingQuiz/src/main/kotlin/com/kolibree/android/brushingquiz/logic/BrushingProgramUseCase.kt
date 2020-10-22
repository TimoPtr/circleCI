/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.logic

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.sdk.connection.brushingmode.BrushingProgramToothbrushesUseCase
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

/** Brushing program feature utilities */
@Keep
interface BrushingProgramUseCase {

    /**
     * @return Single that will emit true if we must show Brushing program, false otherwise
     */
    fun shouldShowBrushingProgram(profileId: Long): Single<Boolean>

    /**
     * @return [Flowable] that will emit true if we must show Brushing program for the active profile,
     * false otherwise
     */
    fun shouldShowBrushingProgram(): Flowable<Boolean>
}

/** [BrushingProgramUseCase] implementation */
internal class BrushingProgramUtilsImpl @Inject constructor(
    private val brushingProgramToothbrushesUseCase: BrushingProgramToothbrushesUseCase,
    private val currentProfileProvider: CurrentProfileProvider
) : BrushingProgramUseCase {

    override fun shouldShowBrushingProgram(): Flowable<Boolean> =
        currentProfileProvider.currentProfileFlowable()
            .switchMapSingle { profile -> shouldShowBrushingProgram(profile.id) }

    /**
     * Check whether the given profile owns (is associated to) at least one device that implements
     * the Vibration Speed Update feature.
     *
     * If BrushingProgram feature toggle is disabled, it'll return false without checking for
     * toothbrushes
     */
    override fun shouldShowBrushingProgram(profileId: Long) =
        brushingProgramToothbrushesUseCase.toothbrushesWithBrushingProgramSupport(profileId)
            .map { it.isNotEmpty() }
}
