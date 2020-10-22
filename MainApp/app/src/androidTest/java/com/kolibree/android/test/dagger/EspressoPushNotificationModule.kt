/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.dagger

import com.kolibree.android.app.push.PushNotificationUseCase
import dagger.Module
import dagger.Provides
import io.reactivex.Completable

@Module
class EspressoPushNotificationModule {

    @Provides
    fun providePushNotificationUseCase(): PushNotificationUseCase =
        object : PushNotificationUseCase {

            override fun forceUploadCurrentTokenForCurrentAccount() = Completable.complete()

            override fun uploadNewTokenForCurrentAccount(token: String) = Completable.complete()
        }
}
