/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence

/** [BrushingModeSettingsBuilder] extension that allows usage of custom sequence */
// Do not keep /!\ Kolibree internal use only
@VisibleForApp
class BrushingModeSettingsTweaker : BrushingModeSettingsBuilder() {

    fun addSegmentWithCustomSequence(strength: Int) = apply {
        addSegment(
            sequenceId = BrushingModeSequence.CUSTOM_MODE_SEQUENCE_BLE_INDEX,
            strength = strength
        )
    }
}
