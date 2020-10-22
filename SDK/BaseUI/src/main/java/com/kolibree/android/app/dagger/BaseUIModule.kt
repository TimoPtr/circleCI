/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.location.LocationActionChecker
import com.kolibree.android.app.location.LocationActionCheckerImpl
import com.kolibree.android.app.utils.AvatarDataStore
import com.kolibree.android.app.utils.AvatarUtils
import dagger.Binds
import dagger.Module

@Module
abstract class BaseUIModule {

    @Binds
    internal abstract fun bindsLocationForConnections(implementation: LocationActionCheckerImpl): LocationActionChecker

    @Binds
    internal abstract fun bindsAvatarUtils(impl: AvatarUtils): AvatarDataStore
}
