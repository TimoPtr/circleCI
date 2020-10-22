/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing.startscreen

import android.app.Activity
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.home.pairing.startToothbrushPairingIntent

internal class PairingStartScreenNavigator : BaseNavigator<PairingStartScreenActivity>() {
    fun navigateToPairingFlowAndFinish() = withOwner {
        startToothbrushPairingIntent(this)

        setResult(Activity.RESULT_OK)

        finish()
    }

    fun navigateToShopAndFinish() = withOwner {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_RESULT_SHOP, true)
        })

        finish()
    }
}

internal fun shouldOpenShopAfterPairingStart(intent: Intent?): Boolean {
    if (intent == null) return false

    return intent.getBooleanExtra(EXTRA_RESULT_SHOP, false)
}

@VisibleForTesting
internal const val EXTRA_RESULT_SHOP = "extra_result_shop"
