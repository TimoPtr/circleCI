/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.migration

import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.migration.Migration
import com.kolibree.android.migration.MigrationProvider
import com.kolibree.android.migration.MigrationProvider.Companion.NOT_EXISTING
import com.kolibree.android.migration.Migrations
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
interface MigrationUseCase {

    /**
     * @return the [Completable] of all pending migrations.
     * The [Completable] complete instantly if there are no waiting migrations.
     * If one [Migration] fails, the other migrations are executed and it complete as well.
     */
    fun getMigrationsCompletable(): Completable
}

@VisibleForApp
internal class MigrationUseCaseImpl @Inject constructor(
    private val migrations: Migrations,
    private val migrationProvider: MigrationProvider,
    private val accountDatastore: AccountDatastore
) : MigrationUseCase {

    override fun getMigrationsCompletable(): Completable {
        logMigrations("Migration list : ", migrations)

        return getExecutableMigrations()
            .flatMapCompletable(::getMigrationCompletable)
            .andThen(migrationProvider.setStartNextMigrationAt(migrations.size.toLong()))
    }

    private fun getMigrationCompletable(migration: Migration): Completable {
        return migration.getMigrationCompletable()
            .doOnError(Timber::e)
            .onErrorComplete()
    }

    /**
     * @return an [Observable] which dispatch the remaining [Migration] which need to be executed.
     * The migrations won't be executed if [MigrationProvider.getStartNextMigrationAt] returns `-1`
     * unless the user came from the legacy and have an account.
     */
    private fun getExecutableMigrations(): Observable<Migration> {
        return getStartMigration()
            .flatMapObservable { startingMigration ->
                if (startingMigration == NOT_EXISTING) {
                    Observable.empty()
                } else {
                    val executableMigration = migrations.drop(startingMigration.toInt())
                    logMigrations("Migrations which will be executed : ", executableMigration)

                    Observable.fromIterable(executableMigration)
                }
            }
    }

    private fun getStartMigration(): Single<Long> {
        return migrationProvider.getStartNextMigrationAt()
            .flatMap { startingMigration ->
                if (startingMigration == NOT_EXISTING) {
                    isLegacyUserSingle().flatMap { isLegacyUser ->
                        if (isLegacyUser) {
                            Single.just(0)
                        } else {
                            Single.just(startingMigration)
                        }
                    }
                } else {
                    Single.just(startingMigration)
                }
            }
    }

    /**
     * @return a [Single] containing `true` if the account is found, or `false otherwise`
     */
    private fun isLegacyUserSingle(): Single<Boolean> {
        return accountDatastore.getAccountMaybe()
            .map { true }
            .toSingle(false)
            .doOnSuccess { Timber.i("Is legacy user : $it") }
    }

    private fun logMigrations(message: String, list: Migrations) {
        Timber.i("$message ${list.joinToString { it.javaClass.simpleName }}")
    }
}
