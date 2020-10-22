/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger

import com.kolibree.android.app.ui.brushing.BrushingsForCurrentProfileUseCase
import com.kolibree.android.app.ui.game.DefaultUserActivityUseCase
import com.kolibree.android.app.ui.profile.NonActiveProfilesUseCase
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        EspressoDefaultUserActivityUseCaseModule::class,
        EspressoNonActiveProfilesUseCaseModule::class,
        EspressoBrushingUseCaseModule::class
    ]
)
object EspressoHomeScreenModule

@Module
object EspressoDefaultUserActivityUseCaseModule {

    val mock: DefaultUserActivityUseCase by lazy { mock<DefaultUserActivityUseCase>() }

    @Provides
    internal fun provideDefaultUserActivityUseCaseModule(): DefaultUserActivityUseCase = mock
}

@Module
object EspressoNonActiveProfilesUseCaseModule {

    val mock: NonActiveProfilesUseCase by lazy { mock<NonActiveProfilesUseCase>() }

    @Provides
    internal fun provideNonActiveProfilesUseCase(): NonActiveProfilesUseCase = mock
}

@Module
object EspressoBrushingUseCaseModule {

    val mock: BrushingsForCurrentProfileUseCase by lazy { mock<BrushingsForCurrentProfileUseCase>() }

    @Provides
    internal fun provideBrushingUseCase(): BrushingsForCurrentProfileUseCase = mock
}
