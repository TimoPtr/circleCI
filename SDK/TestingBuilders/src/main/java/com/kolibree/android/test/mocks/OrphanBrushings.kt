/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks

import androidx.annotation.Keep
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.offlinebrushings.OrphanBrushing
import com.kolibree.android.test.mocks.BrushingBuilder.DEFAULT_PROCESSED_DATA
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import org.threeten.bp.OffsetDateTime

@JvmOverloads
@Keep
fun createOrphanBrushing(
    dateTime: OffsetDateTime = TrustedClock.getNowOffsetDateTime(),
    duration: Long = BrushingBuilder.DEFAULT_DURATION.toLong(),
    goalDuration: Int = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS,
    processedData: String = DEFAULT_PROCESSED_DATA,
    serial: String = KLTBConnectionBuilder.DEFAULT_SERIAL,
    mac: String = KLTBConnectionBuilder.DEFAULT_MAC
): OrphanBrushing {
    return OrphanBrushing.create(
        duration,
        goalDuration,
        processedData,
        dateTime,
        serial,
        mac
    )
}
