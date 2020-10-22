/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.di

import com.kolibree.android.offlinebrushings.persistence.OfflineBrushingsRepositoriesModules
import com.kolibree.android.offlinebrushings.persistence.OfflineBrushingsRoomModule
import com.kolibree.android.offlinebrushings.sync.LastSyncModule
import com.kolibree.android.offlinebrushings.sync.job.NightsWatchOfflineBrushingsCheckerModule
import dagger.Module

@Module(
    includes = [OfflineBrushingsRoomModule::class,
        OfflineBrushingsRepositoriesModules::class,
        LastSyncModule::class,
        NightsWatchOfflineBrushingsCheckerModule::class,
        ExtractOfflineBrushingsModule::class]
)
object OfflineBrushingsModule
