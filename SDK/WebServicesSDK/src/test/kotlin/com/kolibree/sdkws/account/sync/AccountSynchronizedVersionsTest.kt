/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.sync

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.account.sync.AccountSynchronizedVersions.Companion.KEY_ACCOUNT_SYNC_VERSION
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class AccountSynchronizedVersionsTest : BaseUnitTest() {
    private val context: Context = mock()

    private val prefs: SharedPreferences = mock()

    private lateinit var syncVersion: AccountSynchronizedVersions

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)

        syncVersion = AccountSynchronizedVersions(context)
    }

    @Test
    fun `getAccountVersion asks for KEY_ACCOUNT_SYNC_VERSION with default value 0`() {
        syncVersion.getAccountVersion()

        verify(prefs).getInt(KEY_ACCOUNT_SYNC_VERSION, 0)
    }

    @Test
    fun `setAccountVersion invokes putInt for KEY_ACCOUNT_SYNC_VERSION with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        syncVersion.setAccountVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_ACCOUNT_SYNC_VERSION, expectedValue)
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
