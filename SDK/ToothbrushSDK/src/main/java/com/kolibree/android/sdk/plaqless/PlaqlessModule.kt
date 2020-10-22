/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.plaqless

import com.kolibree.android.sdk.dagger.ToothbrushSDKScope
import dagger.Binds
import dagger.Module

@Module
internal abstract class PlaqlessModule {
    @Binds
    @ToothbrushSDKScope
    abstract fun bindsDspAwaker(impl: DspAwakerImpl): DspAwaker
}
