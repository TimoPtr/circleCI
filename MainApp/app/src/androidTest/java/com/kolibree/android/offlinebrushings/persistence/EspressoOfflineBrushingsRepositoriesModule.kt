package com.kolibree.android.offlinebrushings.persistence

import com.kolibree.android.app.dagger.AppScope
import dagger.Module
import dagger.Provides
import org.mockito.Mockito

/**
 * Provides a real or mocked Repository, according to the constructor parameter
 *
 */
@Module
class EspressoOfflineBrushingsRepositoriesModule constructor(private val useReal: Boolean) {

    @Provides
    @AppScope
    internal fun providesOrphanBrushingRepository(
        adapter: OrphanBrushingRepositoryRoom
    ): OrphanBrushingRepository {
        if (useReal) {
            return adapter
        }

        return Mockito.mock(OrphanBrushingRepository::class.java)
    }
}
