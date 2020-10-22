package com.kolibree.sdkws.brushing

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.commons.interfaces.RemoteBrushingsProcessor
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsDatastore
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsDatastoreImpl
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepositoryImpl
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import com.kolibree.sdkws.brushing.wrapper.BrushingFacadeImpl
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
abstract class BrushingModule {
    @Binds
    internal abstract fun bindsBrushingManager(brushingManager: BrushingApiManagerImpl): BrushingApiManager

    @Binds
    internal abstract fun bindsBrushingRepository(brushingRep: BrushingsRepositoryImpl): BrushingsRepository

    @Binds
    internal abstract fun bindsBrushingDatastore(brushingsDatastore: BrushingsDatastoreImpl): BrushingsDatastore

    @Binds
    internal abstract fun bindsBrushingManagerWrapper(brushingManagerWrapper: BrushingFacadeImpl): BrushingFacade

    @BindsOptionalOf
    internal abstract fun bindRemoteBrushingsProcessor(): RemoteBrushingsProcessor

    internal companion object {
        @Provides
        @AppScope
        fun providesBrushingApiService(retrofit: Retrofit): BrushingApi =
            retrofit.create(BrushingApi::class.java)
    }
}
