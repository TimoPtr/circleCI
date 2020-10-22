/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap.logic

import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.kml.MouthZone16
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class BrushingResults(
    override val coverage: Int = 0,
    override val duration: Int = 0,
    override val overSpeedPercentageMouthZones: Map<MouthZone16, Int> = emptyMap(),
    override val underSpeedPercentageMouthZones: Map<MouthZone16, Int> = emptyMap(),
    override val angle: Int = 0,
    override val successfulColorMouthZones: ColorMouthZones = ColorMouthZones.white(),
    override val coverageColorMouthZones: Map<MouthZone16, Float> = emptyMap(),
    override val speedColorMouthZones: ColorMouthZones = ColorMouthZones.white(),
    override val hasPlaqlessData: Boolean = false,
    override val plaqlessColorMouthZones: ColorMouthZones = ColorMouthZones.white(),
    override val missedAreas: Int = 0,
    override val buildUpRemains: Int = 0
) : MouthCoverageResult,
    SpeedResult,
    AngleResult,
    SuccessfulResult,
    PlaqlessResult,
    Parcelable

@Keep
interface Result

@Keep
interface SuccessfulResult : Result {
    val successfulColorMouthZones: ColorMouthZones
}

@Keep
interface MouthCoverageResult : Result {
    val coverage: Int
    val duration: Int
    val coverageColorMouthZones: Map<MouthZone16, Float>
    fun isPerfectCoverage() = coverage == 100
}

@Keep
interface SpeedResult : Result {
    val speedColorMouthZones: ColorMouthZones
    val overSpeedPercentageMouthZones: Map<MouthZone16, Int>
    val underSpeedPercentageMouthZones: Map<MouthZone16, Int>
}

@Keep
interface AngleResult : Result {
    val angle: Int
}

@Keep
interface PlaqlessResult : Result {
    val hasPlaqlessData: Boolean
    val plaqlessColorMouthZones: ColorMouthZones
    val duration: Int
    val missedAreas: Int
    val buildUpRemains: Int
    fun cleanScore() = 100 - missedAreas - buildUpRemains
}
