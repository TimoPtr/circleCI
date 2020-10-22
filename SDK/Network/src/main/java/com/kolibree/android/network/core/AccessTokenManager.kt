package com.kolibree.android.network.core

import androidx.annotation.Keep
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.accountinternal.internal.RefreshTokenProvider
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Keep
interface AccessTokenManager {
    fun getAccessToken(): String?

    fun updateTokens(refreshTokenProvider: RefreshTokenProvider): Completable

    fun notifyUnableToRefreshToken()

    val refreshTokenFailedObservable: Observable<Boolean>
}

internal class AccessTokenManagerImpl
@Inject constructor(
    private val accountDatastore: AccountDatastore
) : AccessTokenManager {
    private val refreshTokenFailedRelay = PublishRelay.create<Boolean>()

    override val refreshTokenFailedObservable: Observable<Boolean> by lazy {
        refreshTokenFailedRelay.share()
    }

    override fun notifyUnableToRefreshToken() {
        refreshTokenFailedRelay.accept(true)
    }

    override fun getAccessToken(): String? {
        return accountDatastore.getAccountMaybe()
            .subscribeOn(Schedulers.io())
            .map { it.accessToken }
            .blockingGet()
    }

    override fun updateTokens(refreshTokenProvider: RefreshTokenProvider): Completable {
        return accountDatastore.getAccountMaybe()
            .flatMapCompletable {
                it.updateTokensWith(refreshTokenProvider)

                Completable.fromAction { accountDatastore.updateTokens(it) }
            }
    }
}
