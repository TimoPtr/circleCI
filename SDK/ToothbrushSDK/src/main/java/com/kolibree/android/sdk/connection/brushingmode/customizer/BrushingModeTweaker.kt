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
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurve
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.pattern.BrushingModePatternSettings
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequence
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequencePattern
import com.kolibree.android.sdk.connection.brushingmode.customizer.sequence.BrushingModeSequenceSettings
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Brushing Mode tweaker. Allows deep Custom Brushing Mode tweaking (internal version of
 * [BrushingModeCustomizer])
 */
// Do not keep, internal use only /!\
@VisibleForApp
interface BrushingModeTweaker {

    /**
     * Get the settings of the given [BrushingMode]
     *
     * @param mode [BrushingMode]
     * @return [BrushingModeSettings] [Single]
     */
    fun getBrushingModeSettings(mode: BrushingMode): Single<BrushingModeSettings>

    /**
     * Customize the toothbrush's Custom Brushing Mode
     *
     * @param brushingModeSettings [BrushingModeSettings]
     * @return [Completable]
     */
    fun setCustomBrushingModeSettings(brushingModeSettings: BrushingModeSettings): Completable

    /**
     * Get the settings of the given [BrushingModeSequence]
     *
     * @param sequence [BrushingModeSequence]
     * @return [BrushingModeSequenceSettings] [Single]
     */
    fun getSequenceSettings(sequence: BrushingModeSequence): Single<BrushingModeSequenceSettings>

    /**
     * Set the settings of the customizable [BrushingModeSequence]
     *
     * @param patterns [BrushingModeSequencePattern] [List]
     * @return [Completable]
     */
    fun setSequenceSettings(patterns: List<BrushingModeSequencePattern>): Completable

    /**
     * Get the settings of the given [BrushingModePattern]
     *
     * @param pattern [BrushingModePattern]
     * @return [BrushingModePatternSettings] [Single]
     */
    fun getPatternSettings(pattern: BrushingModePattern): Single<BrushingModePatternSettings>

    /**
     * Set the settings of the customizable [BrushingModePattern]
     *
     * @param settings [BrushingModePatternSettings]
     * @return [Completable]
     */
    fun setPatternSettings(settings: BrushingModePatternSettings): Completable

    /**
     * Get the [BrushingModeCurve] settings
     *
     * @param curve [BrushingModeCurve]
     * @return [BrushingModeCurveSettings] [Single]
     */
    fun getCurveSettings(curve: BrushingModeCurve): Single<BrushingModeCurveSettings>

    /**
     * Set the settings of a given [BrushingModeCurve]
     *
     * @param settings [BrushingModeCurveSettings]
     * @return [Completable]
     */
    fun setCurveSettings(settings: BrushingModeCurveSettings): Completable
}
