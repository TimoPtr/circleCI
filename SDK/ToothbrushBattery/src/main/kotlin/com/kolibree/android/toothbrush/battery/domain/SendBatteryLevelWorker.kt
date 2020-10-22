/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.toothbrush.battery.domain

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
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kolibree.account.utils.ForgottenToothbrush
import com.kolibree.account.utils.ToothbrushForgottenHook
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.network.toParsedResponseCompletable
import com.kolibree.android.toothbrush.battery.data.BatteryLevelApi
import com.kolibree.android.toothbrush.battery.data.model.SendBatteryLevelRequest
import com.kolibree.android.worker.LazyWorkManager
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber

internal class SendBatteryLevelWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val gson: Gson,
    private val batteryLevelApi: BatteryLevelApi
) : RxWorker(context, workerParameters) {

    override fun createWork(): Single<Result> {
        return Single
            .fromCallable(::readInput)
            .flatMap(::sendBatteryLevel)
            .toParsedResponseCompletable()
            .doOnError(Timber::e)
            .toSingleDefault(Result.success())
            .onErrorReturnItem(Result.failure())
            .doOnSubscribe { Timber.d("Sending battery level") }
            .doOnSuccess { Timber.d("Battery level has been sent") }
    }

    private fun readInput(): Input {
        return gson.fromJson(inputData.getString(INPUT_JSON)!!, Input::class.java).also {
            checkNotNull(it.request) // safety check for gson parsing
        }
    }

    private fun sendBatteryLevel(input: Input): Single<Response<ResponseBody>> {
        return batteryLevelApi.sendBatteryLevel(
            accountId = input.accountId,
            profileId = input.profileId,
            body = input.request
        )
    }

    class Factory @Inject constructor(
        private val gson: Gson,
        private val batteryLevelApi: BatteryLevelApi
    ) : WorkerFactory() {

        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return SendBatteryLevelWorker(
                appContext,
                workerParameters,
                gson,
                batteryLevelApi
            )
        }
    }

    class Configurator @Inject constructor(
        private val workManager: LazyWorkManager,
        private val gson: Gson
    ) {

        fun sendBatteryLevel(
            accountId: Long,
            profileId: Long,
            request: SendBatteryLevelRequest
        ) = Completable.fromAction {
            val input = Input(accountId, profileId, request)
            val inputData = workDataOf(
                INPUT_JSON to gson.toJson(input)
            )

            val work = buildWork(inputData)
            val workName = createWorkName(
                accountId,
                profileId,
                request.macAddress,
                request.serialNumber
            )

            workManager.get().enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, work)
            Timber.d("Send work enqueued")
        }

        private fun buildWork(inputData: Data): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SendBatteryLevelWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(inputData)
                .build()
        }
    }

    class CancelHook @Inject constructor(
        private val workManager: LazyWorkManager
    ) : ToothbrushForgottenHook {

        override fun onForgottenCompletable(toothbrush: ForgottenToothbrush) =
            Completable.fromAction {
                val workName = createWorkName(
                    toothbrush.accountId,
                    toothbrush.profileId,
                    StrippedMac.fromMac(toothbrush.mac),
                    toothbrush.serial
                )
                workManager.get().cancelUniqueWork(workName)
            }
    }

    data class Input(
        @SerializedName("account_id") val accountId: Long,
        @SerializedName("profile_id") val profileId: Long,
        @SerializedName("request") val request: SendBatteryLevelRequest
    )

    internal companion object {
        private const val WORK_NAME = "SEND_BATTERY_LEVEL"
        const val INPUT_JSON = "INPUT_JSON"

        fun createWorkName(
            accountId: Long,
            profileId: Long,
            macAddress: StrippedMac,
            serialNumber: String
        ): String {
            return "$WORK_NAME-$accountId-$profileId-${macAddress.value}-$serialNumber"
        }
    }
}
