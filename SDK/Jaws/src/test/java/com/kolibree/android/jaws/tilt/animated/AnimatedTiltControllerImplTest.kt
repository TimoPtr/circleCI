/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt.animated

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl.Companion.ANIMATION_DURATION_MILLIS
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl.Companion.DECELERATION_FACTOR
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl.Companion.FACE_BOTTOM_WORLD_ROTATION_X
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl.Companion.FACE_LEFT_TRANSLATION_X
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl.Companion.FACE_LEFT_WORLD_ROTATION_Y
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl.Companion.FACE_RIGHT_TRANSLATION_X
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl.Companion.FACE_RIGHT_WORLD_ROTATION_Y
import com.kolibree.android.jaws.tilt.animated.AnimatedTiltControllerImpl.Companion.FACE_TOP_WORLD_ROTATION_X
import org.junit.Assert.assertEquals
import org.junit.Test

/** [AnimatedTiltControllerImpl] unit tests */
class AnimatedTiltControllerImplTest : BaseUnitTest() {

    @Test
    fun `value of ANIMATION_DURATION_MILLIS is 1400L`() {
        assertEquals(1400L, ANIMATION_DURATION_MILLIS)
    }

    @Test
    fun `value of DECELERATION_FACTOR is 2f`() {
        assertEquals(2f, DECELERATION_FACTOR)
    }

    @Test
    fun `value of FACE_TOP_WORLD_ROTATION_X is -36f`() {
        assertEquals(-36f, FACE_TOP_WORLD_ROTATION_X)
    }

    @Test
    fun `value of FACE_BOTTOM_WORLD_ROTATION_X is 36f`() {
        assertEquals(36f, FACE_BOTTOM_WORLD_ROTATION_X)
    }

    @Test
    fun `value of FACE_LEFT_WORLD_ROTATION_Y is -42f`() {
        assertEquals(-42f, FACE_LEFT_WORLD_ROTATION_Y)
    }

    @Test
    fun `value of FACE_RIGHT_WORLD_ROTATION_Y is 42f`() {
        assertEquals(42f, FACE_RIGHT_WORLD_ROTATION_Y)
    }

    @Test
    fun `value of FACE_LEFT_TRANSLATION_X is -0,24f`() {
        assertEquals(-0.24f, FACE_LEFT_TRANSLATION_X)
    }

    @Test
    fun `value of FACE_RIGHT_TRANSLATION_X is 0,24f`() {
        assertEquals(0.24f, FACE_RIGHT_TRANSLATION_X)
    }
}
