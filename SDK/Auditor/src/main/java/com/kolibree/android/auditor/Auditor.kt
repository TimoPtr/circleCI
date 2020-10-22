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
import android.app.Application
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import java.net.HttpURLConnection

@Keep
interface Auditor : Tracker {
    val tracker: Tracker

    val auditTree: AuditTree

    fun init(
        application: Application,
        tags: String,
        tracker: Tracker? = NoOpTracker(),
        auditTree: AuditTree? = NoOpAuditTree()
    )

    override fun notifyActivityGotTouchEvent(event: MotionEvent, activity: Activity?) {
        tracker.notifyActivityGotTouchEvent(event, activity)
    }

    override fun notifyFragmentViewCreated(view: View, fragment: Fragment, activity: Activity?) {
        tracker.notifyFragmentViewCreated(view, fragment, activity)
    }

    override fun notifyFragmentStarted(fragment: Fragment, activity: Activity?) {
        tracker.notifyFragmentStarted(fragment, activity)
    }

    override fun notifyFragmentResumed(fragment: Fragment, activity: Activity?) {
        tracker.notifyFragmentResumed(fragment, activity)
    }

    override fun notifyViewPagerFragmentVisible(fragment: Fragment, activity: Activity?) {
        tracker.notifyViewPagerFragmentVisible(fragment, activity)
    }

    override fun notifyFragmentPaused(fragment: Fragment, activity: Activity?) {
        tracker.notifyFragmentPaused(fragment, activity)
    }

    override fun notifyFragmentStopped(fragment: Fragment, activity: Activity?) {
        tracker.notifyFragmentStopped(fragment, activity)
    }

    fun networkLog(urlConnection: HttpURLConnection)

    @Keep
    companion object {
        fun instance(): Auditor = AuditorInstance
    }
}
