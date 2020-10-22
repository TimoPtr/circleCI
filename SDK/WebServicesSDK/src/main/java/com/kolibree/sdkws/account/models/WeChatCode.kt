package com.kolibree.sdkws.account.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/** WeChat connect identification code */
@Keep
data class WeChatCode(
    @field:SerializedName("code") val code: String
)
