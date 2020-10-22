/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.di

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelper
import com.kolibree.android.brushingquiz.feature.BrushingQuizAnalyticsHelperImpl
import com.kolibree.android.brushingquiz.presentation.BrushingProgramActivity
import com.kolibree.android.brushingquiz.presentation.BrushingQuizActivityModule
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationLogoProvider
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeSettingModule
import com.kolibree.android.sdk.connection.brushingmode.SynchronizeBrushingModeUseCaseModule
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [
    BrushingModeSettingModule::class,
    SynchronizeBrushingModeUseCaseModule::class
])
abstract class BrushingProgramModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [BrushingQuizActivityModule::class])
    internal abstract fun bindBrushingQuizActivity(): BrushingProgramActivity

    @Binds
    internal abstract fun bindBrushingQuizAnalyticsHelper(
        impl: BrushingQuizAnalyticsHelperImpl
    ): BrushingQuizAnalyticsHelper

    @BindsOptionalOf
    abstract fun maybeBindQuizConfirmationLogoProvider(): QuizConfirmationLogoProvider
}
