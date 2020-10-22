/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.pairing

import com.kolibree.android.app.dagger.scopes.ActivityScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class PairingBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [PairingModule::class])
    internal abstract fun bindPairingActivity(): PairingActivity
}
