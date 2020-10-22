/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.core

import androidx.annotation.Keep

@Keep
data class UnityCredentials(
    val clientId: String,
    val clientSecret: String,
    val environment: String,
    val clientIv: String
)
