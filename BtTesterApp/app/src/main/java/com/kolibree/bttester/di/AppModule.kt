package com.kolibree.bttester.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.kolibree.android.commons.ExceptionLogger
import com.kolibree.android.commons.NoOpExceptionLogger
import com.kolibree.sdkws.appdata.AppDataSyncEnabled
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.core.NoOpAvatarCache
import dagger.Module
import dagger.Provides

@Module
object AppModule {

    @Provides
    internal fun providesSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @AppDataSyncEnabled
    fun providesIsAppDataSyncEnabled() = false

    @Provides
    fun providesAvatarCache(): AvatarCache = NoOpAvatarCache

    @Provides
    internal fun providesExceptionLogger(): ExceptionLogger =
        NoOpExceptionLogger
}
