/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.pirate.crypto

import dagger.Binds
import dagger.Module

@Module
abstract class PirateCryptoModule {
    @Binds
    internal abstract fun providePirateLanesProvider(impl: PirateLanesProviderImpl): PirateLanesProvider
}
