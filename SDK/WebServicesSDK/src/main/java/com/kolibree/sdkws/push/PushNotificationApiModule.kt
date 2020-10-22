/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.push

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
internal class PushNotificationApiModule {

    @Provides
    fun providePushNotificationApi(retrofit: Retrofit): PushNotificationApi {
        return retrofit.create(PushNotificationApi::class.java)
    }
}
