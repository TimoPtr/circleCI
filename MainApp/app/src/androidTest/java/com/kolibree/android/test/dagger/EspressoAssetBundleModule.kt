package com.kolibree.android.test.dagger

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.app.loader.di.GameLoaderModule
import com.kolibree.android.app.loader.di.GameServiceModule
import com.kolibree.android.app.loader.repo.AssetBundlePreferences
import com.kolibree.android.app.loader.repo.AssetBundleRepository
import com.kolibree.android.app.loader.repo.api.AssetBundleApi
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module(includes = [AssetBundleInternalModule::class, EspressoAssetBundleApiModule::class, GameServiceModule::class, GameLoaderModule::class])
abstract class EspressoAssetBundleModule

@Module
internal object EspressoAssetBundleApiModule {
    @Provides
    @AppScope
    internal fun providesAssetBundleApi(): AssetBundleApi = mock()
}

@Module
internal object AssetBundleInternalModule {

    @Provides
    @AppScope
    internal fun bindsAssetBundlePreferences(): AssetBundlePreferences = mock()

    @Provides
    @AppScope
    internal fun bindsAssetBundleRepository(): AssetBundleRepository = mock()
}

/*@Module
class EspressoGameServiceModule {

    private val mock = mock<GameService>()

    init {
        whenever(mock.stateObservableForGame(any())).thenReturn(
            Observable.just(
            Ready(
                UnityGame.Pirate
            )
        ))
        whenever(mock.currentStateForGame(any())).thenReturn(Ready(
            UnityGame.Pirate
        ))
    }

    @Provides
    @AppScope
    internal fun bindsGameService(): GameService = mock
}*/
