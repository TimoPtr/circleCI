/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.activity

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment

@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun BaseActivity.getCurrentNavFragment(@IdRes navHostIt: Int): Fragment? {
    val navHostFragment = supportFragmentManager.findFragmentById(navHostIt)

    val fragments = navHostFragment?.childFragmentManager?.fragments

    return fragments?.firstOrNull()
}
