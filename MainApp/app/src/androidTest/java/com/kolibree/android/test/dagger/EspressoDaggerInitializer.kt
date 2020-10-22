/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.test.dagger

import android.app.Application
import android.content.Context
import androidx.databinding.DataBindingUtil
import com.kolibree.android.app.BaseKolibreeApplication
import com.kolibree.android.app.dagger.DaggerEspressoAppComponent
import com.kolibree.android.app.dagger.EspressoAppComponent
import com.kolibree.android.app.dagger.EspressoAppModule
import com.kolibree.android.offlinebrushings.persistence.EspressoOfflineBrushingsRepositoriesModule
import com.kolibree.android.sdk.EspressoSDKDaggerWrapper
import com.kolibree.android.sdk.core.BackgroundJobManager
import org.mockito.Mockito

/** Created by miguelaragues on 14/3/18.  */
object EspressoDaggerInitializer {

    @JvmOverloads
    @JvmStatic
    fun initialize(
        context: Context,
        roomRepositoriesModule: EspressoOfflineBrushingsRepositoriesModule? = roomRepositoriesModule()
    ): EspressoAppComponent {
        val sdkComponent = initSdkComponent(context)
        val appComponent = initAppComponent(context, sdkComponent, roomRepositoriesModule)

        appComponent.inject(context.applicationContext as BaseKolibreeApplication)

        DataBindingUtil.setDefaultComponent(appComponent)

        return appComponent
    }

    private fun initSdkComponent(context: Context): EspressoSdkComponent {
        val sdkComponent = DaggerEspressoSdkComponent.builder()
            .context(context.applicationContext)
            .backgroundJobManager(Mockito.mock(BackgroundJobManager::class.java))
            .build()

        EspressoSDKDaggerWrapper.setSdkComponent(sdkComponent)

        return sdkComponent
    }

    private fun initAppComponent(
        context: Context,
        sdkComponent: EspressoSdkComponent,
        roomRepositoriesModule: EspressoOfflineBrushingsRepositoriesModule?
    ): EspressoAppComponent {
        val appComponent = DaggerEspressoAppComponent.builder()
            .application(context.applicationContext as Application)
            .appModule(EspressoAppModule)
            .espressoRoomModule(roomRepositoriesModule)
            .espressoSdkComponent(sdkComponent)
            .build()

        BaseKolibreeApplication.appComponent = appComponent

        return appComponent
    }

    private fun roomRepositoriesModule(): EspressoOfflineBrushingsRepositoriesModule =
        EspressoOfflineBrushingsRepositoriesModule(true)
}
