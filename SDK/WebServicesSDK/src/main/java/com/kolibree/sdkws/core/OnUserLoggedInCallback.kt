/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core

import android.annotation.SuppressLint

/**
 * Callback to be invoked when the user has successfully logged in
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
interface OnUserLoggedInCallback {
    fun onUserLoggedIn()

    fun onUserLoggedOut()
}
