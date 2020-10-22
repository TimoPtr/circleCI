/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.hum

import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.CAMERA_Z
import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.JAWS_ROTATION_Y
import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.LOWER_JAW_POSITION_VECTOR_X
import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.LOWER_JAW_POSITION_VECTOR_Z
import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.LOWER_JAW_ROTATION
import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.LOWER_JAW_TRANSLATION_Y
import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.SELF_ROTATION_VECTOR_Y
import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.UPPER_JAW_ROTATION
import com.kolibree.android.jaws.hum.HumJawsViewRendererImpl.Companion.UPPER_JAW_TRANSLATION_Y
import org.junit.Assert.assertEquals
import org.junit.Test

/** [HumJawsViewRendererImpl] unit tests */
class HumJawsViewRendererImplTest {

    @Test
    fun `value of CAMERA_Z is 0f`() {
        assertEquals(0f, CAMERA_Z)
    }

    @Test
    fun `value of JAWS_ROTATION_Y is 30f`() {
        assertEquals(30f, JAWS_ROTATION_Y)
    }

    @Test
    fun `value of UPPER_JAW_ROTATION is 370f`() {
        assertEquals(370f, UPPER_JAW_ROTATION)
    }

    @Test
    fun `value of UPPER_JAW_TRANSLATION_Y is 0,33f`() {
        assertEquals(0.33f, UPPER_JAW_TRANSLATION_Y)
    }

    @Test
    fun `value of LOWER_JAW_ROTATION is 355f`() {
        assertEquals(355f, LOWER_JAW_ROTATION)
    }

    @Test
    fun `value of LOWER_JAW_TRANSLATION_Y is -0,35f`() {
        assertEquals(-0.35f, LOWER_JAW_TRANSLATION_Y)
    }

    @Test
    fun `value of LOWER_JAW_POSITION_VECTOR_Z is 0f`() {
        assertEquals(0f, LOWER_JAW_POSITION_VECTOR_Z)
    }

    @Test
    fun `value of LOWER_JAW_POSITION_VECTOR_X is 0,092f`() {
        assertEquals(0.092f, LOWER_JAW_POSITION_VECTOR_X)
    }

    @Test
    fun `value of SELF_ROTATION_VECTOR_Y is 180f`() {
        assertEquals(180f, SELF_ROTATION_VECTOR_Y)
    }
}
