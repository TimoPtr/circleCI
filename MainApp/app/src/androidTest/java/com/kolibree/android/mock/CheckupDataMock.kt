/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mock

import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.processedbrushings.PlaqlessCheckupData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.threeten.bp.Duration

fun mockCheckupData(
    coverage: Int,
    cleanScoreValue: Int? = null,
    durationSeconds: Long = DEFAULT_BRUSHING_GOAL.toLong()
): CheckupData {
    val plaqlessData = mock<PlaqlessCheckupData>()
    whenever(plaqlessData.cleanPercent).thenReturn(cleanScoreValue)

    return mock<CheckupData>().apply {
        whenever(surfacePercentage).thenReturn(coverage)
        whenever(plaqlessCheckupData).thenReturn(plaqlessData)
        whenever(duration).thenReturn(Duration.ofSeconds(durationSeconds))
    }
}
