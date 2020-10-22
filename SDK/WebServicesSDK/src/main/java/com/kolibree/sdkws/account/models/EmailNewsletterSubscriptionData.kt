/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class EmailNewsletterSubscriptionData(
    @SerializedName("news_email_subscription") val subscribed: Boolean
)
