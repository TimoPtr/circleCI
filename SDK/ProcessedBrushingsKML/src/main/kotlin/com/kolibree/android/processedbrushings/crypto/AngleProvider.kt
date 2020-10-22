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

@Keep
interface AngleProvider {
    fun getSupervisedAngle(): String
    fun getKPIAngle(): String
}

internal class AngleProviderImpl @Inject constructor(
    private val context: Context,
    private val kolibreeGuard: KolibreeGuard
) : AngleProvider {

    override fun getKPIAngle(): String = String(
        kolibreeGuard.revealFromRaw(
            context,
            R.raw.kpi_angles_1_2_0_json_enc,
            R.raw.kpi_angles_1_2_0_json_iv,
            getKpiAnglesKey()
        )
    )

    override fun getSupervisedAngle(): String = String(
        kolibreeGuard.revealFromRaw(
            context,
            R.raw.coach_angles_1_5_0_json_enc,
            R.raw.coach_angles_1_5_0_json_iv,
            getSupervisedAnglesKey()
        )
    )

    private fun getKpiAnglesKey() = kolibreeGuard.revealFromString(
        context,
        R.string.kpi_angle_key,
        R.string.kpi_angle_iv
    )

    private fun getSupervisedAnglesKey() = kolibreeGuard.revealFromString(
        context,
        R.string.supervised_angle_key,
        R.string.supervised_angle_iv
    )
}
