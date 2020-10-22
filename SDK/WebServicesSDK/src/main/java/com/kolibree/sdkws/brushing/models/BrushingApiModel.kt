package com.kolibree.sdkws.brushing.models

import com.google.gson.annotations.SerializedName

internal data class BrushingApiModel(
    @SerializedName("goal_duration") var goalDuration: Int,
    @SerializedName("id") var id: Long?,
    @SerializedName("processed_data") var processedData: String?
)
