/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.push.di

import com.kolibree.android.app.push.PushNotificationUseCase
import com.kolibree.android.app.push.PushNotificationUseCaseImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class PushNotificationModule {

    @Binds
    abstract fun bindPushNotificationUseCase(impl: PushNotificationUseCaseImpl): PushNotificationUseCase
}
