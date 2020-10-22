/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.synchronization

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushsyncreminder.synchronization.BrushSyncReminderSynchronizedVersions.Companion.KEY_BRUSH_SYNC_REMINDER_VERSION
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class BrushSyncReminderSynchronizedVersionsTest : BaseUnitTest() {

    private val context: Context = mock()
    private val prefs: SharedPreferences = mock()

    private lateinit var brushSyncReminderSynchronizedVersions: BrushSyncReminderSynchronizedVersions

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)

        brushSyncReminderSynchronizedVersions =
            BrushSyncReminderSynchronizedVersions(
                context
            )
    }

    @Test
    fun `default version is 0`() {
        brushSyncReminderSynchronizedVersions.getVersion()
        verify(prefs).getInt(KEY_BRUSH_SYNC_REMINDER_VERSION, 0)
    }

    @Test
    fun `saves new version to prefs`() {
        val editor: SharedPreferences.Editor = mock()
        whenever(prefs.edit()).thenReturn(editor)
        whenever(editor.putInt(any(), any())).thenReturn(editor)

        val newVersion = 123
        brushSyncReminderSynchronizedVersions.setVersion(newVersion)

        inOrder(prefs.edit()) {
            verify(prefs.edit()).putInt(KEY_BRUSH_SYNC_REMINDER_VERSION, newVersion)
            verify(prefs.edit()).apply()
        }
    }
}
