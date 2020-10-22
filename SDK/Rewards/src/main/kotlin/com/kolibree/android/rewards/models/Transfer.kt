/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.models

import androidx.annotation.Keep

@Keep
data class Transfer(
    val smiles: Int,
    val fromProfileId: Long,
    val toProfileId: Long
)