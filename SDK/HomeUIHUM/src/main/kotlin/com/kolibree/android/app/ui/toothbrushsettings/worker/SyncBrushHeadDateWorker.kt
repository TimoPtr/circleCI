/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorker.Companion.TOOTHBRUSH_MAC
import com.kolibree.android.app.ui.toothbrushsettings.worker.SyncBrushHeadDateWorker.Companion.TOOTHBRUSH_SERIAL_NUMBER
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.android.worker.LazyWorkManager
import com.kolibree.android.worker.WorkRequestBuilder
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

/**
 * When a Toothbrush is newly associated, this Worker retrieves
 * the most recent brush head from the server and save it locally.
 * If the Back-End response is a 404, then this Worker have the responsibility to send the new date.
 *
 * If there's a more recent local brush head reset date, it's ignored and we override it
 *
 * [See this Diagram for reference](https://kolibree.atlassian.net/browse/KLTB002-12857)
 */
internal class SyncBrushHeadDateWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val brushHeadConditionUseCase: BrushHeadConditionUseCase
) : RxWorker(context, workerParameters) {

    override fun createWork(): Single<Result> {

        val mac = inputData.getString(TOOTHBRUSH_MAC)
            ?: return Single.just(Result.failure())
                .also { Timber.e("Mac should be provided to this Worker") }

        val serialNumber = inputData.getString(TOOTHBRUSH_SERIAL_NUMBER)
            ?: return Single.just(Result.failure())
                .also { Timber.e("Serial Number should be provided to this Worker") }

        return brushHeadConditionUseCase.getBrushHeadInformationFromApi(mac, serialNumber)
            .doOnSubscribe { Timber.i("Start work") }
            .doAfterTerminate { Timber.i("Terminate work") }
            .doOnError(Timber::e)
            .flatMapCompletable(brushHeadConditionUseCase::writeBrushHeadInfo)
            .handleNonExistingBrushHeadError(mac)
            .toSingleDefault(Result.success())
            .onErrorReturnItem(Result.failure())
    }

    /**
     * We handle the scenario of an error 404, which means that we have to notify the back-end about
     * the new toothbrush head here.
     */
    private fun Completable.handleNonExistingBrushHeadError(mac: String): Completable {
        return onErrorResumeNext { error ->
            if (error is ApiError && isBrushHeadNonExisting(error)) {
                brushHeadConditionUseCase.updateBrushHeadDateIfNeeded(mac).ignoreElement()
            } else {
                Completable.error(error)
            }
        }
    }

    private fun isBrushHeadNonExisting(error: ApiError) =
        error.internalErrorCode == ApiErrorCode.BRUSH_HEAD_NON_EXISTING

    companion object {
        const val TOOTHBRUSH_MAC = "toothbrush_mac"
        const val TOOTHBRUSH_SERIAL_NUMBER = "toothbrush_serial_number"
    }
}

/**
 * Factory used for Dependency Injection
 */
internal class SyncBrushHeadDateWorkerFactory @Inject constructor(
    private val brushHeadConditionUseCase: BrushHeadConditionUseCase
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return SyncBrushHeadDateWorker(appContext, workerParameters, brushHeadConditionUseCase)
    }
}

internal class SyncBrushHeadDateWorkerBuilder @Inject constructor() :
    WorkRequestBuilder<OneTimeWorkRequest> {

    override fun buildRequest(data: Data): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<SyncBrushHeadDateWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(data)
            .build()
            .also { Timber.i("Worker scheduled to run") }
    }
}

internal class SyncBrushHeadWorkerDateConfigurator @Inject constructor(
    private val builder: SyncBrushHeadDateWorkerBuilder,
    private val workManager: LazyWorkManager,
    private val nameProvider: SyncBrushHeadWorkerNameProvider
) {

    fun configure(payload: Payload) {
        val workData = workDataOf(
            TOOTHBRUSH_MAC to payload.mac,
            TOOTHBRUSH_SERIAL_NUMBER to payload.serialNumber
        )

        workManager.get().enqueueUniqueWork(
            nameProvider.provide(payload.mac),
            ExistingWorkPolicy.REPLACE,
            builder.buildRequest(workData)
        )
    }

    internal data class Payload(val mac: String, val serialNumber: String)
}
