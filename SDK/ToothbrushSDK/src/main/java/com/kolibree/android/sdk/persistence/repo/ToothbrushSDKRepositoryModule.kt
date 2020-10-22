package com.kolibree.android.sdk.persistence.repo

import com.kolibree.android.sdk.persistence.room.AccountToothbrushDao
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class ToothbrushSDKRepositoryModule {

    @Binds
    abstract fun bindsToothbrushModule(toothbrushRepository: ToothbrushRepositoryImpl): ToothbrushRepository

    companion object {
        @Provides
        internal fun providesAccountToothbrushRepository(dao: AccountToothbrushDao): AccountToothbrushRepository =
            AccountToothbrushRepositoryImpl(dao)
    }
}
