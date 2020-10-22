/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import androidx.annotation.Keep
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * Show [DialogFragment] returned by [dialogFragmentInstance] if [FragmentManager] doesn't
 * contain another fragment with [tag]
 *
 * [dialogFragmentInstance] is only invoked if [tag] can't be found
 */
@Keep
fun FragmentManager.showIfNotPresent(tag: String, dialogFragmentInstance: () -> DialogFragment) {
    if (findFragmentByTag(tag) == null) {
        dialogFragmentInstance().showNow(this, tag)
    }
}
