package com.kolibree.sdkws.room

import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import javax.inject.Inject

internal class MigrateAccountsFacade @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val brushingsRepository: dagger.Lazy<BrushingsRepository>
) {

    @VisibleForTesting
    companion object {
        const val ACCOUNTS_MIGRATED_KEY = "accounts_migrated_key"
    }

    /**
     * When moving from database 18 to 19, we moved Account and Profiles to AccountInternal module
     *
     * We were checking if an account existed in the AccountDataStore provided by AccountInternal module, but user's
     * Account and Profile still lived in the WSSDK database.
     *
     * Since the account check was the first step of the app and it was to a different DB, the migration from 18 to 19
     * wasn't triggered, so we returned null for Account.
     *
     * We need a way to force the migration to happen before any check on the new database. By querying for number of
     * brushings, we force the migration from 18 to 19.
     *
     * We delegate the storeAccountsMigrated invocation to the actual migration
     */
    fun maybeMigrateAccounts() {
        if (shouldMigrateAccounts())
            forceAccountMigration()
    }

    @VisibleForTesting
    fun shouldMigrateAccounts() = !sharedPreferences.getBoolean(ACCOUNTS_MIGRATED_KEY, false)

    fun storeAccountsMigrated() {
        sharedPreferences.edit().putBoolean(ACCOUNTS_MIGRATED_KEY, true).apply()
    }

    @VisibleForTesting
    fun forceAccountMigration() {
        brushingsRepository.get().countBrushings(0).blockingGet()
    }
}
