/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network

import com.google.common.base.Optional
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor

@Module
internal object NetworkInterceptorModule {
    @Provides
    fun providesEmptyNetworkInterceptor(): Optional<Interceptor> {
        return Optional.absent()
    }
}
