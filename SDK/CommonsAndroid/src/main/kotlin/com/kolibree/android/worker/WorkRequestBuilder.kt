/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.worker

import androidx.work.Data
import androidx.work.WorkRequest
import com.kolibree.android.annotation.VisibleForApp

/**
 * Build the [WorkRequest], which can be either a [OneTimeWorkRequest] or a [PeriodicWorkRequest]
 * to the convenience of its subclass
 */
@VisibleForApp
interface WorkRequestBuilder<WR : WorkRequest> {
    fun buildRequest(data: Data = Data.EMPTY): WR
}
