/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import android.content.ComponentName
import android.content.pm.PackageManager
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

internal class BrushReminderComponentsToggleImplTest : BaseUnitTest() {

    lateinit var componentsToggle: BrushSyncReminderComponentsToggle

    private val notificationComponent: ComponentName = mock()
    private val restoreComponent: ComponentName = mock()
    private val packageManager: PackageManager = mock()

    override fun setup() {
        super.setup()

        doNothing().whenever(packageManager).setComponentEnabledSetting(any(), any(), any())

        componentsToggle =
            BrushReminderComponentsToggleImpl(
                notificationComponent,
                restoreComponent,
                packageManager
            )
    }

    @Test
    fun `setComponents on true set COMPONENT_ENABLED_STATE_ENABLED on notification component`() {
        componentsToggle.setComponents(true)

        verify(packageManager).setComponentEnabledSetting(
            notificationComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    @Test
    fun `setComponents on true set COMPONENT_ENABLED_STATE_ENABLED on restore component`() {
        componentsToggle.setComponents(true)

        verify(packageManager).setComponentEnabledSetting(
            restoreComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    @Test
    fun `setComponents on false set COMPONENT_ENABLED_STATE_DISABLED on notificaiton component`() {
        componentsToggle.setComponents(false)

        verify(packageManager).setComponentEnabledSetting(
            notificationComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    @Test
    fun `setComponents on false set COMPONENT_ENABLED_STATE_DISABLED on restore component`() {
        componentsToggle.setComponents(false)

        verify(packageManager).setComponentEnabledSetting(
            restoreComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
