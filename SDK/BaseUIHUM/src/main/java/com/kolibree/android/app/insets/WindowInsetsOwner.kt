/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.insets

import android.app.Activity
import android.view.WindowInsets
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import com.kolibree.android.failearly.FailEarly

/**
 * Represents an entity holding window insets (for ex. Activity).
 */
@Keep
interface WindowInsetsOwner {

    fun withWindowInsets(block: (WindowInsets) -> Unit)
}

@Keep
fun Fragment.withWindowInsetsOwner(block: (WindowInsets) -> Unit) {
    requireActivity().withWindowInsetsOwner(block)
}

@Keep
fun Activity.withWindowInsetsOwner(block: (WindowInsets) -> Unit) {
    if (this !is WindowInsetsOwner) {
        FailEarly.fail("Activity needs to implement WindowInsetsOwner!")
        return
    }
    withWindowInsets(block)
}
