/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.data

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

class BrushingTipsProviderImplTest : BaseUnitTest() {

    private val preferences: SharedPreferences = mock()

    private lateinit var brushingTipsProvider: BrushingTipsProviderImpl

    @Before
    fun setUp() {
        val context: Context = mock()
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)

        brushingTipsProvider = BrushingTipsProviderImpl(context)
    }

    @Test
    fun `isScreenDisplayable query the preferences and returns true`() {
        val expectedValue = true
        whenever(preferences.getBoolean(KEY_BRUSHING_TIPS_DISPLAYABLE, true))
            .thenReturn(expectedValue)

        brushingTipsProvider.isScreenDisplayable()
            .test()
            .assertValue(expectedValue)
    }

    @Test
    fun `isScreenDisplayable query the preferences and returns false`() {
        val expectedValue = false
        whenever(preferences.getBoolean(KEY_BRUSHING_TIPS_DISPLAYABLE, true))
            .thenReturn(expectedValue)

        brushingTipsProvider.isScreenDisplayable()
            .test()
            .assertValue(expectedValue)
    }

    @Test
    fun `setNoShowAgain call the editor and set the value to false`() {
        val editor = mock<SharedPreferences.Editor>()
        whenever(preferences.edit()).thenReturn(editor)

        brushingTipsProvider.setNoShowAgain()
            .test()
            .assertComplete()

        verify(editor).putBoolean(KEY_BRUSHING_TIPS_DISPLAYABLE, false)
    }
}

internal const val KEY_BRUSHING_TIPS_DISPLAYABLE = "brushing_tips_displayable"
