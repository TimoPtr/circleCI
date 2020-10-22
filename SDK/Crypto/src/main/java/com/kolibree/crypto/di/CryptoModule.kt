/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto.di

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.crypto.SecurityKeeper
import com.kolibree.crypto.SecurityKeeperImpl
import dagger.Binds
import dagger.Module

@Module
abstract class CryptoModule {

    @Binds
    @AppScope
    internal abstract fun bindSecurityKeeper(impl: SecurityKeeperImpl): SecurityKeeper
}
