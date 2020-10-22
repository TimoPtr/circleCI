/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.amazondrs.sync

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.amazondrs.sync.AmazonDrsSynchronizedVersions.Companion.KEY_DRS_SYNC_VERSION
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class AmazonDrsSynchronizedVersionsTest : BaseUnitTest() {
    private val context: Context = mock()

    private val prefs: SharedPreferences = mock()

    private lateinit var syncVersion: AmazonDrsSynchronizedVersions

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)

        syncVersion = AmazonDrsSynchronizedVersions(context)
    }

    @Test
    fun `getDrsVersion asks for KEY_DRS_SYNC_VERSION with default value 0`() {
        syncVersion.getDrsVersion()

        verify(prefs).getInt(KEY_DRS_SYNC_VERSION, 0)
    }

    @Test
    fun `setDrsVersion invokes putInt for KEY_DRS_SYNC_VERSION with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        syncVersion.setDrsVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_DRS_SYNC_VERSION, expectedValue)
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
