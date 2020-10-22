/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.kolibree.android.glimmer.pairing.PairingBindingModule
import com.kolibree.sdkws.appdata.AppDataSyncEnabled
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.core.CoreModule
import com.kolibree.sdkws.core.NoOpAvatarCache
import dagger.Module
import dagger.Provides
import dagger.android.support.AndroidSupportInjectionModule

@Module(
    includes = [
        AndroidSupportInjectionModule::class,
        PairingBindingModule::class,
        CoreModule::class
    ]
)
object GlimmerTweakerApplicationModule {

    @Provides
    internal fun providesSharedPreferences(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @AppDataSyncEnabled
    fun providesIsAppDataSyncEnabled() = false

    @Provides
    fun providesAvatarCache(): AvatarCache = NoOpAvatarCache
}
