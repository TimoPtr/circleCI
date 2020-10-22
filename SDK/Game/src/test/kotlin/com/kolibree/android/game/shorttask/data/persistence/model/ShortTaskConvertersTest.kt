/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.data.persistence.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ShortTask
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ShortTaskConvertersTest : BaseUnitTest() {

    @Test(expected = IllegalStateException::class)
    fun `unknown value for short task throws`() {
        ShortTaskConverters().fromString("hello")
    }

    @Test
    fun `short tasks internal value are well converted to short task`() {
        val converter = ShortTaskConverters()
        ShortTask.values().forEach { task ->
            assertEquals("task conversion for task $task is wrong", task, converter.fromString(task.internalValue))
        }
    }

    @Test
    fun `short tasks are well converted to internal value`() {
        val converter = ShortTaskConverters()
        ShortTask.values().forEach { task ->
            assertEquals("task conversion for task $task is wrong", task.internalValue, converter.toString(task))
        }
    }
}
