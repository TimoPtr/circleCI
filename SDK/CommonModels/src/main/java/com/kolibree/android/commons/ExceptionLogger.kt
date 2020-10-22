/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import androidx.annotation.Keep

/**
 * This interface allow to catch some exception and log them
 * in another system like Firebase.
 *
 * NoOpExceptionLogger should be use to just ignore the exception
 */
@Keep
interface ExceptionLogger {
    fun logException(throwable: Throwable)
}

@Keep
object NoOpExceptionLogger : ExceptionLogger {
    override fun logException(throwable: Throwable) {
        // no-op
    }
}
