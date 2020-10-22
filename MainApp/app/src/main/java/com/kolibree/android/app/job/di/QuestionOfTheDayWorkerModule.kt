/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.job.di

import androidx.work.WorkerFactory
import com.kolibree.android.app.job.QuestionOfTheDayWorker
import com.kolibree.android.app.job.QuestionOfTheDayWorkerConfigurator
import com.kolibree.android.worker.AppStartupWorkerConfigurator
import com.kolibree.android.worker.WorkerKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet

@Module
internal interface QuestionOfTheDayWorkerModule {

    @Binds
    @IntoSet
    fun bindsQuestionOfTheDayWorkerConfigurator(
        configuration: QuestionOfTheDayWorkerConfigurator
    ): AppStartupWorkerConfigurator

    @Binds
    @IntoMap
    @WorkerKey(QuestionOfTheDayWorker::class)
    fun bindsQuestionOfTheDayWorkerFactory(factory: QuestionOfTheDayWorker.Factory): WorkerFactory
}
