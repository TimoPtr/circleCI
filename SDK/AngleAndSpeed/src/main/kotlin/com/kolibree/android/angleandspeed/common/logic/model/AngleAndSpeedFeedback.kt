/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.logic.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class AngleAndSpeedFeedback(
    val angleDegrees: AngleFeedback,
    val speedFeedback: SpeedFeedback,
    val isZoneCorrect: Boolean
) : Parcelable {

    override fun toString(): String {
        return "AngleAndSpeedFeedback(" +
            "anglesDegrees=[roll=${angleDegrees.roll},pitch=${angleDegrees.pitch},yaw=${angleDegrees.yaw}]," +
            "speedFeedback=$speedFeedback," +
            "isZoneCorrect=$isZoneCorrect" +
            ")"
    }
}
