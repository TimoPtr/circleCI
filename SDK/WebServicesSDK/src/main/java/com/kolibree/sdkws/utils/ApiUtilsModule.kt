/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.utils

import dagger.Binds
import dagger.Module

/** Created by miguelaragues on 16/1/18.  */
@Module
abstract class ApiUtilsModule {
    @Binds
    internal abstract fun providesProfileUtils(utils: ProfileUtilsImpl): ProfileUtils

    @Binds
    internal abstract fun bindsApiSDKUtils(apiSDKUtilsImpl: ApiSDKUtilsImpl): ApiSDKUtils
}
