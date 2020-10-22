/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.about.di

import android.content.Context
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.settings.about.AboutActivity
import com.kolibree.android.app.ui.settings.about.AboutNavigator
import com.kolibree.android.utils.KolibreeAppVersions
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class AboutBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [AboutActivityModule::class])
    internal abstract fun bindAboutActivity(): AboutActivity
}

@Module
internal object AboutActivityModule {

    @Provides
    fun providesAppVersion(context: Context): KolibreeAppVersions = KolibreeAppVersions(context)

    @Provides
    fun providesAboutNavigator(
        activity: AboutActivity,
        factory: AboutNavigator.Factory
    ): AboutNavigator {
        return activity.createNavigatorAndBindToLifecycle(AboutNavigator::class) { factory }
    }
}
