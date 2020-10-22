package com.kolibree.sdkws.profile.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Guillaume Agis on 29/10/2018.
 */
data class PictureResponse(
    @field:SerializedName("picture") val picture: String,
    @field:SerializedName("picture_last_modifier") val pictureLastModifier: String
)
