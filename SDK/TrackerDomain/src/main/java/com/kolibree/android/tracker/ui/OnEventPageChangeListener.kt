/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.ui

import androidx.annotation.Keep
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.AnalyticsEvent

/** Created by Kornel on 4/4/2018.  */
@Keep
class OnEventPageChangeListener(
    private vararg val events: AnalyticsEvent
) : OnPageChangeListener {

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        // no-op
    }

    override fun onPageSelected(position: Int) {
        if (position >= 0 && position < events.size) {
            Analytics.send(events[position])
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        // no-op
    }
}
