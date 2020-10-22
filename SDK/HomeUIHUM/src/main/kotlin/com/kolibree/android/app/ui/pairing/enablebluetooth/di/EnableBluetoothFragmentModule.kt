/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.enablebluetooth.di

import com.kolibree.android.app.ui.pairing.enablebluetooth.EnableBluetoothFragment
import dagger.Module
import dagger.Provides

@Module
internal object EnableBluetoothFragmentModule {

    @Provides
    internal fun providesPopOnSuccess(
        fragment: EnableBluetoothFragment
    ): Boolean {
        return fragment.extractPopOnSuccess()
    }
}
