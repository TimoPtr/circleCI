/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import com.kolibree.android.app.BaseKolibreeApplication
import com.kolibree.android.app.dagger.EspressoAppComponent
import com.kolibree.android.app.loader.di.GameServiceModule
import com.kolibree.android.app.loader.entity.AssetBundle
import com.kolibree.android.app.loader.entity.DownloadCancelled
import com.kolibree.android.app.loader.entity.DownloadError
import com.kolibree.android.app.loader.entity.Downloading
import com.kolibree.android.app.loader.entity.GameClosed
import com.kolibree.android.app.loader.entity.GameLoaded
import com.kolibree.android.app.loader.entity.GameState
import com.kolibree.android.app.loader.entity.GameVersion
import com.kolibree.android.app.loader.entity.LoaderVersion
import com.kolibree.android.app.loader.entity.MandatoryUpdateRequired
import com.kolibree.android.app.loader.entity.NotInstalled
import com.kolibree.android.app.loader.entity.Ready
import com.kolibree.android.app.loader.entity.Unsupported
import com.kolibree.android.app.loader.entity.UpdateAvailable
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single

class UnityGameMocker private constructor() {

    private val gameStates = mutableListOf<GameState>()

    fun addGameState(gameState: GameState): UnityGameMocker {
        gameStates.add(gameState)
        return this
    }

    fun build() {
        GameServiceModule.gameService = null // Hack to re-create a new GameService
        // TODO maybe move the minSupportedGames to a global var
        gameStates.forEach(::mockGameState)
    }

    private fun mockGameState(state: GameState) {
        when (state) {
            is NotInstalled -> {
                val initialGameVersion = GameVersion("0.0.0")
                val minSupportedGames = hashMapOf(Pair(state.game.id, initialGameVersion))
                whenever(component().assetBundleRepository().refreshAssets()).thenReturn(
                    Single.just(
                        LoaderVersion("2.0.4", minSupportedGames)
                    )
                )
            }
            is Downloading -> {
                val initialGameVersion = GameVersion("0.0.0")
                val minSupportedGames = hashMapOf(Pair(state.game.id, initialGameVersion))
                whenever(component().assetBundleRepository().refreshAssets()).thenReturn(
                    Single.just(
                        LoaderVersion("2.0.4", minSupportedGames)
                    )
                )
                component().gameService().onNewGameState(state)
            }
            is UpdateAvailable -> {
                val initialGameVersion = GameVersion("0.0.0")
                val minSupportedGames = hashMapOf(Pair(state.game.id, initialGameVersion))

                whenever(component().assetBundlePreferences().loadLocal(state.game)).thenReturn(
                    AssetBundle(
                        state.game,
                        "",
                        "",
                        initialGameVersion
                    )
                )

                whenever(component().assetBundlePreferences().loadServer(state.game)).thenReturn(
                    AssetBundle(
                        state.game,
                        "",
                        "",
                        GameVersion("99999.0.0")
                    )
                )

                whenever(component().assetBundleRepository().refreshAssets()).thenReturn(
                    Single.just(
                        LoaderVersion("2.0.4", minSupportedGames)
                    )
                )
            }
            is MandatoryUpdateRequired -> {
                val initialGameVersion = GameVersion("9999.0.0")
                val minSupportedGames = hashMapOf(Pair(state.game.id, initialGameVersion))

                whenever(component().assetBundlePreferences().loadLocal(state.game)).thenReturn(
                    AssetBundle(
                        state.game,
                        "",
                        "",
                        GameVersion("0.0.0")
                    )
                )

                whenever(component().assetBundleRepository().refreshAssets()).thenReturn(
                    Single.just(
                        LoaderVersion("2.0.4", minSupportedGames)
                    )
                )
            }
            is Ready -> {
                val initialGameVersion = GameVersion("0.0.0")
                val minSupportedGames = hashMapOf(Pair(state.game.id, initialGameVersion))

                whenever(component().assetBundlePreferences().loadLocal(state.game)).thenReturn(
                    AssetBundle(
                        state.game,
                        "",
                        "",
                        initialGameVersion
                    )
                )

                whenever(component().assetBundleRepository().refreshAssets()).thenReturn(
                    Single.just(
                        LoaderVersion("2.0.4", minSupportedGames)
                    )
                )
            }
            is Unsupported -> {
                TODO("Not Implemented")
            }
            is DownloadError -> {
                TODO("Not Implemented")
            }
            is DownloadCancelled -> {
                TODO("Not Implemented")
            }
            is GameLoaded -> {
                TODO("Not Implemented")
            }
            is GameClosed -> {
                TODO("Not Implemented")
            }
        }
    }

    companion object {
        private fun component(): EspressoAppComponent = BaseKolibreeApplication.appComponent as EspressoAppComponent

        @JvmStatic
        fun create(): UnityGameMocker = UnityGameMocker()

        fun emptyUnityGame() {
            whenever(component().assetBundleRepository().refreshAssets()).thenReturn(
                Single.never()
            )
        }
    }
}
