/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.disconnection

import dagger.Binds
import dagger.Module

@Module
abstract class LostConnectionHandlerModule {

    @Binds
    internal abstract fun bindsLostConnectionHandler(
        implementation: LostConnectionHandlerImpl
    ): LostConnectionHandler
}
