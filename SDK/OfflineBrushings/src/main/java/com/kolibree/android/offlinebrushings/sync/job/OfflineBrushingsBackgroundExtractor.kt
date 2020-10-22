/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.offlinebrushings.BrushingSyncedResult
import com.kolibree.android.offlinebrushings.ExtractOfflineBrushingsUseCase
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionPool
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Extracts Offline Brushings from [KLTBConnection] in [connectionPool] and emits a
 * [List]<[ProfileOfflineBrushingSyncResult]>
 */
internal class OfflineBrushingsBackgroundExtractor @Inject constructor(
    private val connectionPool: KLTBConnectionPool,
    private val extractOfflineBrushingsUseCase: ExtractOfflineBrushingsUseCase,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler
) {
    /**
     * Init ConnectionPool and wait for some time for connections to be established. We invoke
     * initOnlyPreviouslyScanned to avoid "is accessing your location on the background" dialog on
     * Android 10 https://kolibree.atlassian.net/browse/KLTB002-9867. There's a known issue with
     * the actual approach, please check documentation in EstablishConnectionFilter.
     *
     * Then, extract offline brushings and process them. More often than not, no offline brushing
     * will be pulled, so we'll just complete and finalize the job
     *
     * @return [Single]<[List]<[BrushingSyncedResult]>> that will emit a list of all OfflineBrushing extracted
     *
     * If we discarded the brushing session because [OfflineBrushing.isValid] returned false,
     * the list will be empty.
     */
    fun extractOfflineBrushingsOnce(): Single<List<BrushingSyncedResult>> = connectionPool.initOnlyPreviouslyScanned()
        .subscribeOn(Schedulers.io())
        .delay(WAIT_FOR_POOL_INIT_SECONDS, TimeUnit.SECONDS, timeoutScheduler, false)
        .andThen(Observable.defer { extractOfflineBrushingsUseCase.extractOfflineBrushings() })
        .lastOrError()
        .map {
            it.brushingsSynced
        }
        .doAfterTerminate { connectionPool.close() }
}

@VisibleForTesting
const val WAIT_FOR_POOL_INIT_SECONDS = 15L
