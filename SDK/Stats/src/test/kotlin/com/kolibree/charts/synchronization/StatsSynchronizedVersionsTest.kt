/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.charts.synchronization.StatsSynchronizedVersions.Companion.KEY_IN_OFF_BRUSHINGS_COUNT
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class StatsSynchronizedVersionsTest : BaseUnitTest() {
    private val context: Context = mock()

    private val prefs: SharedPreferences = mock()

    private lateinit var syncVersion: StatsSynchronizedVersions

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)

        syncVersion = StatsSynchronizedVersions(context)
    }

    /*
    in off Brushing count
     */

    @Test
    fun `inOffBrushingsCountVersion asks for KEY_IN_OFF_BRUSHINGS_COUNT with default value 0`() {
        syncVersion.inOffBrushingsCountVersion()

        verify(prefs).getInt(KEY_IN_OFF_BRUSHINGS_COUNT, 0)
    }

    @Test
    fun `setInOffBrushingsCountVersion invokes putInt for KEY_IN_OFF_BRUSHINGS_COUNT with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        syncVersion.setInOffBrushingsCountVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_IN_OFF_BRUSHINGS_COUNT, expectedValue)
            verify(editor).apply()
        }
    }

    private fun mockPrefsEditor(): SharedPreferences.Editor {
        val editor: SharedPreferences.Editor = mock()

        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putInt(any(), any())).thenReturn(editor)

        return editor
    }
}
