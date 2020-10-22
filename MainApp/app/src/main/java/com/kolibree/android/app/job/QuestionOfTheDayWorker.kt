/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.job

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy.REPLACE
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.android.app.job.QuestionOfTheDayWorker.Companion.WORKER_TAG
import com.kolibree.android.clock.TrustedClock.getNowOffsetDateTime
import com.kolibree.android.extensions.toEpochMilli
import com.kolibree.android.questionoftheday.domain.QuestionOfTheDayUseCase
import com.kolibree.android.worker.AppStartupWorkerConfigurator
import com.kolibree.android.worker.LazyWorkManager
import com.kolibree.android.worker.WorkRequestBuilder
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

class QuestionOfTheDayWorker(
    context: Context,
    workerParameters: WorkerParameters,
    private val questionOfTheDayUseCase: QuestionOfTheDayUseCase,
    private val workerConfiguration: QuestionOfTheDayWorkerConfigurator
) : RxWorker(context, workerParameters) {

    override fun createWork(): Single<Result> {
        return Completable.defer { questionOfTheDayUseCase.refreshQuestions() }
            .doOnSubscribe { Timber.i("start work") }
            .doOnTerminate { Timber.i("terminate work") }
            .doOnError(Timber::i)
            .toSingleDefault(Result.success())
            .onErrorReturnItem(Result.retry())
            .doAfterTerminate { workerConfiguration.configure() }
    }

    companion object {
        const val WORKER_TAG = "QuestionOfTheDayWorker"
    }

    /**
     * Factory used for Dependency Injection
     */
    class Factory @Inject constructor(
        private val questionOfTheDayUseCase: QuestionOfTheDayUseCase,
        private val workerConfiguration: QuestionOfTheDayWorkerConfigurator
    ) : WorkerFactory() {

        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker {
            return QuestionOfTheDayWorker(
                appContext,
                workerParameters,
                questionOfTheDayUseCase,
                workerConfiguration
            )
        }
    }

    /**
     * Builder building the [OneTimeWorkRequest] for [QuestionOfTheDayWorker]
     * Note that there is yet no native ways to schedule a periodic request for a specific hour each day.
     * We can however schedule the request to target 4AM according to the current time difference.
     * The code has been inspired
     * by [this article](https://medium.com/androiddevelopers/workmanager-periodicity-ff35185ff006)
     */
    class Builder @Inject constructor() : WorkRequestBuilder<OneTimeWorkRequest> {

        override fun buildRequest(data: Data): OneTimeWorkRequest {
            val currentDate = getNowOffsetDateTime()
            val scheduledDate = getScheduledDate(currentDate)
            val delayBefore4AM = scheduledDate.toEpochMilli() - currentDate.toEpochMilli()

            return OneTimeWorkRequestBuilder<QuestionOfTheDayWorker>()
                .setInitialDelay(delayBefore4AM, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
                .also { Timber.i("Worker will start in : $delayBefore4AM millis") }
        }

        /**
         * Get the scheduled date for the task to be run
         */
        private fun getScheduledDate(currentDate: OffsetDateTime): OffsetDateTime {
            val scheduleDate = currentDate
                .truncatedTo(ChronoUnit.DAYS)
                .withHour(KOLIBREE_DAY_START_HOUR)

            return if (scheduleDate.isBefore(currentDate)) {
                scheduleDate.plusDays(1)
            } else {
                scheduleDate
            }
        }
    }
}

class QuestionOfTheDayWorkerConfigurator @Inject constructor(
    private val requestBuilder: QuestionOfTheDayWorker.Builder,
    private val workManager: LazyWorkManager
) : AppStartupWorkerConfigurator {

    override fun configure() {
        workManager.get().enqueueUniqueWork(WORKER_TAG, REPLACE, requestBuilder.buildRequest())
    }
}
