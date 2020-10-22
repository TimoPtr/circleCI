/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import androidx.annotation.Keep
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.KLTBConnection
import io.reactivex.Flowable
import javax.inject.Inject

@Keep
interface RingLedColorUseCase {
    fun getRingLedColor(connection: KLTBConnection): Flowable<Int>
}

internal class RingLedColorUseCaseImpl @Inject constructor(private val colorMapper: CoachPlaqlessRingLedColorMapper) :
    RingLedColorUseCase {
    override fun getRingLedColor(connection: KLTBConnection): Flowable<Int> =
        if (connection.toothbrush().model != ToothbrushModel.PLAQLESS) {
            Flowable.never()
        } else {
            connection.detectors().plaqlessRingLedState()
                .map { colorMapper.getRingLedColor(it) }
        }
}
