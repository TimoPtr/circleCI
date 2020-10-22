/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.animation

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertTrue
import org.junit.Test

/** [PlaqlessBrushHeadAnimationMapper] tests */
class PlaqlessBrushHeadAnimationMapperTest : BaseUnitTest() {

    private val mapper = PlaqlessBrushHeadAnimationMapper()

    @Test
    fun `upIncIntAnimation is a PlaqlessIncisorInteriorAnimation`() {
        assertTrue(mapper.upIncIntAnimation::class == PlaqlessIncisorInteriorAnimation::class)
    }

    @Test
    fun `loIncIntAnimation is a PlaqlessIncisorInteriorAnimation`() {
        assertTrue(mapper.loIncIntAnimation::class == PlaqlessIncisorInteriorAnimation::class)
    }
}
