/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.kolibree.android.failearly.FailEarly
import timber.log.Timber

@Keep
abstract class BaseDaggerBroadcastReceiver : BroadcastReceiver() {

    abstract fun internalOnReceive(context: Context, intent: Intent?)

    abstract fun isDaggerReady(): Boolean

    abstract fun injectSelf(context: Context)

    final override fun onReceive(context: Context, intent: Intent?) {
        safeAndroidInjection(context)
        if (isDaggerReady().not()) {
            FailEarly.fail("Could not perform Android injection, canceling receiver...")
            return
        }

        internalOnReceive(context, intent)
    }

    private fun safeAndroidInjection(context: Context) = try {
        injectSelf(context)
    } catch (runtimeException: RuntimeException) {
        Timber.e(runtimeException)
    }
}
