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
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.migration.Migration
import com.kolibree.android.migration.MigrationProvider
import com.kolibree.android.migration.Migrations
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationUseCaseImplTest : BaseUnitTest() {

    private val migrationProvider: MigrationProvider = mock()
    private val accountDatastore: AccountDatastore = mock()
    private lateinit var migrationUseCase: MigrationUseCaseImpl

    private fun setUpMigrationUseCase(migrations: Migrations) {
        migrationUseCase = MigrationUseCaseImpl(
            migrations,
            migrationProvider,
            accountDatastore
        )
    }

    @Test
    fun `getMigrationsCompletable should complete when migrations has not been executed`() {
        val migration1 = TestMigration()
        val migration2 = TestMigration()
        val migration3 = TestMigration()

        setUpMigrationUseCase(listOf(migration1, migration2, migration3))

        whenever(migrationProvider.getStartNextMigrationAt()).thenReturn(Single.just(0))
        whenever(migrationProvider.setStartNextMigrationAt(any()))
            .thenReturn(Completable.complete())

        migrationUseCase.getMigrationsCompletable()
            .test()
            .assertComplete()

        assertTrue(migration1.isSubscribed)
        assertTrue(migration2.isSubscribed)
        assertTrue(migration3.isSubscribed)

        verify(migrationProvider).setStartNextMigrationAt(3)
    }

    @Test
    fun `getMigrationsCompletable should complete even when one migration fails`() {
        val migration1 = TestMigration()
        val migrationWithError =
            TestMigration(completable = Completable.error(NullPointerException("Error")))
        val migration3 = TestMigration()

        setUpMigrationUseCase(listOf(migration1, migrationWithError, migration3))

        whenever(migrationProvider.getStartNextMigrationAt()).thenReturn(Single.just(0))
        whenever(migrationProvider.setStartNextMigrationAt(any()))
            .thenReturn(Completable.complete())

        migrationUseCase.getMigrationsCompletable()
            .test()
            .assertComplete()

        assertTrue(migration1.isSubscribed)
        assertTrue(migrationWithError.isSubscribed)
        assertTrue(migration3.isSubscribed)

        verify(migrationProvider).setStartNextMigrationAt(3)
    }

    @Test
    fun `getMigrationsCompletable should not subscribes any migrations if the user is a fresh one`() {
        val migration1 = TestMigration()
        val migration2 = TestMigration()
        val migration3 = TestMigration()

        setUpMigrationUseCase(listOf(migration1, migration2, migration3))

        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.empty())
        whenever(migrationProvider.getStartNextMigrationAt())
            .thenReturn(Single.just(-1L))
        whenever(migrationProvider.setStartNextMigrationAt(any()))
            .thenReturn(Completable.complete())

        migrationUseCase.getMigrationsCompletable()
            .test()
            .assertComplete()

        assertFalse(migration1.isSubscribed)
        assertFalse(migration2.isSubscribed)
        assertFalse(migration3.isSubscribed)

        verify(migrationProvider).setStartNextMigrationAt(3)
    }

    @Test
    fun `getMigrationsCompletable should play the  migrations if the user is a legacy one and an account exists`() {
        val migration1 = TestMigration()
        val migration2 = TestMigration()
        val migration3 = TestMigration()

        setUpMigrationUseCase(listOf(migration1, migration2, migration3))

        whenever(accountDatastore.getAccountMaybe())
            .thenReturn(Maybe.just(mock()))
        whenever(migrationProvider.getStartNextMigrationAt())
            .thenReturn(Single.just(-1L))
        whenever(migrationProvider.setStartNextMigrationAt(any()))
            .thenReturn(Completable.complete())

        migrationUseCase.getMigrationsCompletable()
            .test()
            .assertComplete()

        assertTrue(migration1.isSubscribed)
        assertTrue(migration2.isSubscribed)
        assertTrue(migration3.isSubscribed)

        verify(migrationProvider).setStartNextMigrationAt(3)
    }

    @Test
    fun `getMigrationsCompletable should play in the correct order the migrations if there are 3 first animations and then 4 others`() {
        val migration1 = TestMigration()
        val migration2 = TestMigration()
        val migration3 = TestMigration()
        val migration4 = TestMigration()
        val migration5 = TestMigration()
        val migration6 = TestMigration()
        val migration7 = TestMigration()

        setUpMigrationUseCase(listOf(migration1, migration2, migration3))

        whenever(migrationProvider.getStartNextMigrationAt())
            .thenReturn(Single.just(0))
        whenever(migrationProvider.setStartNextMigrationAt(any()))
            .thenReturn(Completable.complete())

        migrationUseCase.getMigrationsCompletable()
            .test()
            .assertComplete()

        assertTrue(migration1.isSubscribed)
        assertTrue(migration2.isSubscribed)
        assertTrue(migration3.isSubscribed)

        verify(migrationProvider).setStartNextMigrationAt(3)
        whenever(migrationProvider.getStartNextMigrationAt())
            .thenReturn(Single.just(3))

        migration1.isSubscribed = false
        migration2.isSubscribed = false
        migration3.isSubscribed = false

        setUpMigrationUseCase(
            listOf(
                migration1,
                migration2,
                migration3,
                migration4,
                migration5,
                migration6,
                migration7
            )
        )

        migrationUseCase.getMigrationsCompletable()
            .test()
            .assertComplete()

        assertFalse(migration1.isSubscribed)
        assertFalse(migration2.isSubscribed)
        assertFalse(migration3.isSubscribed)

        assertTrue(migration4.isSubscribed)
        assertTrue(migration5.isSubscribed)
        assertTrue(migration6.isSubscribed)
        assertTrue(migration7.isSubscribed)

        verify(migrationProvider).setStartNextMigrationAt(7)
    }

    @Test
    fun `getMigrationsCompletable should play the migrations after the 3 first one`() {
        val migration1 = TestMigration()
        val migration2 = TestMigration()
        val migration3 = TestMigration()
        val migration4 = TestMigration()
        val migration5 = TestMigration()
        val migration6 = TestMigration()
        val migration7 = TestMigration()

        setUpMigrationUseCase(listOf(migration1, migration2, migration3))

        whenever(migrationProvider.getStartNextMigrationAt())
            .thenReturn(Single.just(0))
        whenever(migrationProvider.setStartNextMigrationAt(any()))
            .thenReturn(Completable.complete())
        whenever(migrationProvider.getStartNextMigrationAt())
            .thenReturn(Single.just(3))

        setUpMigrationUseCase(
            listOf(
                migration1,
                migration2,
                migration3,
                migration4,
                migration5,
                migration6,
                migration7
            )
        )

        migrationUseCase.getMigrationsCompletable()
            .test()
            .assertComplete()

        assertFalse(migration1.isSubscribed)
        assertFalse(migration2.isSubscribed)
        assertFalse(migration3.isSubscribed)

        assertTrue(migration4.isSubscribed)
        assertTrue(migration5.isSubscribed)
        assertTrue(migration6.isSubscribed)
        assertTrue(migration7.isSubscribed)

        verify(migrationProvider).setStartNextMigrationAt(7)
    }

    class TestMigration(val completable: Completable = Completable.complete()) : Migration {

        var isSubscribed = false

        override fun getMigrationCompletable(): Completable {
            return completable.doOnSubscribe {
                isSubscribed = true
            }
        }
    }
}
