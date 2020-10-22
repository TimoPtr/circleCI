/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import android.os.Handler
import android.os.Looper
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.sdk.connection.brushing.BrushingSessionMonitor
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import timber.log.Timber

internal class BrushingSessionMonitorImpl
    constructor(
        private val mainHandler: Handler
    ) : BrushingSessionMonitor {

    constructor() : this(Handler(Looper.getMainLooper()))

    private val sessionMonitorRelay = PublishRelay.create<Boolean>()

    override val sessionMonitorStream: Flowable<Boolean>
        get() = sessionMonitorRelay.hide().toFlowable(BackpressureStrategy.LATEST)

    fun onBrushingSessionStateChanged(started: Boolean) {
        Timber.d("Brushing session status stopped ? ${!started}")
        mainHandler.post {
            sessionMonitorRelay.accept(started)
        }
    }
}
