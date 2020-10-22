/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.crashlogger

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ExceptionLogger
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.tracker.logic.userproperties.UserProperties.PUB_ID

object CrashLogger : ExceptionLogger {

    private const val DATE_TIME = "dateTime"

    override fun logException(throwable: Throwable) {
        updateDateTime()
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    fun setUserProperty(key: String, value: String) {
        updateDateTime()
        if (PUB_ID == key) FirebaseCrashlytics.getInstance().setUserId(value)
        FirebaseCrashlytics.getInstance().setCustomKey(key, value)
    }

    fun forceCrash() {
        FailEarly.fail("Crash forced! with fail early")
        throw IllegalStateException("Crash forced!")
    }

    private fun updateDateTime() {
        FirebaseCrashlytics.getInstance().setCustomKey(
            DATE_TIME,
            TrustedClock.getNowOffsetDateTime().toString()
        )
    }
}
