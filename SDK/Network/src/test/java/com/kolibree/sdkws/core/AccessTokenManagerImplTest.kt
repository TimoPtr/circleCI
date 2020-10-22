package com.kolibree.sdkws.core

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.internal.RefreshTokenProvider
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.network.core.AccessTokenManagerImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccessTokenManagerImplTest {
    private val accountDatastore: AccountDatastore = mock()
    private val accessTokenManager = AccessTokenManagerImpl(accountDatastore)

    /*
    GET ACCESS TOKEN
     */
    @Test
    fun getAccessToken_accountDatastoreCurrentAccountEmpty_returnsNull() {
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty())

        assertNull(accessTokenManager.getAccessToken())
    }

    @Test
    fun getAccessToken_accountDatastoreCurrentAccountNotNull_returnsValueFromCurrentAccount() {
        val account = mock<AccountInternal>()
        val expectedToken = "dasdv"
        whenever(account.accessToken).thenReturn(expectedToken)
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account))

        assertEquals(expectedToken, accessTokenManager.getAccessToken())
    }

    /*
    UPDATE TOKENS
     */
    @Test
    fun updateTokens_maybeAccountEmpty_neverTouchesAccountDataStore() {
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty())

        val expectedRefreshTokenProvider = mock<RefreshTokenProvider>()

        accessTokenManager.updateTokens(expectedRefreshTokenProvider).test()

        verify(accountDatastore, never()).updateTokens(any())
    }

    @Test
    fun updateTokens_maybeHasAccount_invokesAccountDataStoreWithUpdatedTokens() {
        val account = mock<AccountInternal>()

        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account))
        val expectedRefreshTokenProvider = mock<RefreshTokenProvider>()

        accessTokenManager.updateTokens(expectedRefreshTokenProvider).test()

        verify(account).updateTokensWith(expectedRefreshTokenProvider)

        verify(accountDatastore).updateTokens(account)
    }

    /*
    refreshTokenFailedObservable
     */
    @Test
    fun `refreshTokenFailedObservable subscribers receive event after notifyUnableToRefreshToken`() {
        val observer = accessTokenManager.refreshTokenFailedObservable.test().assertEmpty()

        accessTokenManager.notifyUnableToRefreshToken()

        observer.assertValue(true)
    }

    @Test
    fun `refreshTokenFailedObservable new subscribers don't receive event after notifyUnableToRefreshToken`() {
        accessTokenManager.notifyUnableToRefreshToken()

        accessTokenManager.refreshTokenFailedObservable.test().assertEmpty()
    }
}
