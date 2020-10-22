/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.crypto

import android.content.Context
import androidx.annotation.Keep
import com.kolibree.android.pirate_logic.R
import com.kolibree.crypto.KolibreeGuard
import javax.inject.Inject

/**
 * This provide the pirateLanes string needed by KML Contexts
 */
@Keep
interface PirateLanesProvider {
    fun getPirateLanes(): String
}

internal class PirateLanesProviderImpl @Inject constructor(
    private val context: Context,
    private val kolibreeGuard: KolibreeGuard
) : PirateLanesProvider {

    override fun getPirateLanes(): String {
        val key = kolibreeGuard.revealFromString(
            context,
            R.string.pirate_lanes_key,
            R.string.pirate_lanes_iv
        )

        return String(
            kolibreeGuard.revealFromRaw(
                context,
                R.raw.pirate_lanes_1_json_enc,
                R.raw.pirate_lanes_1_json_iv,
                key
            )
        )
    }
}
