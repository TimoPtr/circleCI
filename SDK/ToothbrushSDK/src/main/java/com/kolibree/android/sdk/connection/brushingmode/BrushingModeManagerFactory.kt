/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.core.driver.KLTBDriver
import com.kolibree.android.sdk.core.driver.ble.BleDriver

/** [BrushingModeManager] tests */
internal object BrushingModeManagerFactory {

    /** Create a [BrushingModeManager] instance */
    fun createBrushingModeManager(
        toothbrushModel: ToothbrushModel,
        driver: KLTBDriver,
        connectionState: ConnectionState
    ): BrushingModeManager =
        if (toothbrushModel.supportsVibrationSpeedUpdate())
            BrushingModeManagerImpl(
                bleDriver = driver as BleDriver,
                brushingModeChangedUseCase = BrushingModeChangedUseCase(),
                connectionState = connectionState
            )
        else
            NoBrushingModeManagerImpl()
}
