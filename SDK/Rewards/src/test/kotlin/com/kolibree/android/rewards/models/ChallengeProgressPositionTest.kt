/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.models

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Test

/** [ChallengeProgressPosition] tests */
class ChallengeProgressPositionTest : BaseUnitTest() {

    /*
    Constants
     */

    @Test
    fun `value of CHALLENGE_PROGRESS_NO_POSITION is -1f`() {
        assertEquals(-1f, CHALLENGE_PROGRESS_NO_POSITION)
    }

    /*
    Constructor
     */

    @Test
    fun `positions that are not passed as parameters default to CHALLENGE_PROGRESS_NO_POSITION`() {
        val challenge = ChallengeProgressPosition(mock())

        assertEquals(CHALLENGE_PROGRESS_NO_POSITION, challenge.positionX)
        assertEquals(CHALLENGE_PROGRESS_NO_POSITION, challenge.positionY)
    }
}
