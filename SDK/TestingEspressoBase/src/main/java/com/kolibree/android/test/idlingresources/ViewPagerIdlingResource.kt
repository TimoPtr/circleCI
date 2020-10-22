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
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_DRAGGING
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.test.utils.IdlingResourceUtils

/**
 * Extracted from https://stackoverflow.com/a/32763454/218473
 *
 *
 * Created by miguelaragues on 4/4/18.
 * Updated by Maciej Sady on 5/5/20.
 */
@VisibleForApp
class ViewPagerIdlingResource(
    @IdRes
    private val resId: Int
) : OneTimeIdlingResource(
    name = "${ViewPagerIdlingResource::class.simpleName}: waiting for $resId to go idle"
) {

    private var isIdle = true // Default to idle since we can't query the scroll state.

    private val viewPagerListener = object : SimpleOnPageChangeListener() {
        override fun onPageScrollStateChanged(state: Int) {
            isIdle = (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_DRAGGING)
        }
    }

    init {
        IdlingResourceUtils.findView<ViewPager>(resId)
            ?.addOnPageChangeListener(viewPagerListener)
            ?: error("ViewPager with id: $resId not found!")
    }

    override fun isIdle(): Boolean {
        return isIdle
    }
}
