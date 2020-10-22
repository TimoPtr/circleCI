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
import androidx.fragment.app.Fragment
import com.instabug.library.InstabugTrackingDelegate

class InstabugTracker : Tracker {
    override fun notifyViewPagerFragmentVisible(fragment: Fragment, activity: Activity?) {
        if (fragment is ViewPagerUserStep) {
            /*
            If we use notifyFragmentVisibilityChanged, it's difficult to see the steps in the web site
             */
            InstabugTrackingDelegate.notifyFragmentResumed(fragment, activity)
        }
    }

    override fun notifyFragmentViewCreated(view: View, fragment: Fragment, activity: Activity?) {
        if (fragment is UserStep) {
            InstabugTrackingDelegate.notifyFragmentViewCreated(view, fragment, activity)
        }
    }

    override fun notifyFragmentStarted(fragment: Fragment, activity: Activity?) {
        if (fragment is UserStep) {
            InstabugTrackingDelegate.notifyFragmentStarted(fragment, activity)
        }
    }

    override fun notifyFragmentResumed(fragment: Fragment, activity: Activity?) {
        if (fragment is UserStep) {
            InstabugTrackingDelegate.notifyFragmentResumed(fragment, activity)
        }
    }

    override fun notifyFragmentPaused(fragment: Fragment, activity: Activity?) {
        if (fragment is UserStep) {
            InstabugTrackingDelegate.notifyFragmentPaused(fragment, activity)
        }
    }

    override fun notifyFragmentStopped(fragment: Fragment, activity: Activity?) {
        if (fragment is UserStep) {
            InstabugTrackingDelegate.notifyFragmentStopped(fragment, activity)
        }
    }

    override fun notifyActivityGotTouchEvent(event: MotionEvent, activity: Activity?) {
        if (activity !is NoUserStep) {
            InstabugTrackingDelegate.notifyActivityGotTouchEvent(event, activity)
        }
    }
}
