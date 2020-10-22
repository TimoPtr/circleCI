/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto

import android.annotation.SuppressLint

@SuppressLint("DeobfuscatedPublicSdkClass")
interface SecurityKeeper {

    val isLoggingAllowed: Boolean

    val testFeaturesAllowed: Boolean

    val isAuditAllowed: Boolean
}
