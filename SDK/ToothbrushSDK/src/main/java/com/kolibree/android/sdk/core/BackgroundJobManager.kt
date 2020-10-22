/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import android.content.Context
import androidx.annotation.Keep

/**
 * Instances implementing [BackgroundJobManager] will be notified when the application is going to
 * background and *after* we've closed the connection to every toothbrush
 */
@Keep
interface BackgroundJobManager {
    fun scheduleJob(context: Context)
    fun cancelJob()
}
