package com.kolibree.android.offlinebrushings.persistence

import com.kolibree.android.offlinebrushings.di.ExtractOfflineBrushingsModule
import com.kolibree.android.offlinebrushings.sync.job.NightsWatchOfflineBrushingsCheckerModule
import com.kolibree.android.test.dagger.EspressoLastSyncObservableModule
import dagger.Module

@Module(
    includes = [EspressoRoomModule::class,
        EspressoOfflineBrushingsRepositoriesModule::class,
        EspressoLastSyncObservableModule::class,
        NightsWatchOfflineBrushingsCheckerModule::class,
        ExtractOfflineBrushingsModule::class]
)
object EspressoOfflineBrushingsModule
