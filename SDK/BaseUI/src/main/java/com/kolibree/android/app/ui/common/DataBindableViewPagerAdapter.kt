/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.common

import android.annotation.SuppressLint

@SuppressLint("DeobfuscatedPublicSdkClass")
interface DataBindableViewPagerAdapter<in T : Any> {
    fun update(content: List<T>)
}
