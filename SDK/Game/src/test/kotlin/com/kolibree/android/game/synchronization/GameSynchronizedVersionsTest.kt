/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.game.synchronization.GameSynchronizedVersions.Companion.KEY_GAME_PROGRESS
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class GameSynchronizedVersionsTest : BaseUnitTest() {

    private val context = mock<Context>()
    private val prefs = mock<SharedPreferences>()

    private lateinit var gameSynchronizedVersions: GameSynchronizedVersions

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)

        gameSynchronizedVersions = GameSynchronizedVersions(context)
    }

    /*
    GAME PROGRESS
     */

    @Test
    fun `gameProgressVersion asks for KEY_GAME_PROGRESS with default value 0`() {
        gameSynchronizedVersions.gameProgressVersion()

        verify(prefs).getInt(KEY_GAME_PROGRESS, 0)
    }

    @Test
    fun `setGameProgressVersion invokes putInt for KEY_GAME_PROGRESS with expected value`() {
        val editor = mockPrefsEditor()

        val expectedValue = 543
        gameSynchronizedVersions.setGameProgressVersion(expectedValue)

        inOrder(editor) {
            verify(editor).putInt(KEY_GAME_PROGRESS, expectedValue)
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
