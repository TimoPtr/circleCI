package com.kolibree.sdkws.account.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Guillaume Agis on 07/11/2018.
 */
@Keep
data class WeeklyDigestData(@SerializedName("weekly_digest_subscription") val enable: Boolean)
