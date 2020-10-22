/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode.customizer

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import io.reactivex.Completable
import io.reactivex.Single

/** Brushing Mode customizer. Allows Custom Brushing Mode tweaking */
@Keep
interface BrushingModeCustomizer {

    /**
     * Customize the toothbrush's Custom Brushing Mode
     *
     * @param brushingModeSettings [BrushingModeSettings]
     * @return [Completable]
     */
    fun setCustomBrushingModeSettings(brushingModeSettings: BrushingModeSettings): Completable

    /**
     * Get the settings of the customizable [BrushingMode]
     *
     * @return [BrushingModeSettings] [Single]
     */
    fun getCustomBrushingModeSettings(): Single<BrushingModeSettings>
}
