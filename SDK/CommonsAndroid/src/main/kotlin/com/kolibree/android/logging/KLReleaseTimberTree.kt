/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.logging

import android.annotation.SuppressLint
import android.util.Log
import com.kolibree.android.commons.JavaLogger

@SuppressLint("LogNotTimber")
internal class KLReleaseTimberTree : KLTimberTree(), JavaLogger {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)

        when (priority) {
            Log.WARN -> Log.w(tag, message)
            Log.ERROR -> Log.e(tag, message)
            Log.ASSERT -> Log.wtf(tag, message)
            else -> {
                /*
                Ignore Log.VERBOSE, Log.DEBUG, Log.INFO for release

                It's ignored anyway by debuggable=true, but just in case
                 */
            }
        }
    }

    override fun debug(message: String) {
        // ignore
    }

    override fun warning(message: String) {
        w(message)
    }

    override fun error(throwable: Throwable?, message: String) {
        e(throwable, message)
    }

    override fun error(message: String) {
        e(message)
    }
}
