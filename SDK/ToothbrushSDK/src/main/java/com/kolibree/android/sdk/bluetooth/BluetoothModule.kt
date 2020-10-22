/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.bluetooth

import dagger.Module
import dagger.Provides

/** Created by miguelaragues on 19/9/17.  */
@Module
object BluetoothModule {

    @Provides
    internal fun providesBluetoothAdapterWrapper(): BluetoothAdapterWrapper {
        return BluetoothAdapterWrapperImpl.create()
    }
}
