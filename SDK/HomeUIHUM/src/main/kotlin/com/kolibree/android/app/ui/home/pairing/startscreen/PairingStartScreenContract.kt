/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing.startscreen

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.Canceled
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.OpenShop
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenResult.Success

@VisibleForApp
class PairingStartScreenContract : ActivityResultContract<Unit, PairingStartScreenResult>() {

    override fun createIntent(context: Context, param: Unit): Intent {
        return pairingStartScreenIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): PairingStartScreenResult {
        return when (resultCode) {
            RESULT_OK -> {
                if (shouldOpenShopAfterPairingStart(intent)) {
                    OpenShop
                } else {
                    Success
                }
            }
            else -> Canceled
        }
    }
}

@VisibleForApp
enum class PairingStartScreenResult {
    Success,
    OpenShop,
    Canceled
}
