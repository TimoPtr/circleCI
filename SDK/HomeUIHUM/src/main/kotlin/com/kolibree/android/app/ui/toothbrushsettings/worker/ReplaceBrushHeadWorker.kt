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
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorker.Companion.TOOTHBRUSH_MAC
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorker.Companion.TOOTHBRUSH_SERIAL_NUMBER
import com.kolibree.android.worker.LazyWorkManager
import com.kolibree.android.worker.WorkRequestBuilder
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

internal class ReplaceBrushHeadWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val brushHeadRepository: BrushHeadRepository
) : RxWorker(context, workerParameters) {

    override fun createWork(): Single<Result> {

        val mac = inputData.getString(TOOTHBRUSH_MAC)
            ?: return Single.just(Result.failure())

        val serialNumber = inputData.getString(TOOTHBRUSH_SERIAL_NUMBER)
            ?: return Single.just(Result.failure())

        return brushHeadRepository.brushHeadInformationOnce(mac)
            .sendDateToBackEnd(serialNumber)
            .doOnSubscribe { Timber.i("Start work") }
            .doOnTerminate { Timber.i("Terminate work") }
            .doOnError(Timber::i)
            .toSingleDefault(Result.success())
            .onErrorReturnItem(Result.retry())
    }

    private fun Single<BrushHeadInformation>.sendDateToBackEnd(serialNumber: String) =
        flatMapCompletable { info ->
            brushHeadRepository.sendReplacedDateToApiCompletable(
                mac = info.macAddress,
                serialNumber = serialNumber,
                replacedDate = info.resetDate
            )
        }

    companion object {
        const val TOOTHBRUSH_MAC = "toothbrush_mac"
        const val TOOTHBRUSH_SERIAL_NUMBER = "toothbrush_serial_number"
    }
}

/**
 * Factory used for Dependency Injection
 */
internal class Factory @Inject constructor(
    private val brushHeadRepository: BrushHeadRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return ReplaceBrushHeadWorker(appContext, workerParameters, brushHeadRepository)
    }
}

internal class Builder @Inject constructor() : WorkRequestBuilder<OneTimeWorkRequest> {

    override fun buildRequest(data: Data): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<ReplaceBrushHeadWorker>()
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

internal class ReplaceBrushHeadWorkerConfigurator @Inject constructor(
    private val builder: Builder,
    private val workManager: LazyWorkManager,
    private val nameProvider: ReplaceBrushHeadWorkerNameProvider
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
