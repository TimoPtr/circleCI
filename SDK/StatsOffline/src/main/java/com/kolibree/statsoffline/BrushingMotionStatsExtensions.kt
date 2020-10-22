/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
@file:Suppress("TooManyFunctions")
package com.kolibree.statsoffline

import com.kolibree.statsoffline.models.BrushingMotionStats
import org.threeten.bp.LocalDate

internal fun Map<LocalDate, BrushingMotionStats>.calculateCorrectMovementAverage(): Double =
    values.calculateCorrectMovementAverage()

internal fun Map<LocalDate, BrushingMotionStats>.calculateUnderSpeedAverage(): Double =
    values.calculateUnderSpeedAverage()

internal fun Map<LocalDate, BrushingMotionStats>.calculateCorrectSpeedAverage(): Double =
    values.calculateCorrectSpeedAverage()

internal fun Map<LocalDate, BrushingMotionStats>.calculateOverSpeedAverage(): Double =
    values.calculateOverSpeedAverage()

internal fun Map<LocalDate, BrushingMotionStats>.calculateCorrectOrientationAverage(): Double =
    values.calculateCorrectOrientationAverage()

internal fun Map<LocalDate, BrushingMotionStats>.calculateOverPressureAverage(): Double =
    values.calculateOverPressureAverage()

internal fun Collection<BrushingMotionStats>.calculateCorrectMovementAverage(excludeZero: Boolean = true): Double =
    map { it.correctMovementAverage }.calculateStatsAverage(excludeZero)

internal fun Collection<BrushingMotionStats>.calculateUnderSpeedAverage(excludeZero: Boolean = true): Double =
    map { it.underSpeedAverage }.calculateStatsAverage(excludeZero)

internal fun Collection<BrushingMotionStats>.calculateCorrectSpeedAverage(excludeZero: Boolean = true): Double =
    map { it.correctSpeedAverage }.calculateStatsAverage(excludeZero)

internal fun Collection<BrushingMotionStats>.calculateOverSpeedAverage(excludeZero: Boolean = true): Double =
    map { it.overSpeedAverage }.calculateStatsAverage(excludeZero)

internal fun Collection<BrushingMotionStats>.calculateCorrectOrientationAverage(excludeZero: Boolean = true): Double =
    map { it.correctOrientationAverage }.calculateStatsAverage(excludeZero)

internal fun Collection<BrushingMotionStats>.calculateOverPressureAverage(excludeZero: Boolean = true): Double =
    map { it.overPressureAverage }.calculateStatsAverage(excludeZero)
