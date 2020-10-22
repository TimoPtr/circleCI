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
import android.content.Context.MODE_PRIVATE
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.extensions.edit
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.threeten.bp.DayOfWeek

internal class IntegritySealDataStoreTest : BaseInstrumentationTest() {
    companion object {
        fun createIntegritySealDataStore(context: Context) = IntegritySealDataStore(context)
    }

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val dataStore = createIntegritySealDataStore(context())

    override fun setUp() {
        super.setUp()

        dataStore.clear()
    }

    override fun tearDown() {
        super.tearDown()

        dataStore.clear()
    }

    /*
    storedFirstDayOfWeek
     */
    @Test
    fun whenStoredFirstDayOfWeekIsInvokedOnEmptyDataStore_thenNullIsReturned() {
        assertNull(dataStore.storedFirstDayOfWeek())
    }

    @Test
    fun whenStoredFirstDayOfWeekValueIsWrong_thenNullIsReturned() {
        context().sealSharedPreferences().edit {
            putInt(PREVIOUS_FIRST_DAY_OF_WEEK, 70)
        }

        assertNull(dataStore.storedFirstDayOfWeek())
    }

    /*
    storeFirstDayOfWeek
     */
    @Test
    fun storeFirstDayOfWeek_storesDayOfWeek() {
        val dayOfWeek = DayOfWeek.SATURDAY

        assertNull(dataStore.storedFirstDayOfWeek())

        dataStore.storeFirstDayOfWeek(dayOfWeek)

        assertEquals(dayOfWeek, dataStore.storedFirstDayOfWeek())
    }

    @Test
    fun whenStoreFirstDayOfWeekIsInvokedMultipleTimes_thenWeStoreTheLastValue() {
        val dayOfWeek = DayOfWeek.SATURDAY
        val expectedDayOfWeek = DayOfWeek.MONDAY

        assertNull(dataStore.storedFirstDayOfWeek())

        dataStore.storeFirstDayOfWeek(dayOfWeek)

        assertNotNull(dataStore.storedFirstDayOfWeek())

        dataStore.storeFirstDayOfWeek(expectedDayOfWeek)

        assertEquals(expectedDayOfWeek, dataStore.storedFirstDayOfWeek())
    }

    /*
    truncate
     */

    @Test
    fun truncate_clearsAllPreferences() {
        dataStore.truncate().test().assertComplete()
    }
}

internal fun Context.sealSharedPreferences() = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
