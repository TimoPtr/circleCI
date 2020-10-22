/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.internal

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
data class AccountWechat(
    @ColumnInfo(name = "wc_openid") @SerializedName("openid") val wcOpenId: String,
    @ColumnInfo(name = "wc_unionid") @SerializedName("unionid") val wcUnionId: String,
    @ColumnInfo(name = "wc_access_token") @SerializedName("access_token") val wcAccessToken: String,
    @ColumnInfo(name = "wc_refresh_token") @SerializedName("refresh_token") val wcRefreshToken: String,
    @ColumnInfo(name = "wc_expires_in") @SerializedName("expires_in") val wcExpiresIn: Int,
    @ColumnInfo(name = "wc_scope") @SerializedName("scope") val wcScope: String
)
