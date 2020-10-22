/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.redeem

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.models.Redeem

@VisibleForApp
interface RedeemNetworkService {
    fun claimRedeem(redeemData: RedeemData): Redeem
}
