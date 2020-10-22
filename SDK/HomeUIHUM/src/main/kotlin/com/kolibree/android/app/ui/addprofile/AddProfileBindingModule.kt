/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.selectavatar.SelectAvatarDialogModule
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class AddProfileBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [AddProfileActivityModule::class])
    internal abstract fun bindAddProfileActivity(): AddProfileActivity
}

@Module(includes = [SelectAvatarDialogModule::class])
internal object AddProfileActivityModule {

    @Provides
    fun providesAddProfileNavigator(activity: AddProfileActivity): AddProfileNavigator {
        return activity.createNavigatorAndBindToLifecycle(AddProfileNavigator::class)
    }
}
