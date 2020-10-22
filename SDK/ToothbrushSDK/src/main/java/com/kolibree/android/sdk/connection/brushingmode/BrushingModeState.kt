/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import org.threeten.bp.OffsetDateTime

/** Brushing mode parameters data cache */
internal data class BrushingModeState(
    val currentMode: BrushingMode,
    val lastUpdateDate: OffsetDateTime,
    val availableModes: List<BrushingMode>
)
