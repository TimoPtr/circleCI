/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.amazondash.R
import com.kolibree.android.amazondash.data.model.AmazonDashException
import com.kolibree.android.amazondash.data.model.AmazonDashSendTokenRequest
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.network.toParsedResponseCompletable
import com.kolibree.android.synchronizator.Synchronizator
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

internal interface AmazonDashSendTokenUseCase {

    fun sendToken(token: String): Completable
}

internal class AmazonDashSendTokenUseCaseImpl @Inject constructor(
    private val accountDataStore: AccountDatastore,
    private val amazonDashApi: AmazonDashApi,
    private val synchronizator: Synchronizator
) : AmazonDashSendTokenUseCase {

    override fun sendToken(token: String): Completable {
        return accountDataStore
            .getAccountMaybe()
            .errorIfMissing()
            .sendToken(token)
            .synchronizeIfSuccess()
            .doOnSubscribe { Timber.d("Uploading token") }
            .doOnComplete { Timber.d("Token uploaded") }
    }

    private fun Maybe<AccountInternal>.errorIfMissing(): Single<AccountInternal> {
        return switchIfEmpty(Single.error(AmazonDashException(R.string.amazon_dash_connect_error_unknown)))
    }

    private fun Single<AccountInternal>.sendToken(token: String): Completable {
        return flatMapCompletable { account ->
            amazonDashApi
                .sendToken(account.id, AmazonDashSendTokenRequest(token))
                .toParsedResponseCompletable()
        }
    }

    private fun Completable.synchronizeIfSuccess(): Completable {
        return andThen(synchronizator.delaySynchronizeCompletable())
    }
}
