/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.crashlogger

import com.kolibree.android.commons.ExceptionLogger
import com.kolibree.android.failearly.FailEarly

object CrashLogger : ExceptionLogger {

    override fun logException(throwable: Throwable) {
        // no-op
    }

    fun setUserProperty(key: String, value: String) {
        // no-op
    }

    fun forceCrash() {
        FailEarly.fail("Crash forced! with fail early")
        throw IllegalStateException("Crash forced!")
    }
}
