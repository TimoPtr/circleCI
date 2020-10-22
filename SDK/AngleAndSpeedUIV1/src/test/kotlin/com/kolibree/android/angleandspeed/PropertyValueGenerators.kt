/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed

import com.kolibree.android.angleandspeed.common.logic.model.AngleFeedback
import com.kolibree.android.angleandspeed.common.logic.model.SpeedFeedback
import com.kolibree.android.sdk.disconnection.LostConnectionHandler
import io.kotlintest.properties.Gen
import kotlin.random.Random

internal fun lostConnectionState(): Gen<LostConnectionHandler.State> = Gen.enum()

internal fun boolean() = Gen.bool()

internal fun speedFeedback(): Gen<SpeedFeedback> = Gen.enum()

fun angleDegrees(): Gen<AngleFeedback> = object : Gen<AngleFeedback> {

    override fun constants(): Iterable<AngleFeedback> = emptyList()

    override fun random(): Sequence<AngleFeedback> = generateSequence {
        AngleFeedback(
            roll = (-359 + Random.nextInt(720)).toFloat(),
            pitch = (-359 + Random.nextInt(720)).toFloat(),
            yaw = (-359 + Random.nextInt(720)).toFloat()
        )
    }
}
