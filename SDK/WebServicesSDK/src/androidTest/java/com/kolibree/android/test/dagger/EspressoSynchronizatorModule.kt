package com.kolibree.android.test.dagger

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.sdkws.appdata.AppDataManager
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.core.NoOpAvatarCache
import com.kolibree.sdkws.profile.ProfileApi
import com.kolibree.sdkws.profile.ProfileManager
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module(includes = [EspressoSynchronizatorProfileApiModule::class])
object EspressoSynchronizatorModule {
    @Provides
    fun providesSynchronizator(): Synchronizator = mock()

    @Provides
    fun provideAppDataManager(): AppDataManager = mock()

    @Provides
    fun providesAvatarCache(): AvatarCache = NoOpAvatarCache
}

@Module
internal object EspressoSynchronizatorProfileApiModule {
    @Provides
    @AppScope
    fun provideProfileApi(): ProfileApi = mock()

    @Provides
    @AppScope
    fun providesProfileManager(): ProfileManager = mock()
}
