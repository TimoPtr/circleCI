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
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettingsBuilder.Companion.MAX_SEGMENT_COUNT
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettingsBuilder.Companion.MAX_STRENGTH
import com.kolibree.android.sdk.connection.brushingmode.customizer.BrushingModeSettingsBuilder.Companion.MIN_STRENGTH

/**
 * Thrown when attempting to add a segment in an already full segment list
 */
@Keep
class TooManySegmentsException : Exception(
    "Segments count limit ($MAX_SEGMENT_COUNT) already reached."
)

/**
 * Thrown when attempting to add a segment with an invalid strength value
 */
@Keep
class StrengthOutOfBoundsException : Exception(
    "Segment strength value must be in [$MIN_STRENGTH, $MAX_STRENGTH]"
)

/**
 * Thrown when the segment limit is not reached and there is no last segment
 */
@Keep
class NoLastSegmentException : Exception(
    "Segment count is below $MAX_SEGMENT_COUNT, last segment must be specified"
)
