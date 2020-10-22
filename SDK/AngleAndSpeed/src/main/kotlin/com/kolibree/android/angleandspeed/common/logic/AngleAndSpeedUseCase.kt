/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.logic

import androidx.annotation.Keep
import com.kolibree.android.angleandspeed.common.logic.model.AngleAndSpeedFeedback
import com.kolibree.kml.MouthZone16
import io.reactivex.Flowable

@Keep
interface AngleAndSpeedUseCase {

    val angleAndSpeedFlowable: Flowable<AngleAndSpeedFeedback>

    fun setPrescribedZones(prescribedZones: Array<MouthZone16>)
}
