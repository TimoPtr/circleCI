/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.tbreplace

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.threeten.bp.LocalDate

class HeadReplacementProviderImplTest : BaseUnitTest() {

    private lateinit var headReplaceProvider: HeadReplacementProviderImpl

    private val context: Context = mock()
    private val preferences = mock<SharedPreferences>()

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)
        headReplaceProvider = HeadReplacementProviderImpl(context = context)
    }

    @Test
    fun `getNeverShowAgainDate should returns a well formed date`() {
        val mac = "mac-cam"
        val key = "$KEY_NEVER_SHOW_AGAIN$mac"
        val orwellDate = LocalDate.of(1984, 1, 1)

        whenever(preferences.getLong(key, 0)).thenReturn(orwellDate.toEpochDay())

        headReplaceProvider.getWarningHiddenDate(mac)
            .test()
            .assertValue(orwellDate)
    }

    @Test
    fun `setNeverShowAgainDate should edit the preferences and put the LocalDate time`() {
        val mac = "dave-vs-hal"
        val key = "$KEY_NEVER_SHOW_AGAIN$mac"
        val editor = mock<SharedPreferences.Editor>()
        val odysseyDate = LocalDate.of(2001, 1, 1)

        whenever(preferences.edit()).thenReturn(editor)

        headReplaceProvider.setWarningHiddenDate(mac, odysseyDate).test()

        verify(editor).putLong(key, odysseyDate.toEpochDay())
    }
}

private const val KEY_NEVER_SHOW_AGAIN = "hide_head_replacement_warning_"
