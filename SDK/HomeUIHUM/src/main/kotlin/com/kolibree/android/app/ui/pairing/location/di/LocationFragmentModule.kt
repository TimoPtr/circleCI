/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing.location.di

import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.ui.pairing.location.LocationFragment
import com.kolibree.android.app.ui.pairing.location.LocationNavigator
import com.kolibree.android.app.ui.pairing.location.LocationScreenType
import dagger.Module
import dagger.Provides

@Module
object LocationFragmentModule {

    @Provides
    internal fun providesPopOnSuccess(
        fragment: LocationFragment
    ): Boolean {
        return fragment.extractPopToScanListOnSuccess()
    }

    @Provides
    internal fun providesLocationScreenType(
        fragment: LocationFragment
    ): LocationScreenType {
        return fragment.extractScreenType()
    }

    @Provides
    internal fun providesNavigator(
        fragment: LocationFragment
    ): LocationNavigator {
        return fragment.createNavigatorAndBindToLifecycle(LocationNavigator::class)
    }
}
