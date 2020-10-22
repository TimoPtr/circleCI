/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectprofile.di

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.ui.selectprofile.NotSupportedSelectProfileUseCase
import com.kolibree.android.app.ui.selectprofile.SelectProfileDialogUseCase
import com.kolibree.android.app.ui.selectprofile.SelectProfileDialogUseCaseImpl
import com.kolibree.android.app.ui.selectprofile.SelectProfileNavigator
import com.kolibree.android.app.ui.selectprofile.SelectProfileUseCase
import com.kolibree.android.app.ui.selectprofile.SelectProfileUseCaseImpl
import com.kolibree.android.commons.AppConfiguration
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        SelectProfileUseCaseModule::class,
        SelectProfileDialogUseCaseModule::class,
        SelectProfileNavigatorModule::class
    ]
)
object SelectProfileModule

@Module
internal object SelectProfileNavigatorModule {
    @Provides
    internal fun providesNavigator(activity: AppCompatActivity): SelectProfileNavigator {
        return activity.createNavigatorAndBindToLifecycle(SelectProfileNavigator::class)
    }
}

@Module
internal abstract class SelectProfileDialogUseCaseModule {
    @Binds
    internal abstract fun bindsSelectProfileDialogUseCase(
        impl: SelectProfileDialogUseCaseImpl
    ): SelectProfileDialogUseCase
}

@Module
internal object SelectProfileUseCaseModule {
    @Provides
    fun providesSelectProfileUseCase(
        appConfiguration: AppConfiguration,
        selectProfileUseCase: SelectProfileUseCaseImpl
    ): SelectProfileUseCase {
        return if (appConfiguration.isSelectProfileSupported) {
            selectProfileUseCase
        } else {
            NotSupportedSelectProfileUseCase
        }
    }
}
