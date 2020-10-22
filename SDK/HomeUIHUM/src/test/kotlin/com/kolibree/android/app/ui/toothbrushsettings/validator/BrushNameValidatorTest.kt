/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.validator

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class BrushNameValidatorTest : BaseUnitTest() {

    @Test
    fun `blank name is not valid`() {
        assertFalse(BrushNameValidator.isValid(""))
        assertFalse(BrushNameValidator.isValid("    "))
    }

    @Test
    fun `name longer than 19 characters is not valid`() {
        val tooLongName = generateName(20)
        assertFalse(BrushNameValidator.isValid(tooLongName))

        val validNameWith18Characters = generateName(18)
        val tooLongNameWithEmoji = "$validNameWith18Characters😀"
        assertFalse(BrushNameValidator.isValid(tooLongNameWithEmoji))
    }

    @Test
    fun `not blank name and with at most 19 characters is valid`() {
        assertTrue(BrushNameValidator.isValid(generateName(19)))
        assertTrue(BrushNameValidator.isValid(generateName(1)))
        assertTrue(BrushNameValidator.isValid("HUM_TB_v1"))
    }

    private fun generateName(length: Int): String {
        var name = ""
        for (index in 0 until length) name += "X"
        return name
    }
}
