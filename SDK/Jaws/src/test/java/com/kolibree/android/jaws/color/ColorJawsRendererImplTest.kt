/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.color

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.ANIMATION_DURATION
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.LOWER_JAW_CLOSE_ROTATION
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.LOWER_JAW_CLOSE_TRANSLATION_Y
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.LOWER_JAW_CLOSE_TRANSLATION_Z
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.LOWER_JAW_OPEN_ROTATION
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.LOWER_JAW_OPEN_TRANSLATION_Y
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.LOWER_JAW_OPEN_TRANSLATION_Z
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.UPPER_JAW_CLOSE_ROTATION
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.UPPER_JAW_CLOSE_TRANSLATION_Y
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.UPPER_JAW_CLOSE_TRANSLATION_Z
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.UPPER_JAW_OPEN_ROTATION
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.UPPER_JAW_OPEN_TRANSLATION_Y
import com.kolibree.android.jaws.color.ColorJawsRendererImpl.Companion.UPPER_JAW_OPEN_TRANSLATION_Z
import org.junit.Assert.assertEquals
import org.junit.Test

/** [ColorJawsRendererImpl] tests */
class ColorJawsRendererImplTest : BaseUnitTest() {

    /*
    Constants
     */

    @Test
    fun `value of ANIMATION_DURATION is 500f`() {
        assertEquals(500f, ANIMATION_DURATION)
    }

    @Test
    fun `value of UPPER_JAW_OPEN_ROTATION is 340f`() {
        assertEquals(340f, UPPER_JAW_OPEN_ROTATION)
    }

    @Test
    fun `value of UPPER_JAW_OPEN_TRANSLATION_Y is 11f`() {
        assertEquals(11f, UPPER_JAW_OPEN_TRANSLATION_Y)
    }

    @Test
    fun `value of UPPER_JAW_OPEN_TRANSLATION_Z is 0f`() {
        assertEquals(0f, UPPER_JAW_OPEN_TRANSLATION_Z)
    }

    @Test
    fun `value of UPPER_JAW_CLOSE_ROTATION is 375f`() {
        assertEquals(375f, UPPER_JAW_CLOSE_ROTATION)
    }

    @Test
    fun `value of UPPER_JAW_CLOSE_TRANSLATION_Y is 7f`() {
        assertEquals(7f, UPPER_JAW_CLOSE_TRANSLATION_Y)
    }

    @Test
    fun `value of UPPER_JAW_CLOSE_TRANSLATION_Z is 4f`() {
        assertEquals(4f, UPPER_JAW_CLOSE_TRANSLATION_Z)
    }

    @Test
    fun `value of LOWER_JAW_OPEN_ROTATION is 385f`() {
        assertEquals(385f, LOWER_JAW_OPEN_ROTATION)
    }

    @Test
    fun `value of LOWER_JAW_OPEN_TRANSLATION_Y is -14f`() {
        assertEquals(-14f, LOWER_JAW_OPEN_TRANSLATION_Y)
    }

    @Test
    fun `value of LOWER_JAW_OPEN_TRANSLATION_Z is 0f`() {
        assertEquals(0f, LOWER_JAW_OPEN_TRANSLATION_Z)
    }

    @Test
    fun `value of LOWER_JAW_CLOSE_ROTATION is 345f`() {
        assertEquals(345f, LOWER_JAW_CLOSE_ROTATION)
    }

    @Test
    fun `value of LOWER_JAW_CLOSE_TRANSLATION_Y -9f `() {
        assertEquals(-9f, LOWER_JAW_CLOSE_TRANSLATION_Y)
    }

    @Test
    fun `value of LOWER_JAW_CLOSE_TRANSLATION_Z is 5f`() {
        assertEquals(5f, LOWER_JAW_CLOSE_TRANSLATION_Z)
    }
}
