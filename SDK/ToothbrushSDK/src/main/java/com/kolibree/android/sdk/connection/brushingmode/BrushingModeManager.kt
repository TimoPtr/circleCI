/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import androidx.annotation.Keep
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeCustomizer
import com.kolibree.android.sdk.error.CommandNotSupportedException
import io.reactivex.Completable
import io.reactivex.Single
import org.threeten.bp.OffsetDateTime

/** 3rd generation toothbrushes vibration speed update manager (E2 and B1) */
@Keep
interface BrushingModeManager {

    /**
     * Check whether the toothbrush is compatible with the Brushing Mode feature
     *
     * @return true if the Brushing Mode feature is available on the device, false otherwise
     */
    fun isAvailable(): Boolean

    /**
     * Get a list of the Brushing Modes that are available for this toothbrush
     *
     * @see [BrushingMode]
     *
     * @return available [BrushingMode] [List] [Single]
     *
     * B1 implements only Regular and Strong modes
     * E2 implements all modes
     * Other devices will emit a [CommandNotSupportedException]
     */
    fun availableBrushingModes(): Single<List<BrushingMode>>

    /**
     * Retrieve the last Brushing Mode set date for this toothbrush
     *
     * Only available in E2 and B1 toothbrushes
     * Other devices will emit a [CommandNotSupportedException]
     *
     * @return [OffsetDateTime] [Single]
     */
    fun lastUpdateDate(): Single<OffsetDateTime>

    /**
     * Set the toothbrush's brushing mode
     *
     * Only available in E2 and B1 toothbrushes
     * Other devices will emit a [CommandNotSupportedException]
     *
     * @see [BrushingMode]
     */
    fun set(mode: BrushingMode): Completable

    /**
     * Get the current Brushing Mode
     *
     * Only available in E2 and B1 toothbrushes
     * Other devices will emit a [CommandNotSupportedException]
     *
     * @see [BrushingMode]
     */
    fun getCurrent(): Single<BrushingMode>

    /**
     * Get the device's [BrushingModeCustomizer], an utility that allows Custom Brushing Mode
     * tweaking
     *
     * @return [BrushingModeCustomizer]
     */
    fun customize(): BrushingModeCustomizer
}
