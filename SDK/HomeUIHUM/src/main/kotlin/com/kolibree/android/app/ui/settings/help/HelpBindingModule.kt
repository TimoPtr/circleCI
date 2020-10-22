/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.help

import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class HelpBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [HelpActivityModule::class])
    internal abstract fun bindNotificationsActivity(): HelpActivity
}

@Module
internal object HelpActivityModule {

    @Provides
    fun providesHelpNavigator(activity: HelpActivity): HelpNavigator {
        return activity.createNavigatorAndBindToLifecycle(HelpNavigator::class)
    }
}
