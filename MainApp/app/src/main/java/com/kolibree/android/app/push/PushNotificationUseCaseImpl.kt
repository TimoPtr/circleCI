/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.push

import androidx.annotation.VisibleForTesting
import com.google.firebase.iid.FirebaseInstanceId
import com.kolibree.android.network.toParsedResponseCompletable
import com.kolibree.sdkws.KolibreeUtils
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.push.PushNotificationApi
import com.kolibree.sdkws.push.PushNotificationTokenRequestBody
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import javax.inject.Inject
import timber.log.Timber

internal class PushNotificationUseCaseImpl @Inject constructor(
    private val pushNotificationApi: PushNotificationApi,
    private val kolibreeConnector: InternalKolibreeConnector,
    private val kolibreeUtils: KolibreeUtils
) : PushNotificationUseCase {

    override fun uploadNewTokenForCurrentAccount(token: String): Completable =
        uploadTokenToServer(token)

    override fun forceUploadCurrentTokenForCurrentAccount(): Completable =
        retrieveCurrentToken()
            .flatMapCompletable { token -> uploadTokenToServer(token) }

    @VisibleForTesting
    fun retrieveCurrentToken(): Single<String> {
        val currentTokenSubject = AsyncSubject.create<String>()
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    currentTokenSubject.onError(IllegalStateException("Firebase token retrieval failed"))
                    return@addOnCompleteListener
                }
                task.result?.token?.let {
                    currentTokenSubject.onNext(it)
                    currentTokenSubject.onComplete()
                } ?: currentTokenSubject.onError(IllegalStateException("Firebase token is null"))
            }
        return currentTokenSubject.lastOrError()
    }

    private fun uploadTokenToServer(token: String): Completable {
        val deviceId = kolibreeUtils.deviceId
        val accountId = kolibreeConnector.currentAccount()?.id
            ?: return Completable.error(
                IllegalStateException("Account is null, cannot proceed with Firebase token upload")
            )

        return pushNotificationApi.updatePushNotificationToken(
            accountId,
            PushNotificationTokenRequestBody(token, deviceId)
        ).toParsedResponseCompletable()
            .subscribeOn(Schedulers.io())
            .doOnComplete {
                Timber.d("Firebase token $token successfully uploaded for account $accountId.")
                Timber.d("Device ID: $deviceId")
            }
    }
}
