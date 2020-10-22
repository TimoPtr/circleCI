/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.idlingresources

import android.widget.TextView
import androidx.annotation.IdRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.test.utils.IdlingResourceUtils

@VisibleForApp
class TextViewContentIdlingResource(
    @IdRes
    private val resId: Int,
    private val content: String
) : OneTimeIdlingResource(
    name = "${TextViewContentIdlingResource::class.simpleName}: $resId waiting for $content"
) {

    override fun isIdle(): Boolean {
        val view = IdlingResourceUtils.findView<TextView>(resId)
        return view != null && view.text.toString() == content
    }
}
