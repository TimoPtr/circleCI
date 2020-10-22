/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class BrushSyncReminderMonitor
@Inject constructor(
    private val activeConnectionUseCase: ActiveConnectionUseCase,
    private val brushReminderUseCase: BrushSyncReminderUseCase
) : ApplicationLifecycleObserver {

    private var disposable: Disposable? = null

    override fun onApplicationStarted() {
        disposable = activeConnectionUseCase.onConnectionsUpdatedStream()
            .subscribeOn(Schedulers.io())
            .switchMapCompletable {
                brushReminderUseCase.scheduleReminders()
                    .onErrorComplete()
            }
            .subscribe(
                { },
                Timber::e
            )
    }

    override fun onApplicationStopped() = disposable.forceDispose()
}
