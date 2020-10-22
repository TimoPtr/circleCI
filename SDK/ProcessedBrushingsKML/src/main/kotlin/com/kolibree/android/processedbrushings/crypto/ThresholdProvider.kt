/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings.crypto

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.processedbrushings.R
import com.kolibree.crypto.KolibreeGuard
import javax.inject.Inject

/**
 * This provide the transition string needed by KML Contexts
 */
@Keep
interface ThresholdProvider {
    fun getThresholdBalancing(): String
}

internal class ThresholdProviderImpl @Inject constructor(
    private val context: Context,
    private val kolibreeGuard: KolibreeGuard
) : ThresholdProvider {

    override fun getThresholdBalancing(): String {
        val key = kolibreeGuard.revealFromString(
            context,
            R.string.threshold_key,
            R.string.threshold_iv
        )

        return String(
            kolibreeGuard.revealFromRaw(
                context,
                R.raw.threshold_balancing_1_json_enc,
                R.raw.threshold_balancing_1_json_iv,
                key
            )
        ) }
}
