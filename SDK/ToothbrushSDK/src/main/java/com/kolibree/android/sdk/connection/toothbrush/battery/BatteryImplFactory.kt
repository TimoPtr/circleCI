/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.GLINT
import com.kolibree.android.commons.ToothbrushModel.HILINK
import com.kolibree.android.commons.ToothbrushModel.HUM_BATTERY
import com.kolibree.android.commons.ToothbrushModel.HUM_ELECTRIC
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.core.driver.ble.BleDriver

/** [Battery] implementation factory */
internal class BatteryImplFactory {

    /**
     * Create a [Battery] implementation for 2nd and 3rd generation toothbrushes
     */
    fun createBatteryImplementation(model: ToothbrushModel, bleDriver: BleDriver): Battery =
        when (model) {
            CONNECT_E1,
            ARA,
            CONNECT_E2,
            PLAQLESS,
            HILINK,
            GLINT,
            HUM_ELECTRIC -> RechargeableBatteryImpl(bleDriver, usesCompatPayload(model))
            CONNECT_M1, CONNECT_B1,
            HUM_BATTERY -> ReplaceableBatteryImpl(bleDriver, usesCompatPayload(model))
        }

    @VisibleForTesting
    fun usesCompatPayload(model: ToothbrushModel) =
        when (model) {
            CONNECT_E1, ARA -> true
            CONNECT_M1, CONNECT_E2, CONNECT_B1, PLAQLESS, HILINK, HUM_BATTERY, HUM_ELECTRIC, GLINT -> false
        }
}
