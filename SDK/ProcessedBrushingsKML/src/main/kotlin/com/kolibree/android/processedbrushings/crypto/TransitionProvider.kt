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
interface TransitionProvider {
    fun getTransition(): String
}

internal class TransitionProviderImpl @Inject constructor(
    private val context: Context,
    private val kolibreeGuard: KolibreeGuard
) : TransitionProvider {

    override fun getTransition(): String {
        val key = kolibreeGuard.revealFromString(
            context,
            R.string.transition_key,
            R.string.transition_iv
        )

        return String(
            kolibreeGuard.revealFromRaw(
                context,
                R.raw.transitions_1_4_0_json_enc,
                R.raw.transitions_1_4_0_json_iv,
                key
            )
        )
    }
}
