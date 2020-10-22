/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.migration

import android.content.Context
import android.content.SharedPreferences
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.migration.MigrationProviderImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test

class MigrationProviderImplTest : BaseUnitTest() {

    val context: Context = mock()
    private val preferences: SharedPreferences = mock()

    private lateinit var migrationProvider: MigrationProviderImpl

    @Before
    fun setUp() {
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)

        migrationProvider = MigrationProviderImpl(context)
    }

    @Test
    fun `getMigrationsDoneCount should call the preferences and returns its value`() {

        val expectedCount = 20L

        whenever(preferences.getLong(KEY_MIGRATION_DONE_COUNT, -1))
            .thenReturn(expectedCount)

        migrationProvider.getStartNextMigrationAt()
            .test()
            .assertValue(expectedCount)

        verify(preferences).getLong(KEY_MIGRATION_DONE_COUNT, -1)
    }

    @Test
    fun `setMigrationsDoneCount should call the preferences function and complete`() {
        val expectedCount = 20L
        val editor = mock<SharedPreferences.Editor>()

        whenever(preferences.edit()).thenReturn(editor)

        migrationProvider.setStartNextMigrationAt(expectedCount)
            .test()
            .assertComplete()

        inOrder(editor) {
            this.verify(editor).putLong(KEY_MIGRATION_DONE_COUNT, expectedCount)
        }
    }
}

const val KEY_MIGRATION_DONE_COUNT = "start_next_migration_at"
