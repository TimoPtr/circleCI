/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import org.junit.Test

class StageTest : BaseUnitTest() {

    @Test
    fun `nextStage for OUTER_MOLARS returns CHEWING_MOLARS`() {
        assertEquals(Stage.CHEWING_MOLARS, Stage.OUTER_MOLARS.nextStage())
    }

    @Test
    fun `nextStage for CHEWING_MOLARS returns FRONT_INCISORS`() {
        assertEquals(Stage.FRONT_INCISORS, Stage.CHEWING_MOLARS.nextStage())
    }

    @Test
    fun `nextStage for FRONT_INCISORS returns COMPLETED`() {
        assertEquals(Stage.COMPLETED, Stage.FRONT_INCISORS.nextStage())
    }

    @Test
    fun `nextStage for COMPLETED returns COMPLETED`() {
        assertEquals(Stage.COMPLETED, Stage.COMPLETED.nextStage())
    }
}
