/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.AppScope
import javax.inject.Inject

/**
 * Provides [PendingIntent] to scan for toothbrushes in Nights Watch
 */
@AppScope
internal class ScanPendingIntentProvider @Inject constructor(context: Context) {
    private val applicationContext = context.applicationContext

    fun provide(): PendingIntent {
        val intent = Intent(applicationContext, ToothbrushScannedBroadcastReceiver::class.java)

        intent.action = TOOTHBRUSH_FOUND

        return PendingIntent.getBroadcast(
            applicationContext,
            TOOTHBRUSH_FOUND_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}

@VisibleForTesting
internal const val TOOTHBRUSH_FOUND_REQUEST_CODE = 1357

private const val TOOTHBRUSH_FOUND = "com.kolibree.android.offlinebrushings.TOOTHBRUSH_FOUND"
