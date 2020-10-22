/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.logout

import com.kolibree.android.network.core.AccessTokenManager
import com.kolibree.android.network.errorhandler.RemoteAccountDoesNotExistDetector
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Detects when SDK clients should forcefully log out users
 */
internal interface ShouldLogoutUseCase {
    val shouldLogoutStream: Observable<ForceLogoutReason>
}

/**
 * Listens to [AccessTokenManager] and [RemoteAccountDoesNotExistDetector] for irrecoverable errors
 * for which the only solution is for the user to log in again
 */
internal class ShouldLogoutUseCaseImpl @Inject constructor(
    accessTokenManager: AccessTokenManager,
    accountDoesNotExistDetector: RemoteAccountDoesNotExistDetector
) : ShouldLogoutUseCase {
    private val refreshTokenFailedStream: Observable<ForceLogoutReason> =
        accessTokenManager.refreshTokenFailedObservable
            .filter { it }
            .map { RefreshTokenFailed }

    private val accountDoesNotExistStream: Observable<ForceLogoutReason> =
        accountDoesNotExistDetector.accountDoesNotExistStream
            .filter { it }
            .map { AccountDoesNotExist }

    override val shouldLogoutStream: Observable<ForceLogoutReason> =
        Observable.merge(refreshTokenFailedStream, accountDoesNotExistStream)
}
