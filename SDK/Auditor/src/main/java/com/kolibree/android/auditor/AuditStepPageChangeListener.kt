/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.auditor

import androidx.annotation.Keep
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import java.lang.ref.WeakReference
import timber.log.Timber

@Keep
class AuditStepPageChangeListener(
    activity: FragmentActivity,
    fragmentPagerAdapter: FragmentPagerAdapter,
    currentItem: Int
) :
    ViewPager.OnPageChangeListener {
    private val weakActivity = WeakReference(activity)
    private val weakPagerAdapter = WeakReference(fragmentPagerAdapter)

    init {
        logPosition(currentItem)
    }

    override fun onPageScrollStateChanged(state: Int) {
        // no-op
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // no-op
    }

    override fun onPageSelected(position: Int) {
        logPosition(position)
    }

    private fun logPosition(position: Int) {
        weakPagerAdapter.get()?.let { pagerAdapter ->
            if (position < pagerAdapter.count) {
                pagerAdapter.getItem(position)?.let { fragment ->
                    // fragment.getActivity is null at this point
                    weakActivity.get()?.let { activity ->
                        Auditor.instance().notifyViewPagerFragmentVisible(fragment, activity)
                    }
                } ?: Timber.e("Fragment at position %s is null", position)
            }
        }
    }
}
