/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.studies

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StudiesRepositoryImplInstrumentationTest : BaseInstrumentationTest() {

    private val repository = StudiesRepositoryImpl(context())

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun setUp() {
        super.setUp()

        repository.clear()
    }

    /*
    getStudy
     */

    @Test
    fun getStudy_returns_NO_STUDY_when_there_is_no_preference_value() {
        assertEquals(NO_STUDY, repository.getStudy(""))
    }

    @Test
    fun profileStudies_returns_preference_value_when_there_is_one() {
        val expectedValue = "Value"
        val mac = "mac"
        repository.addStudy(mac, expectedValue)

        assertEquals(expectedValue, repository.getStudy(mac))
    }

    /*
    addStudy
     */

    @Test
    fun addStudy_persists_mac_to_study_pairs() {
        val mac = "mac"
        val expectedStudy = "Study"

        repository.addStudy(mac, expectedStudy)

        assertEquals(expectedStudy, repository.getStudy(mac))
    }
}
