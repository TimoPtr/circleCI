/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.challengeprogress

import android.os.Parcelable
import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
@VisibleForApp
data class ChallengeCompletionDetails(
    val completion: Int,
    val rules: Int
) : Parcelable
