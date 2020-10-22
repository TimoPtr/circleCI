package com.kolibree.sdkws.brushing.models

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.annotations.SerializedName
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import java.util.UUID
import org.threeten.bp.OffsetDateTime

/**
 * It's required as the model returned by the API is not the exactly the same as the
 * BrushingInternal and some action is needed to get the timestamp from
 * the date returned by the api.
 */

internal data class BrushingResponse(
    @SerializedName("game") val game: String,
    @SerializedName("duration") val duration: Long,
    @SerializedName("datetime") val datetime: OffsetDateTime,
    @SerializedName("profile") val profileId: Long,
    @SerializedName("coins") val coins: Int,
    @SerializedName("goal_duration") val goalDuration: Int,
    @SerializedName("processed_data") val processedData: JsonElement? = JsonNull.INSTANCE,
    @SerializedName("id") val kolibreeId: Long,
    @SerializedName("idempotency_key") val idempotencyKey: String
) {

    fun processedDataString(): String =
        if (processedData is JsonNull || processedData == null) "" else processedData.toString()
}

/**
 * Response from the WS
 */
internal data class BrushingsResponse(
    @SerializedName("brushings") private val brushings: List<BrushingResponse>,
    @SerializedName("total_coins") val totalCoins: Int = 0
) {

    internal fun getBrushings(): List<BrushingInternal> =
        brushings.map {
            BrushingInternal(
                game = it.game,
                duration = it.duration,
                profileId = it.profileId,
                coins = it.coins,
                goalDuration = it.goalDuration,
                kolibreeId = it.kolibreeId,
                processedData = it.processedDataString(),
                datetime = it.datetime,
                points = 0,
                isSynchronized = true,
                idempotencyKey = UUID.fromString(it.idempotencyKey)
            )
        }
}
