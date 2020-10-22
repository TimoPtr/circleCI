/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.utils

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeTweaker

internal fun KLTBConnection.brushingModeTweaker(): BrushingModeTweaker =
    brushingMode().customize() as BrushingModeTweaker
