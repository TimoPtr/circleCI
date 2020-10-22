/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.mindful.di

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.base.createAndBindToLifecycle
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.HeadspaceFeature
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMoment
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentUseCase
import com.kolibree.android.headspace.mindful.domain.HeadspaceMindfulMomentUseCaseImpl
import com.kolibree.android.headspace.mindful.domain.NoOpHeadspaceMindfulMomentUseCase
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentActivity
import com.kolibree.android.headspace.mindful.ui.HeadspaceMindfulMomentActivityNavigator
import com.kolibree.android.headspace.mindful.ui.card.HeadspaceMindfulMomentCardViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet

@Module(
    includes = [
        HeadspaceMindfulMomentBindingModule::class,
        HeadspaceMindfulMomentUseCaseModule::class
    ]
)
interface HeadspaceMindfulMomentScreenModule

@Module(includes = [HeadspaceMindfulMomentBindingModule::class])
interface HeadspaceMindfulMomentCoreModule

@Module
object HeadspaceMindfulMomentCardModule {

    @Provides
    @IntoSet
    internal fun provideHeadspaceMindfulMomentCardViewModel(
        fragment: BaseMVIFragment<*, *, *, *, *>,
        viewModelFactory: HeadspaceMindfulMomentCardViewModel.Factory
    ): DynamicCardViewModel<*, *, *> =
        viewModelFactory.createAndBindToLifecycle(
            fragment,
            HeadspaceMindfulMomentCardViewModel::class.java
        )
}

@Module
internal interface HeadspaceMindfulMomentBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [HeadspaceMindfulMomentActivityModule::class])
    fun bindHeadspaceMindfulMomentActivity(): HeadspaceMindfulMomentActivity
}

@Module
internal object HeadspaceMindfulMomentUseCaseModule {

    @Provides
    internal fun provideHeadspaceUseCase(
        impl: HeadspaceMindfulMomentUseCaseImpl,
        appConfiguration: AppConfiguration,
        featureToggleSet: FeatureToggleSet
    ): HeadspaceMindfulMomentUseCase =
        if (appConfiguration.showHeadspaceRelatedContent &&
            featureToggleSet.toggleIsOn(HeadspaceFeature)
        ) impl
        else {
            // Headspace is not supported by this configuration.
            // We'll use no-op implementation to satisfy the interface,
            // while skipping injection of the real implementation.
            NoOpHeadspaceMindfulMomentUseCase
        }
}

@Module
internal abstract class HeadspaceMindfulMomentActivityModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: HeadspaceMindfulMomentActivity): AppCompatActivity

    internal companion object {

        @Provides
        fun providesNavigator(activity: HeadspaceMindfulMomentActivity): HeadspaceMindfulMomentActivityNavigator =
            activity.createNavigatorAndBindToLifecycle(
                HeadspaceMindfulMomentActivityNavigator::class
            )

        @Provides
        fun providesMindfulMoment(activity: HeadspaceMindfulMomentActivity): HeadspaceMindfulMoment =
            activity.extractMindfulMoment()
    }
}
