/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.appversion

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppVersionProviderImplTest : BaseUnitTest() {

    val context: Context = mock()
    private val preferences: SharedPreferences = mock()

    private lateinit var appVersionProviderImpl: AppVersionProviderImpl

    @Before
    fun setUp() {
        whenever(context.applicationContext).thenReturn(context)
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)

        appVersionProviderImpl = AppVersionProviderImpl(context)
    }

    @Test
    fun `currentVersionCode should be provided by the packageManager`() {

        val expectedVersionCode = 123L

        setCurrentVersionCodeHappyPath(expectedVersionCode)

        val currentVersionCode = appVersionProviderImpl.currentVersionCode

        assertEquals(expectedVersionCode, currentVersionCode)
    }

    @Test
    fun `getLastVersionCode should pass the last version code from the preferences`() {

        val expectedVersionCode = 246L

        whenever(preferences.getLong("last_version_code", -1))
            .thenReturn(expectedVersionCode)

        val lastVersionCode = appVersionProviderImpl.getLastVersionCode()

        assertEquals(expectedVersionCode, lastVersionCode)
    }

    @Test
    fun `updateLastVersionCode should update the version codes by calling the preferences if the app has changed version`() {

        val lastVersionCode = 15L
        val currentVersionCode = 16L
        val editor = mock<SharedPreferences.Editor>()

        whenever(preferences.edit()).thenReturn(editor)
        whenever(preferences.getLong(KEY_CURRENT_VERSION_CODE, -1))
            .thenReturn(lastVersionCode)
        setCurrentVersionCodeHappyPath(currentVersionCode)

        appVersionProviderImpl.updateLastVersionCode()

        inOrder(preferences, editor) {
            verify(preferences).edit()
            verify(editor).putLong(KEY_CURRENT_VERSION_CODE, currentVersionCode)
            verify(editor).putLong(KEY_LAST_VERSION_CODE, lastVersionCode)
            verify(editor).apply()
        }
    }

    @Test
    fun `updateLastVersionCode should not update the version codes if the app has the same version`() {

        val lastVersionCode = 15L
        val currentVersionCode = 15L
        val editor = mock<SharedPreferences.Editor>()

        whenever(preferences.edit()).thenReturn(editor)
        whenever(preferences.getLong(KEY_CURRENT_VERSION_CODE, -1))
            .thenReturn(lastVersionCode)
        setCurrentVersionCodeHappyPath(currentVersionCode)

        appVersionProviderImpl.updateLastVersionCode()

        verify(preferences, never()).edit()
        verify(editor, never()).putLong(any(), any())
    }

    private fun setCurrentVersionCodeHappyPath(expectedVersionCode: Long) {
        val packageManager = mock<PackageManager>()
        val packageName = "package.name"
        val packageInfo = mock<PackageInfo>().apply {
            versionCode = expectedVersionCode.toInt()
        }

        whenever(context.packageManager).thenReturn(packageManager)
        whenever(context.packageName).thenReturn(packageName)
        whenever(packageManager.getPackageInfo(packageName, 0)).thenReturn(packageInfo)
        whenever(packageInfo.longVersionCode).thenReturn(expectedVersionCode)
    }

    companion object {
        const val KEY_CURRENT_VERSION_CODE = "current_version_code"
        const val KEY_LAST_VERSION_CODE = "last_version_code"
    }
}
