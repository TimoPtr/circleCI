/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.profile.HANDEDNESS_LEFT
import com.kolibree.android.commons.profile.HANDEDNESS_RIGHT
import org.junit.Assert.assertEquals
import org.junit.Test

/** AccountConstants.kt file tests */
class AccountConstantsTest : BaseUnitTest() {

    @Test
    fun `value of HANDEDNESS_LEFT is L`() {
        assertEquals("L", HANDEDNESS_LEFT)
    }

    @Test
    fun `value of HANDEDNESS_RIGHT is R`() {
        assertEquals("R", HANDEDNESS_RIGHT)
    }
}
