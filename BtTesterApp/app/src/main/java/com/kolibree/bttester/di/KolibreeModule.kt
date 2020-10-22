package com.kolibree.bttester.di

import android.app.job.JobScheduler
import android.content.Context
import com.kolibree.android.network.environment.Credentials
import com.kolibree.android.network.environment.DefaultEnvironment
import com.kolibree.android.network.environment.Environment
import com.kolibree.android.network.retrofit.DeviceParameters
import com.kolibree.bttester.BuildConfig
import dagger.Module
import dagger.Provides

@Module
class KolibreeModule {

    @Provides
    internal fun provideFakeCredentials() = Credentials("FAKE", "FAKE")

    @Provides
    internal fun provideDeviceParameter(): DeviceParameters =
        DeviceParameters.create(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)

    @Provides
    internal fun providesDefaultEnvironment(): DefaultEnvironment =
        DefaultEnvironment(Environment.DEV)

    @Provides
    internal fun providesJobScheduler(context: Context): JobScheduler =
        context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
}
