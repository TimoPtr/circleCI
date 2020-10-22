/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.feedback

import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test

class FirstRunDateImplTest : BaseUnitTest() {

    private val preferences = mock<SharedPreferences>()
    private val editor = mock<SharedPreferences.Editor>()

    private lateinit var implementation: FirstLoginDateImpl

    override fun setup() {
        super.setup()

        whenever(preferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)

        implementation = spy(FirstLoginDateImpl(preferences))
    }

    @Test
    fun `firstRunDate returns date from preferences if presents`() {
        whenever(preferences.contains(FIRST_RAN_DATE_KEY)).thenReturn(true)
        doReturn(TrustedClock.getNowZonedDateTime()).whenever(implementation).readDate()

        implementation.firstRunDate()

        verify(implementation).readDate()
    }

    @Test
    fun `firstRunDate invokes update if date is not present in preferences`() {
        whenever(preferences.contains(FIRST_RAN_DATE_KEY)).thenReturn(false)
        doReturn(TrustedClock.getNowZonedDateTime()).whenever(implementation).readDate()
        doNothing().whenever(implementation).update()

        implementation.firstRunDate()

        verify(implementation).update()
    }

    @Test
    fun `update checks if date is already added to preferences`() {
        whenever(preferences.contains(FIRST_RAN_DATE_KEY)).thenReturn(true)

        implementation.update()

        verify(implementation).containsDate()
    }

    @Test
    fun `update adds date to preferences if it is not already there`() {
        whenever(preferences.contains(FIRST_RAN_DATE_KEY)).thenReturn(false)

        implementation.update()

        verify(editor).putString(eq(FIRST_RAN_DATE_KEY), any())
    }

    @Test
    fun `readDate reads date from preferences`() {
        val date = TrustedClock.getNowZonedDateTime()
        val textualDate = date.toString()
        whenever(preferences.getString(FIRST_RAN_DATE_KEY, null)).thenReturn(textualDate)

        assertEquals(date, implementation.readDate())
    }

    @Test
    fun `containsDate check if preferences contains key FIRST_RAN_DATE_KEY`() {
        implementation.containsDate()

        verify(preferences).contains(FIRST_RAN_DATE_KEY)
    }
}
