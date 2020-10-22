/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.start

import com.kolibree.android.app.Error
import com.kolibree.android.app.ui.ota.R
import com.kolibree.android.toothbrushupdate.OtaUpdateBlocker

internal fun mapBlockerToError(blocker: OtaUpdateBlocker): Error =
    Error.from(messageId = when (blocker) {
        OtaUpdateBlocker.CONNECTION_NOT_ACTIVE -> R.string.ota_blocker_not_active_connection
        OtaUpdateBlocker.NOT_CHARGING -> R.string.ota_blocker_not_charging
        OtaUpdateBlocker.NOT_ENOUGH_BATTERY -> R.string.ota_blocker_not_enough_battery
        OtaUpdateBlocker.NO_GRUWARE_DATA -> R.string.ota_blocker_no_internet
    })
