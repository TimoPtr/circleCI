package com.kolibree.sdkws.brushing.wrapper

import androidx.annotation.Keep
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

/**
 * Brushing interface.
 * Your brushing object will need to implement this interface in order to use the brushing module
 * Those fields are required.
 */
@Keep
interface IBrushing {
    val duration: Long // duration of the brushing in seconds
    val goalDuration: Int // goal duration set in the profile
    val dateTime: OffsetDateTime
    val processedData: String? // processed data during the brushing . no need to use or modify the json inside
    val game: String?
    val toothbrushMac: String?
    val kolibreeId: Long? // Kolibree backend unique ID
    val profileId: Long

    val durationObject: Duration
        get() = Duration.ofSeconds(duration)

    fun hasProcessedData(): Boolean = !processedData.isNullOrEmpty()

    companion object {
        // TODO this is a temporary solution and should be removed in the future
        const val PATCH_INCORRECT_BRUSHING_GOAL_VALUES = true

        const val MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS = 10
        const val MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS = Int.MAX_VALUE
        const val MINIMUM_BRUSHING_GOAL_TIME_SECONDS = 120
        const val MAXIMUM_BRUSHING_GOAL_TIME_SECONDS = 300
        const val BRUSHING_GOAL_TIME_STEP_SECONDS = 5

        fun brushingGoalTimes(): List<Int> = IntProgression.fromClosedRange(
            MINIMUM_BRUSHING_GOAL_TIME_SECONDS,
            MAXIMUM_BRUSHING_GOAL_TIME_SECONDS,
            BRUSHING_GOAL_TIME_STEP_SECONDS
        ).toList()
    }
}
