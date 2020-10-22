package com.kolibree.android.offlinebrushings.persistence

import com.kolibree.android.commons.interfaces.Truncable
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
internal abstract class OfflineBrushingsRepositoriesModules {

    @Binds
    @IntoSet
    abstract fun providesOfflineBrushingsDatabase(repository: OrphanBrushingRepository): Truncable

    @Binds
    internal abstract fun bindOrphanBrushingRepository(
        orphanBrushingRepositoryRoom: OrphanBrushingRepositoryRoom
    ): OrphanBrushingRepository

    @Binds
    abstract fun bindSDKOrphanBrushingRepository(
        orphanBrushingRepositoryRoom: OrphanBrushingRepository
    ): SDKOrphanBrushingRepository
}
