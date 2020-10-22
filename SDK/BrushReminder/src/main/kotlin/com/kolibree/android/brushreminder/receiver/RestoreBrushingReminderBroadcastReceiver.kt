/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushreminder.receiver

import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.BaseDaggerBroadcastReceiver
import com.kolibree.android.brushreminder.BrushReminderUseCase
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/*
 * This class is needed for registering app for ACTION_BOOT_COMPLETED
 * Without it, brush sync reminder will not be re-scheduled after reboot of device
 */
@VisibleForApp
class RestoreBrushingReminderBroadcastReceiver : BaseDaggerBroadcastReceiver() {

    private var nextAlarmDisposable: Disposable? = null

    @VisibleForTesting
    @Inject
    lateinit var brushReminderUseCase: BrushReminderUseCase

    override fun internalOnReceive(context: Context, intent: Intent?) {
        nextAlarmDisposable = brushReminderUseCase.scheduleNextReminder()
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    override fun isDaggerReady(): Boolean = ::brushReminderUseCase.isInitialized

    override fun injectSelf(context: Context) {
        AndroidInjection.inject(this, context)
    }
}
