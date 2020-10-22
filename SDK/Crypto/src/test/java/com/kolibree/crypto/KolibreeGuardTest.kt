/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.crypto

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

class KolibreeGuardTest : BaseUnitTest() {

    @Test
    fun testGetPassword() {
        val kolibreeGuard = KolibreeGuard()

        assertEquals("NetworkNotAvailableException", kolibreeGuard.xorPassword)
    }
}
