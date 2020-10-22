/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.di

import android.app.job.JobScheduler
import android.content.Context
import com.kolibree.android.commons.ExceptionLogger
import com.kolibree.android.commons.NoOpExceptionLogger
import com.kolibree.android.network.environment.Credentials
import com.kolibree.android.network.environment.DefaultEnvironment
import com.kolibree.android.network.environment.Environment
import com.kolibree.android.network.retrofit.DeviceParameters
import dagger.Module
import dagger.Provides

@Module
class KolibreeModule {

    @Provides
    internal fun provideFakeCredentials() = Credentials("FAKE", "FAKE")

    @Provides
    internal fun provideDeviceParameter(): DeviceParameters =
        DeviceParameters.create("", 0)

    @Provides
    internal fun providesDefaultEnvironment(): DefaultEnvironment =
        DefaultEnvironment(Environment.DEV)

    @Provides
    internal fun providesJobScheduler(context: Context): JobScheduler =
        context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    @Provides
    internal fun providesExceptionLogger(): ExceptionLogger =
        NoOpExceptionLogger
}
