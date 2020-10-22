/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.idlingresources

import androidx.annotation.IdRes
import androidx.test.espresso.IdlingResource
import com.kolibree.android.annotation.VisibleForApp

/**
 * Here you can find all custom [IdlingResource]s that we have.
 */
@VisibleForApp
object IdlingResourceFactory {

    fun viewVisibility(
        @IdRes
        resId: Int,
        visibility: Int
    ) = ViewVisibilityIdlingResource(resId, visibility)

    fun viewAlpha(
        @IdRes
        resId: Int,
        targetAlphaValue: Float
    ) = ViewAlphaIdlingResource(resId, targetAlphaValue)

    fun viewClickable(
        @IdRes
        resId: Int,
        isClickable: Boolean
    ) = ViewClickableIdlingResource(resId, isClickable)

    fun viewEnabled(
        @IdRes
        resId: Int,
        isEnabled: Boolean
    ) = ViewEnabledIdlingResource(resId, isEnabled)

    fun textViewContent(
        @IdRes
        resId: Int,
        content: String
    ) = TextViewContentIdlingResource(resId, content)

    fun viewPagerIdle(
        @IdRes
        resId: Int
    ) = ViewPagerIdlingResource(resId)

    fun picassoIdle(): IdlingResource = PicassoIdlingResource().asIdlingResource()
}
