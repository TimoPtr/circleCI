/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.utils

import androidx.annotation.Keep
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.UnknownToothbrushException
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Forget toothbrush assistant
 *
 * In our SDK domain, forget includes the following tasks
 * - Disconnect from toothbrush
 * - Don't attempt to connect to the toothbrush in the future
 */
@Keep
interface ToothbrushForgetter {
    /**
     * Forgets all toothbrushes owned by [profileId]
     *
     * It does *NOT* touch shared toothbrushes
     *
     * @return Completable that will forget all toothbrushes. Once that completes successfully, it
     * will run all [ToothbrushForgottenHook]
     */
    fun forgetOwnedByProfile(profileId: Long): Completable

    /**
     * Forgets toothbrush with [mac]
     *
     * If there's no toothbrush associated to [mac], the returned Completable will emit
     * [UnknownToothbrushException]
     *
     * @return Completable that will forget toothbrush associated to mac. Once that completes
     * successfully, it will run all [ToothbrushForgottenHook]
     */
    fun forgetToothbrush(mac: String): Completable

    /**
     * Behave as [forgetToothbrush] but does not initiate a connection to the KLTBConnectionPool to
     * write on the toothbrush.
     *
     * This means that the unlinked toothbrush won't have the information in its hardware
     * that it has been unpaired.
     *
     * @return Completable that will forget toothbrush associated to mac ONLY in the local database.
     * Once that completes successfully, it will run all [ToothbrushForgottenHook]
     */
    fun eraseToothbrushes(toothbrushes: List<AccountToothbrush>): Completable
}

/**
 * Describes a Hook that will be executed *after* a toothbrush has been successfully forgotten
 */
@Keep
interface ToothbrushForgottenHook {

    fun onForgottenCompletable(toothbrush: ForgottenToothbrush): Completable
}

@Keep
data class ForgottenToothbrush(
    val mac: String,
    val serial: String,
    val accountId: Long,
    val profileId: Long
)

internal class ToothbrushForgetterImpl @Inject constructor(
    private val serviceProvider: ServiceProvider,
    private val toothbrushRepository: ToothbrushRepository,
    private val forgottenHooks: Set<@JvmSuppressWildcards ToothbrushForgottenHook>
) : ToothbrushForgetter {
    override fun forgetOwnedByProfile(profileId: Long): Completable {
        return Single.zip(
            serviceProvider.connectOnce(),
            profileToothbrushes(profileId),
            { service, toothbrushes ->
                toothbrushes.map { accountToothbrush ->
                    service.forgetAndRunHooks(accountToothbrush)
                }
            }
        )
            .flatMapCompletable { forgetCompletables ->
                Completable.mergeDelayError(forgetCompletables)
            }
    }

    override fun eraseToothbrushes(toothbrushes: List<AccountToothbrush>): Completable {
        return Observable.fromIterable(toothbrushes)
            .flatMapCompletable {
                toothbrushRepository.removeAccountToothbrush(it)
                    .andThen(getForgetHooksCompletable(it))
            }
    }

    override fun forgetToothbrush(mac: String): Completable {
        return Single.zip(
            serviceProvider.connectOnce(),
            fetchAccountToothbrush(mac),
            { service, toothbrush ->
                service.forgetAndRunHooks(toothbrush)
            }
        )
            .flatMapCompletable { forgetCompletable -> forgetCompletable }
    }

    private fun fetchAccountToothbrush(mac: String): Single<AccountToothbrush> {
        return toothbrushRepository.getAccountToothbrush(mac)
            .toSingle()
            .onErrorResumeNext { throwable ->
                if (throwable is NoSuchElementException) {
                    Single.error(UnknownToothbrushException(mac))
                } else {
                    Single.error(throwable)
                }
            }
    }

    private fun profileToothbrushes(profileId: Long): Single<List<AccountToothbrush>> =
        toothbrushRepository.listAllWithProfileIdOnceAndStream(profileId).take(1).singleOrError()

    private fun KolibreeService.forgetAndRunHooks(toothbrush: AccountToothbrush): Completable {
        return forgetCompletable(toothbrush.mac)
            .andThen(getForgetHooksCompletable(toothbrush))
    }

    private fun getForgetHooksCompletable(toothbrush: AccountToothbrush): Completable {
        return Completable.mergeDelayError(
            forgottenHooks.map { it.onForgottenCompletable(toothbrush.toForgottenToothbrush()) }
        )
    }

    private fun AccountToothbrush.toForgottenToothbrush(): ForgottenToothbrush {
        return ForgottenToothbrush(
            mac = mac,
            serial = serial,
            accountId = accountId,
            profileId = profileId
        )
    }
}
