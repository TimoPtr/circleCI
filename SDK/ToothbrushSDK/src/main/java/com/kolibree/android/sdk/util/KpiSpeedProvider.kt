/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.util

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.R
import com.kolibree.crypto.KolibreeGuard

@Keep
interface KpiSpeedProvider {
    fun getKpiSpeed(): String
}

internal class KpiSpeedProviderImpl constructor(
    private val context: Context,
    private val kolibreeGuard: KolibreeGuard,
    private val toothbrushModel: ToothbrushModel
) : KpiSpeedProvider {

    override fun getKpiSpeed(): String {
        val key = kolibreeGuard.revealFromString(context, R.string.kpi_speed_key, R.string.kpi_speed_iv)
        val (encryptedFileRes, ivFileRes) = getEncryptedKpiRes(toothbrushModel)

        return String(kolibreeGuard.revealFromRaw(context, encryptedFileRes, ivFileRes, key))
    }

    /**
     * Pair<EncryptedFile, IvFile>
     */
    @VisibleForTesting
    fun getEncryptedKpiRes(toothbrushModel: ToothbrushModel): Pair<Int, Int> =
        Pair(R.raw.kpi_speed_ranges_json_enc, R.raw.kpi_speed_ranges_json_iv)
}
