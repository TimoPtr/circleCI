/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.auditor

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.Keep
import com.instabug.library.Instabug
import com.instabug.library.logging.InstabugLog

@SuppressLint("LogNotTimber")
@Keep
class InstabugAuditTree : AuditTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (Instabug.isBuilt() && Instabug.isEnabled()) {
            when (priority) {
                Log.VERBOSE -> {
                    // don't log
                }
                Log.DEBUG -> InstabugLog.d(message)
                Log.INFO -> InstabugLog.i(message)
                Log.WARN -> InstabugLog.w(message)
                Log.ERROR -> InstabugLog.e(message)
                Log.ASSERT -> InstabugLog.wtf(message)
            }
        }
    }
}
