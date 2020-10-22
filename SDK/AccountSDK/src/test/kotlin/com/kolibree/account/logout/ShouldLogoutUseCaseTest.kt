/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.logout

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.core.AccessTokenManager
import com.kolibree.android.network.errorhandler.RemoteAccountDoesNotExistDetector
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.Test

class ShouldLogoutUseCaseTest : BaseUnitTest() {
    private val accessTokenManager: AccessTokenManager = mock()
    private val accountDoesNotExistDetector: RemoteAccountDoesNotExistDetector = mock()

    private lateinit var shouldLogoutUseCase: ShouldLogoutUseCase

    private val refreshTokenFailedSubject = PublishSubject.create<Boolean>()
    private val accountDoesNotExistSubject = PublishSubject.create<Boolean>()

    override fun setup() {
        super.setup()

        whenever(accessTokenManager.refreshTokenFailedObservable).thenReturn(
            refreshTokenFailedSubject
        )

        whenever(accountDoesNotExistDetector.accountDoesNotExistStream).thenReturn(
            accountDoesNotExistSubject
        )

        shouldLogoutUseCase =
            ShouldLogoutUseCaseImpl(
                accessTokenManager,
                accountDoesNotExistDetector
            )
    }

    /*
    Refresh token failed
     */

    @Test
    fun `shouldLogOut emits RefreshTokenFailed and does not complete when accessTokenManager emits true for refresh token failed`() {
        val observer = shouldLogoutUseCase.shouldLogoutStream.test()

        observer.assertEmpty()

        refreshTokenFailedSubject.onNext(true)

        observer.assertValue(com.kolibree.account.logout.RefreshTokenFailed).assertNotComplete()
    }

    @Test
    fun `shouldLogOut doesn't emit and does not complete when accessTokenManager emits false for refresh token failed`() {
        val observer = shouldLogoutUseCase.shouldLogoutStream.test()

        observer.assertEmpty()

        refreshTokenFailedSubject.onNext(false)

        observer.assertEmpty().assertNotComplete()
    }

    /*
    Account does not exist
     */

    @Test
    fun `shouldLogOut emits AccountDoesNotExist and does not complete when accountDoesNotExistDetector emits item`() {
        val observer = shouldLogoutUseCase.shouldLogoutStream.test()

        observer.assertEmpty()

        accountDoesNotExistSubject.onNext(true)

        observer.assertValue(com.kolibree.account.logout.AccountDoesNotExist).assertNotComplete()
    }

    @Test
    fun `shouldLogOut doesn't emit and does not complete when accountDoesNotExistDetector emits false for refresh token failed`() {
        val observer = shouldLogoutUseCase.shouldLogoutStream.test()

        observer.assertEmpty()

        refreshTokenFailedSubject.onNext(false)

        observer.assertEmpty().assertNotComplete()
    }
}
