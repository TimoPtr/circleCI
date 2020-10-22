package com.kolibree.android.app.ui.brushhead.api.model.request.data

import com.google.gson.annotations.SerializedName
import com.kolibree.android.annotation.VisibleForApp
import org.threeten.bp.OffsetDateTime

@VisibleForApp
data class BrushHeadData(
    @SerializedName("first_used") val firstUsed: OffsetDateTime
)
