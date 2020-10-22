/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.initializers

import android.annotation.SuppressLint
import android.app.Application
import com.kolibree.android.app.initializers.base.AppInitializer
import com.kolibree.android.brushsyncreminder.BrushSyncReminderUseCase
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * Schedules brush related reminders
 */
class BrushReminderAppInitializer @Inject constructor(
    private val brushReminderUseCase: BrushSyncReminderUseCase
) : AppInitializer {

    /**
     * Because it is not possible to bind to any application scope
     * we are just doing fire&forget here.
     *
     * We don't need to worry about leaks, because if app gets
     * killed all child "processes" are also killed.
     */
    @SuppressLint("RxLeakedSubscription", "CheckResult")
    override fun initialize(application: Application) {
        brushReminderUseCase.restoreReminders()
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }
}
