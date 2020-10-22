package com.kolibree.account

import androidx.annotation.Keep
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.defensive.Preconditions

/**
 * Created by Guillaume Agis on 19/10/2018.
 */
@Keep
data class Account(
    val pubId: String,
    val backendId: Long,
    val phoneNumber: String?,
    val email: String?,
    val ownerProfileId: Long,
    val weChatData: WeChatData?,
    val profiles: List<IProfile>
) {
    init {
        Preconditions.checkArgument(pubId.isNotEmpty())
        Preconditions.checkArgumentNonNegative(backendId)
        Preconditions.checkArgumentNonNegative(ownerProfileId)
    }
}

@Keep
data class WeChatData(
    val openId: String,
    val unionId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val scope: String
)
