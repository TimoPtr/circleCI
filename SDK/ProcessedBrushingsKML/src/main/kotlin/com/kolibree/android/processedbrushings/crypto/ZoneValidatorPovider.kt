/*
 * Copyright (c) 2020 Kolibree. All rights reserved
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
interface ZoneValidatorProvider {
    fun getZoneValidator(): String
}

internal class ZoneValidatorProviderImpl @Inject constructor(
    private val context: Context,
    private val kolibreeGuard: KolibreeGuard
) : ZoneValidatorProvider {

    override fun getZoneValidator(): String = String(
        kolibreeGuard.revealFromRaw(
            context,
            R.raw.zone_validator_1_3_0_json_enc,
            R.raw.zone_validator_1_3_0_json_iv,
            getZoneValidatorKey()
        )
    )

    private fun getZoneValidatorKey() = kolibreeGuard.revealFromString(
        context,
        R.string.zone_validator_key,
        R.string.zone_validator_iv
    )
}
