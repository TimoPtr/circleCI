/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.ui.connect

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.kolibree.android.amazondash.ui.connect.AmazonDashConnectScreenResult.Closed
import com.kolibree.android.amazondash.ui.connect.AmazonDashConnectScreenResult.Connected
import com.kolibree.android.annotation.VisibleForApp
import org.threeten.bp.OffsetDateTime

@VisibleForApp
class AmazonDashConnectScreenContract : ActivityResultContract<
    Unit,
    AmazonDashConnectScreenResult>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return createAmazonDashConnectIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): AmazonDashConnectScreenResult {
        return when (resultCode) {
            Activity.RESULT_OK -> Connected(
                intent?.getSerializableExtra(EXTRA_SEND_REQUEST_TIME) as OffsetDateTime
            )
            else -> Closed
        }
    }
}

@VisibleForApp
sealed class AmazonDashConnectScreenResult {
    @VisibleForApp
    class Connected(val sendRequestTime: OffsetDateTime) : AmazonDashConnectScreenResult()

    @VisibleForApp
    object Closed : AmazonDashConnectScreenResult()
}
