/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.integrityseal

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.extensions.edit
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.utils.forceLocale
import com.kolibree.statsoffline.persistence.StatsOfflineDao
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import java.util.Locale
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.ZoneId

internal class IntegritySealTest : BaseInstrumentationTest() {
    companion object {
        fun createIntegritySeal(context: Context, dao: StatsOfflineDao) = IntegritySeal(
            dao,
            IntegritySealDataStoreTest.createIntegritySealDataStore(context)
        )
    }

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val statsOfflineDao: StatsOfflineDao = mock()

    private val dataStore = IntegritySealDataStore(context())

    private val seal = IntegritySeal(statsOfflineDao, dataStore)

    override fun setUp() {
        super.setUp()

        dataStore.clear()

        createPreferencesContent()
    }

    override fun tearDown() {
        super.tearDown()

        dataStore.clear()
    }

    @Test
    fun whenSealIsCheckedForFirstTime_thenWeDontTouchDataStore() {
        seal.validateIntegrity()

        assertStoredDataNotWiped()
    }

    @Test
    fun whenFirstDayOfWeekDoesNotChangeBetweenInvocations_thenWeDontWipeDatabase() {
        val mondayAsFirstDayOfWeekLocale = Locale.FRANCE

        context().forceLocale(mondayAsFirstDayOfWeekLocale) {
            seal.validateIntegrity()

            seal.validateIntegrity()

            assertStoredDataNotWiped()
        }
    }

    @Test
    fun whenFirstDayOfWeekChangesBetweenInvocations_thenWeWipeDatabase() {
        val mondayAsFirstDayOfWeekLocale = Locale.FRANCE
        val sundayAsFirstDayOfWeekLocale = Locale.US

        context().forceLocale(mondayAsFirstDayOfWeekLocale) {
            seal.validateIntegrity()

            assertStoredDataNotWiped()
            context().forceLocale(sundayAsFirstDayOfWeekLocale) {
                seal.validateIntegrity()

                assertStoredDataWiped()
            }
        }
    }

    @Test
    fun whenTimezoneIsNotChangesBetweenInvocations_thenWeDontWipeDatabase() {
        val nyZoneId = ZoneId.of("America/New_York")

        TrustedClock.systemZone = nyZoneId

        seal.validateIntegrity()

        seal.validateIntegrity()

        assertStoredDataNotWiped()
    }

    @Test
    fun whenTimezoneChangesBetweenInvocations_thenWeDontWipeDatabase() {
        val nyZoneId = ZoneId.of("America/New_York")
        val londonZoneId = ZoneId.of("Europe/London")

        TrustedClock.systemZone = nyZoneId

        seal.validateIntegrity()

        assertStoredDataNotWiped()

        TrustedClock.systemZone = londonZoneId

        seal.validateIntegrity()

        assertStoredDataNotWiped()
    }

    /*
    Utils
     */

    private fun createPreferencesContent() {
        context().sealSharedPreferences().edit {
            putString(TEST_KEY, TEST_VALUE)
        }
    }

    private fun assertStoredDataNotWiped() {
        verifyNoMoreInteractions(statsOfflineDao)

        assertEquals(TEST_VALUE, context().sealSharedPreferences().getString(TEST_KEY, null))
    }

    private fun assertStoredDataWiped() {
        verify(statsOfflineDao).truncate()

        assertTrue(context().sealSharedPreferences().all.isEmpty())
    }
}

private const val TEST_KEY = "test_key"
private const val TEST_VALUE = "random"
