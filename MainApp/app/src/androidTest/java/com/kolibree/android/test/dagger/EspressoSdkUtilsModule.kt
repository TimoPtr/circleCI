/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.test.dagger

import android.content.Context
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.sdk.connection.CheckConnectionPrerequisitesUseCase
import com.kolibree.android.sdk.core.AlwaysScanBeforeReconnectStrategy
import com.kolibree.android.sdk.core.ScanBeforeReconnectStrategy
import com.kolibree.android.sdk.location.LocationStatusListener
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

/** Created by miguelaragues on 19/9/17.  */
@Module
internal object EspressoSdkUtilsModule {
    /*
    I can't use the singleton scope that I need for testing, so I'm manually returning the same instance
    */
    private val bluetoothUtils: IBluetoothUtils = mock()

    private val locationStatusListener: LocationStatusListener = mock()

    private val checkConnectionPrerequisitesUseCase: CheckConnectionPrerequisitesUseCase = mock()

    @Provides
    fun providesApplicationContext(context: Context): ApplicationContext {
        return ApplicationContext(context)
    }

    @Provides
    fun providesScanBeforeReconnectStrategy(): ScanBeforeReconnectStrategy {
        return AlwaysScanBeforeReconnectStrategy
    }

    @Provides
    fun providesBluetoothUtils(): IBluetoothUtils {
        return bluetoothUtils
    }

    @Provides
    fun providesLocationStatusListener(): LocationStatusListener {
        return locationStatusListener
    }

    @Provides
    fun providesCheckConnectionPrerequisitesUseCase(): CheckConnectionPrerequisitesUseCase {
        return checkConnectionPrerequisitesUseCase
    }
}
