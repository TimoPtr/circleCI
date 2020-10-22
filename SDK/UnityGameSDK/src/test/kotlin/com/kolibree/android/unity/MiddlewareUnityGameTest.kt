/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.unity.UnityGame
import org.junit.Assert.assertEquals
import org.junit.Test

class MiddlewareUnityGameTest : BaseUnitTest() {
    @Test
    fun `ARCHAELOGY maps to Archaelogy`() {
        assertEquals(UnityGame.Archaelogy, MiddlewareUnityGame.ARCHAELOGY.toUnityGame())
    }

    @Test
    fun `PIRATE maps to Pirate`() {
        assertEquals(UnityGame.Pirate, MiddlewareUnityGame.PIRATE.toUnityGame())
    }

    @Test
    fun `RABBIDS maps to Rabbids`() {
        assertEquals(UnityGame.Rabbids, MiddlewareUnityGame.RABBIDS.toUnityGame())
    }
}
