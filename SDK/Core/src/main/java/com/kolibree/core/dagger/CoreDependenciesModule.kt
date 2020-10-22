package com.kolibree.core.dagger

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.annotation.Keep
import com.kolibree.account.eraser.ClearUserContentJobService
import com.kolibree.account.logout.IntentAfterForcedLogout
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.commons.ExceptionLogger
import com.kolibree.android.commons.NoOpExceptionLogger
import com.kolibree.android.network.environment.Credentials
import com.kolibree.android.network.environment.DefaultEnvironment
import com.kolibree.android.network.environment.Environment
import com.kolibree.android.network.retrofit.DeviceParameters
import com.kolibree.android.network.retrofit.DeviceParameters.Companion.create
import com.kolibree.core.BuildConfig
import com.kolibree.sdkws.appdata.AppDataSyncEnabled
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.core.LocalAvatarCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Provider
import timber.log.Timber

@Module
abstract class CoreDependenciesModule {

    @Binds
    abstract fun bindsAvatarCache(impl: LocalAvatarCache): AvatarCache

    @Keep
    companion object {
        @Keep
        const val DI_CLIENT_SECRET_RES = "di_client_secret_res"

        @Keep
        const val DI_CLIENT_ID_RES = "di_client_id_res"

        @Keep
        const val DI_CLIENT_PROD = "di_client_prod"

        /**
         * Provide standard job scheduler
         *
         * @param context current context
         * @return JobScheduler
         */
        @Provides
        fun providesJobScheduler(context: Context): JobScheduler {
            return context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        }

        /**
         * Provide standard SharedPreferences
         *
         * @param context current context
         * @return SharedPreferences
         */
        @Provides
        fun providesSharedPreferences(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        /**
         * Provide Chinese environment
         *
         * @return Chinese environment
         */
        @Provides
        fun providesDefaultEnvironment(@Named(DI_CLIENT_PROD) isInProd: Boolean): DefaultEnvironment {
            return DefaultEnvironment(if (isInProd) Environment.CHINA else Environment.STAGING)
        }

        /**
         * Provide client credentials
         *
         * @param context current context
         * @param clientIdProvider client id provider
         * @param clientSecretProvider client secret provider
         */
        @Provides
        @AppScope
        @Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown", "ThrowsCount")
        fun providesCredentials(
            context: Context,
            @Named(DI_CLIENT_ID_RES) clientIdProvider: Provider<Int>,
            @Named(DI_CLIENT_SECRET_RES) clientSecretProvider: Provider<Int>
        ): Credentials {
            val clientId = context.getString(clientIdProvider.get())
            val secretClientSecret = context.getString(clientSecretProvider.get())
            if (clientId.isEmpty()) throw RuntimeException("The client ID is empty")
            if (secretClientSecret.isEmpty()) throw RuntimeException("The client secret is empty")
            return try {
                Credentials(clientId, secretClientSecret)
            } catch (e: Exception) {
                Timber.e(e)
                throw RuntimeException("The client secret is not encrypted")
            }
        }

        @Provides
        fun provideDeviceParameter(): DeviceParameters {
            return create(BuildConfig.SDK_VERSION_NAME, BuildConfig.SDK_VERSION_CODE)
        }

        @Provides
        fun provideIntentAfterForcedLogout(): IntentAfterForcedLogout? {
            return null
        }

        @Provides
        fun providesClearUserContentJobInfo(context: Context): JobInfo {
            return ClearUserContentJobService.createBuilder(context).build()
        }

        @Provides
        @AppDataSyncEnabled
        fun providesIsAppDataSyncEnabled(): Boolean {
            return false
        }

        @Provides
        fun providesExceptionLogger(): ExceptionLogger {
            return NoOpExceptionLogger
        }
    }
}
