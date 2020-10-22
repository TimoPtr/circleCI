/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.checkup.CheckupUtils.brushingType
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.BrushingType
import com.kolibree.android.commons.GameApiConstants
import org.junit.Assert.assertEquals
import org.junit.Test

/** [CheckupUtils] unit tests */
class CheckupUtilsTest : BaseUnitTest() {

    /*
    brushingType
     */

    @Test
    fun `brushingType returns None if game name is null`() {
        assertEquals(BrushingType.None, brushingType(null))
    }

    @Test
    fun `brushingType returns GuidedBrushing if game name is OFFLINE`() {
        assertEquals(
            BrushingType.OfflineBrushing,
            brushingType(GameApiConstants.GAME_OFFLINE)
        )
    }

    @Test
    fun `brushingType returns TestBrushing if game name is GAME_SBA`() {
        assertEquals(
            BrushingType.TestBrushing,
            brushingType(GameApiConstants.GAME_SBA)
        )
    }

    @Test
    fun `brushingType returns GuidedBrushing for not OFFLINE game`() {
        assertEquals(
            BrushingType.GuidedBrushing,
            brushingType(GameApiConstants.GAME_COACH)
        )
        assertEquals(
            BrushingType.GuidedBrushing,
            brushingType(GameApiConstants.GAME_COACH_PLUS)
        )
        assertEquals(
            BrushingType.GuidedBrushing,
            brushingType(GameApiConstants.GAME_COACH_MANUAL)
        )
    }
}
