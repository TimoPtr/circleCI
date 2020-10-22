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

/** [RegularBrushHeadAnimationMapper] tests */
class RegularBrushHeadAnimationMapperTest : BaseUnitTest() {

    private val mapper = RegularBrushHeadAnimationMapper()

    @Test
    fun `upIncIntAnimation is a IncisorInteriorAnimation`() {
        assertTrue(mapper.upIncIntAnimation::class == IncisorInteriorAnimation::class)
    }

    @Test
    fun `loIncIntAnimation is a IncisorInteriorAnimation`() {
        assertTrue(mapper.loIncIntAnimation::class == IncisorInteriorAnimation::class)
    }
}
