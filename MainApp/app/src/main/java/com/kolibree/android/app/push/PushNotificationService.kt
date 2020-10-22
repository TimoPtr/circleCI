/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.kolibree.android.app.BaseKolibreeApplication
import com.kolibree.android.extensions.addSafely
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject
import timber.log.Timber

class PushNotificationService : FirebaseMessagingService() {

    private val disposables = CompositeDisposable()

    @Inject
    internal lateinit var pushNotificationUseCase: PushNotificationUseCase

    override fun onCreate() {
        super.onCreate()
        BaseKolibreeApplication.appComponent.inject(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        disposables.addSafely(
            pushNotificationUseCase.uploadNewTokenForCurrentAccount(token)
                .subscribe({}, Timber::w)
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let { showPushNotification(this, it, message.data) }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}
