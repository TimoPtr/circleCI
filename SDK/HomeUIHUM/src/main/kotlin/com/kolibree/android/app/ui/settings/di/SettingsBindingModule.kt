/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.di

import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.selectprofile.di.SelectProfileModule
import com.kolibree.android.app.ui.settings.SettingsActivity
import com.kolibree.android.app.ui.settings.SettingsInitialAction
import com.kolibree.android.app.ui.settings.SettingsNavigator
import com.kolibree.android.app.ui.settings.brushingquiz.QuizConfirmationLogoProviderImpl
import com.kolibree.android.brushingquiz.di.BrushingProgramModule
import com.kolibree.android.brushingquiz.di.BrushingProgramUseCaseModule
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationLogoProvider
import com.kolibree.android.google.auth.GoogleSignInModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Keep
@Module(includes = [BrushingProgramModule::class])
abstract class SettingsBindingModule {

    @Binds
    internal abstract fun bindQuizConfirmationLogoProvider(
        impl: QuizConfirmationLogoProviderImpl
    ): QuizConfirmationLogoProvider

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            SettingsActivityNavigatorModule::class,
            SettingsActivityModule::class,
            SelectProfileModule::class
        ]
    )
    internal abstract fun bindSettingsActivity(): SettingsActivity
}

@Module(includes = [GoogleSignInModule::class, BrushingProgramUseCaseModule::class])
internal abstract class SettingsActivityModule {

    @Binds
    abstract fun bindMviActivity(
        implementation: SettingsActivity
    ): BaseMVIActivity<*, *, *, *, *>

    @Binds
    abstract fun bindAppCompatActivity(
        implementation: SettingsActivity
    ): AppCompatActivity

    companion object {

        @Provides
        fun provideInitialAction(activity: SettingsActivity): SettingsInitialAction? =
            activity.getInitialAction()
    }
}

@Module
internal object SettingsActivityNavigatorModule {

    @Provides
    fun providesSettingsNavigator(
        activity: SettingsActivity,
        factory: SettingsNavigator.Factory
    ): SettingsNavigator {
        return activity.createNavigatorAndBindToLifecycle(SettingsNavigator::class) { factory }
    }
}
