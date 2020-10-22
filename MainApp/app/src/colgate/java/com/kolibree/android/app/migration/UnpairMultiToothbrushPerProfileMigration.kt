/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.migration

import com.kolibree.account.utils.ToothbrushForgetter
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.migration.Migration
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.Completable
import io.reactivex.Maybe
import javax.inject.Inject
import timber.log.Timber

/**
 * This Migration provides a way for unpairing the Toothbrushes
 * when the user came from an old version of Colgate Connect
 *
 * It follows these points :
 * - If there’s a shared toothbrush, forget all shared toothbrushes
 * - If there’s 1 toothbrush per profile, keep them
 * - If a profile has more than one toothbrushes, the toothbrushes are forgotten
 *
 * Reference task can be found [here](https://kolibree.atlassian.net/browse/KLTB002-12748)
 */
class UnpairMultiToothbrushPerProfileMigration @Inject constructor(
    private val appConfiguration: AppConfiguration,
    private val toothbrushRepository: ToothbrushRepository,
    private val accountDatastore: AccountDatastore,
    private val toothbrushForgetter: ToothbrushForgetter
) : Migration {

    override fun getMigrationCompletable(): Completable {

        // If Multi-Toothbrush per profile is enabled, migration is ignored
        if (appConfiguration.isMultiToothbrushesPerProfileEnabled) {
            return Completable.complete()
        }

        return accountDatastore.getAccountMaybe()
            .flatMapCompletable { account ->
                migrateSharedToothbrushes(account)
                    .andThen(migrateMultiToothbrushes(account))
            }
    }

    private fun migrateSharedToothbrushes(account: AccountInternal): Completable {
        return getAccountToothbrushesMaybe(account)
            .flatMapCompletable { toothbrushes ->
                unpairSharedToothbrushes(toothbrushes)
            }
    }

    private fun migrateMultiToothbrushes(account: AccountInternal): Completable {
        return getAccountToothbrushesMaybe(account)
            .flatMapCompletable { toothbrushes ->
                unpairMultiToothbrushes(toothbrushes)
            }
    }

    private fun getAccountToothbrushesMaybe(account: AccountInternal):
        Maybe<List<AccountToothbrush>> {
        return Maybe.fromCallable {
            toothbrushRepository.getAccountToothbrushes(account.id)
        }
    }

    /**
     * Unpair the shared toothbrushes if there are some.
     */
    private fun unpairSharedToothbrushes(toothbrushes: List<AccountToothbrush>): Completable {

        val sharedToothbrushes = toothbrushes.filter { it.isSharedToothbrush }

        return when {

            // If there are shared toothbrushes to the account, we unpair all of them
            sharedToothbrushes.isNotEmpty() ->
                toothbrushForgetter.eraseToothbrushes(sharedToothbrushes)
                    .doOnComplete { Timber.i("Shared toothbrushes has been forgotten") }

            // Else no migration needs to be done regarding the shared toothbrushes
            else -> Completable.complete()
        }
            .doOnError(Timber::e)
            .onErrorComplete()
    }

    /**
     * Get a [Completable] which forget the toothbrushes for each profile
     * which have more than one connected toothbrushes.
     * These [Completable] are then merged into one Completable and won't block the execution of
     * other [Completable] if one fails.
     */
    private fun unpairMultiToothbrushes(toothbrushes: List<AccountToothbrush>): Completable {
        val multiToothbrushes = toothbrushes.groupBy { it.profileId }
            .filter { it.value.size > 1 }.values
            .flatten()

        return toothbrushForgetter.eraseToothbrushes(multiToothbrushes)
    }
}
