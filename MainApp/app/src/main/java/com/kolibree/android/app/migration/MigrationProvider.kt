/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.migration

import android.content.Context
import com.kolibree.android.extensions.edit
import com.kolibree.android.migration.MigrationProvider.Companion.NOT_EXISTING
import com.kolibree.android.persistence.BasePreferencesImpl
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import timber.log.Timber

/**
 * Provide the persistence for [MigrationUseCase] to determine if a Migration has already been
 * executed.
 */
internal interface MigrationProvider {
    fun getStartNextMigrationAt(): Single<Long>
    fun setStartNextMigrationAt(migrationDoneCount: Long): Completable

    companion object {
        const val NOT_EXISTING = -1L
    }
}

internal class MigrationProviderImpl @Inject constructor(
    context: Context
) : BasePreferencesImpl(context), MigrationProvider {

    private val preferences = prefs

    override fun getStartNextMigrationAt(): Single<Long> {
        return Single.fromCallable {
            preferences.getLong(KEY_MIGRATION_DONE_COUNT, NOT_EXISTING)
        }.doOnSuccess { Timber.i("Next Migration to be played is number : $it") }
    }

    override fun setStartNextMigrationAt(nextMigration: Long): Completable {
        return Completable.fromAction {
            preferences.edit {
                this.putLong(KEY_MIGRATION_DONE_COUNT, nextMigration)
            }
        }.doOnComplete { Timber.i("Next Migration has been set to number $nextMigration") }
    }

    companion object {
        const val KEY_MIGRATION_DONE_COUNT = "start_next_migration_at"
    }
}
