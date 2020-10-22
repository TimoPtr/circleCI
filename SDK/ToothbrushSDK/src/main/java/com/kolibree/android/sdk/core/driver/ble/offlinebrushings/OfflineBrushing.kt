/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.offlinebrushings

import androidx.annotation.Keep
import com.kolibree.android.commons.MIN_BRUSHING_DURATION_SECONDS
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime

@Keep
interface OfflineBrushing {
    val datetime: LocalDateTime
    val duration: Duration
    val processedData: String

    /**
     * Check record validity (brushing must have a duration &gt; 10 seconds)
     *
     * @return true if valid, false otherwise
     */
    fun isValid(): Boolean = duration.seconds >= MIN_BRUSHING_DURATION_SECONDS
}
