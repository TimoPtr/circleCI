/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.magiclink

import androidx.annotation.Keep

@Keep
data class MagicCode(
    val code: String,
    val alreadyValidated: Boolean
)
