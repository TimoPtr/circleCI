/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SelectAvatarActionInitialProviderTest : BaseUnitTest() {
    private val actionProvider = SelectAvatarActionInitialProvider()

    @Test
    fun `provides null if action is null`() {
        assertNull(actionProvider.action)
        assertNull(actionProvider.get())
    }

    @Test
    fun `provides instance of action`() {
        val expectedAction = SelectAvatarAction.LaunchCameraAction
        actionProvider.action = expectedAction
        assertEquals(expectedAction, actionProvider.get())
    }

    @Test
    fun `nullifiesAction after it's provided`() {
        actionProvider.action = SelectAvatarAction.LaunchCameraAction
        actionProvider.get()

        assertNull(actionProvider.action)
    }
}
