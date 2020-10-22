/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.auditor

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.Fragment

@Keep
interface Tracker {
    fun notifyActivityGotTouchEvent(event: MotionEvent, activity: Activity?)

    fun notifyFragmentViewCreated(view: View, fragment: Fragment, activity: Activity?)

    fun notifyFragmentStarted(fragment: Fragment, activity: Activity?)

    fun notifyFragmentResumed(fragment: Fragment, activity: Activity?)

    fun notifyFragmentPaused(fragment: Fragment, activity: Activity?)

    fun notifyFragmentStopped(fragment: Fragment, activity: Activity?)

    fun notifyViewPagerFragmentVisible(fragment: Fragment, activity: Activity?)
}

internal class NoOpTracker : Tracker {
    override fun notifyViewPagerFragmentVisible(fragment: Fragment, activity: Activity?) {
        // no-op
    }

    override fun notifyFragmentViewCreated(view: View, fragment: Fragment, activity: Activity?) {
        // no-op
    }

    override fun notifyFragmentStarted(fragment: Fragment, activity: Activity?) {
        // no-op
    }

    override fun notifyFragmentResumed(fragment: Fragment, activity: Activity?) {
        // no-op
    }

    override fun notifyFragmentPaused(fragment: Fragment, activity: Activity?) {
        // no-op
    }

    override fun notifyFragmentStopped(fragment: Fragment, activity: Activity?) {
        // no-op
    }

    override fun notifyActivityGotTouchEvent(event: MotionEvent, activity: Activity?) {
        // no-op
    }
}
