/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.di

import com.kolibree.android.offlinebrushings.OfflineBrushingsResourceProvider
import com.kolibree.android.offlinebrushings.V1OfflineBrushingsResourceProvider
import dagger.Module
import dagger.Provides

@Module
object V1OfflineBrushingsModule {

    @Provides
    fun provideOfflineBrushingsResourceProvider(): OfflineBrushingsResourceProvider =
        V1OfflineBrushingsResourceProvider
}
