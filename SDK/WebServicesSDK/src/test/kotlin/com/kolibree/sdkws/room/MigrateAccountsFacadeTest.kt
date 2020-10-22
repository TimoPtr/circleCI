package com.kolibree.sdkws.room

import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.dagger.LazyContainer
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase
import org.junit.Test
import org.mockito.Mock

class MigrateAccountsFacadeTest : BaseUnitTest() {
    @Mock
    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var brushingsRepository: BrushingsRepository

    private lateinit var migrateAccountsFacade: MigrateAccountsFacade

    override fun setup() {
        super.setup()

        migrateAccountsFacade = spy(MigrateAccountsFacade(sharedPreferences, LazyContainer.create(brushingsRepository)))
    }

    /*
    MAYBE MIGRATE ACCOUNTS
     */

    @Test
    fun maybeMigrateAccounts_shouldMigrateFalse_neverInvokesForceMigration() {
        doReturn(false).whenever(migrateAccountsFacade).shouldMigrateAccounts()

        migrateAccountsFacade.maybeMigrateAccounts()

        verify(migrateAccountsFacade, never()).forceAccountMigration()
    }

    @Test
    fun maybeMigrateAccounts_shouldMigrateTrue_invokesForceMigration() {
        doReturn(true).whenever(migrateAccountsFacade).shouldMigrateAccounts()

        doNothing().whenever(migrateAccountsFacade).forceAccountMigration()

        migrateAccountsFacade.maybeMigrateAccounts()

        verify(migrateAccountsFacade).forceAccountMigration()
    }

    /*
    SHOULD MIGRATE ACCOUNTS
     */

    @Test
    fun shouldMigrateAccounts_instructsPreferencesToGetFalseIfNotStored() {
        migrateAccountsFacade.shouldMigrateAccounts()

        verify(sharedPreferences).getBoolean(MigrateAccountsFacade.ACCOUNTS_MIGRATED_KEY, false)
    }

    @Test
    fun shouldMigrateAccounts_negatesStoredValue() {
        whenever(sharedPreferences.getBoolean(eq(MigrateAccountsFacade.ACCOUNTS_MIGRATED_KEY), any())).thenReturn(false)

        TestCase.assertTrue(migrateAccountsFacade.shouldMigrateAccounts())

        whenever(sharedPreferences.getBoolean(eq(MigrateAccountsFacade.ACCOUNTS_MIGRATED_KEY), any())).thenReturn(true)

        TestCase.assertFalse(migrateAccountsFacade.shouldMigrateAccounts())
    }
}
