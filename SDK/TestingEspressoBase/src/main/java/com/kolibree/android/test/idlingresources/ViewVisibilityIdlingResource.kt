/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.idlingresources

import android.view.View
import androidx.annotation.IdRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.test.utils.IdlingResourceUtils

@VisibleForApp
class ViewVisibilityIdlingResource(
    @IdRes
    private val resId: Int,
    private val visibility: Int
) : OneTimeIdlingResource(
    name = "${ViewVisibilityIdlingResource::class.simpleName}: $resId waiting for $visibility"
) {

    override fun isIdle(): Boolean {
        val view = IdlingResourceUtils.findView<View>(resId)
        return view != null && view.visibility == visibility
    }
}
