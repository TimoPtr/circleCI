/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.lifetimesmiles

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class LifetimeSmilesResponse(
    @SerializedName("lifetime_points") val lifetimePoints: Int
)
