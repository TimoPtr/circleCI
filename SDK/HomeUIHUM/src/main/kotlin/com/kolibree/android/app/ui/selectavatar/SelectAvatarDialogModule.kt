/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.app.Application
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class SelectAvatarDialogModule {

    @ContributesAndroidInjector
    internal abstract fun bindSelectAvatarDialogFragment(): SelectAvatarDialogFragment

    @Binds
    internal abstract fun bindsPhotoFileProvider(provider: PhotoFileProviderPreferences): PhotoFileProvider

    @Binds
    internal abstract fun bindStoreAvatarProcessor(impl: SelectAvatarUseCase): AvatarSelectedUseCase

    @Binds
    internal abstract fun bindStoreAvatarProducer(impl: SelectAvatarUseCase): StoreAvatarProducer

    internal companion object {

        @Provides
        fun provideTempFileCreator(application: Application): TempFileCreator =
            ExternalTempFileCreator(application)
    }
}
