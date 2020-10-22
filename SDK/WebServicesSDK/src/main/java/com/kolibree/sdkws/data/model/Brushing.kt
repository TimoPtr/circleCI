package com.kolibree.sdkws.data.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.defensive.Preconditions
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import java.lang.IllegalArgumentException
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.OffsetDateTime

/**
 * Created by guillaumeagis 21/06/201
 */
@Keep
@Parcelize
data class Brushing(
    override val duration: Long, // Seconds
    // TODO change this back to val
    // when PATCH_INCORRECT_BRUSHING_GOAL_VALUES flag will be removed
    override var goalDuration: Int,
    override val dateTime: OffsetDateTime,
    val coins: Int, // not used, we keep it for retrocompatibility
    val points: Int, // not used, we keep it for retrocompatibility
    override var processedData: String?,
    override val profileId: Long,
    override val kolibreeId: Long? = null,
    override val game: String? = null,
    override val toothbrushMac: String? = null
) : Parcelable, IBrushing {

    init {
        try {
            Preconditions.checkArgumentInRange(
                goalDuration,
                IBrushing.MINIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
                IBrushing.MAXIMUM_ACCEPTABLE_BRUSHING_GOAL_TIME_SECONDS,
                "goal duration"
            )
        } catch (e: IllegalArgumentException) {
            // TODO this is a temporary solution and should be removed in the future
            @Suppress("ConstantConditionIf")
            if (IBrushing.PATCH_INCORRECT_BRUSHING_GOAL_VALUES) {
                goalDuration = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
            } else {
                throw e
            }
        }
        Preconditions.checkArgumentNonNegative(duration)
        Preconditions.checkArgumentNonNegative(coins)
        Preconditions.checkArgumentNonNegative(points)
        Preconditions.checkArgumentNonNegative(profileId)

        processedData = if (processedData == null) "" else processedData
    }
}
