/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import android.view.View
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.utils.DebouncedClickListener.Companion.doubleClickTimeoutDuration
import org.threeten.bp.Duration

/**
 * This class is an Helper to avoid double click (spam) on a view click listener
 * It will avoid it by dismiss click which are not separate by at least [doubleClickTimeoutDuration]
 *
 * @param debounceDuration duration object to specify the time you want by default.
 * It will be [doubleClickTimeoutDuration]
 * @param click the clickListener to apply when ot respect the constraint of time
 */
@Keep
class DebouncedClickListener(
    private val debounceDuration: Duration?,
    private val click: (v: View) -> Unit
) : View.OnClickListener {

    companion object {
        private val doubleClickTimeoutDuration: Duration = Duration.ofMillis(500)

        private var allowFastClicks: Boolean = false

        @VisibleForTesting
        @JvmStatic
        fun allowFastClicks() {
            allowFastClicks = true
        }
    }

    private var lastClick: Long = 0

    /**
     * Checker method, the first click is done (call [click] and store the last click time
     * into [lastClick])
     * when a second click arrived verify that the minimum time [debounceDuration] or if null
     * [doubleClickTimeoutDuration]
     * is pass otherwise drop the click
     *
     * @param v the view where the click appear
     */
    override fun onClick(v: View) {
        if (allowFastClicks ||
            getLastClickTimeout() > (debounceDuration ?: doubleClickTimeoutDuration).toNanos()
        ) {
            lastClick = System.nanoTime()
            click(v)
        }
    }

    private fun getLastClickTimeout(): Long =
        System.nanoTime() - lastClick
}
