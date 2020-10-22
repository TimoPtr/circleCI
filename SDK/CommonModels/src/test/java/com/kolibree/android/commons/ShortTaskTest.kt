/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class ShortTaskTest {

    @Test
    fun `mind your speed is well map`() {
        assertEquals(ShortTask.MIND_YOUR_SPEED, ShortTask.fromInternalValue("ms"))
        assertEquals(ShortTask.MIND_YOUR_SPEED.internalValue, "ms")
    }

    @Test
    fun `test your angle is well map`() {
        assertEquals(ShortTask.TEST_YOUR_ANGLE, ShortTask.fromInternalValue("ta"))
        assertEquals(ShortTask.TEST_YOUR_ANGLE.internalValue, "ta")
    }

    @Test
    fun `unknown task does not map to anything`() {
        assertNull(ShortTask.fromInternalValue("hello"))
    }
}
