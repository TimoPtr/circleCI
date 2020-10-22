/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.google.auth

import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
data class GoogleSignInCredentials(
    @StringRes val encryptedWebClientId: Int,
    @StringRes val iv: Int
)
