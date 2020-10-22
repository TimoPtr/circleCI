/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.redeem

import androidx.annotation.Keep

/**
 * See https://confluence.kolibree.com/pages/viewpage.action?pageId=15272055
 */
@Keep
data class RedeemApi(
    val redeemUrl: String?,
    val result: String,
    val rewardsId: Long
)
