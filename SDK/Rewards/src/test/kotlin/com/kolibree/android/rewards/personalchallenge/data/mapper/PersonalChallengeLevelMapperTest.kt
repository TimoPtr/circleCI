/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.mapper

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalChallengeLevelMapperTest : BaseUnitTest() {

    @Test
    fun `EASY maps to 'easy' value`() {
        assertEquals("easy", PersonalChallengeLevel.EASY.stringify())
    }

    @Test
    fun `HARD maps to 'hard' value`() {
        assertEquals("hard", PersonalChallengeLevel.HARD.stringify())
    }

    @Test
    fun `levelFromJsonString('easy') returns EASY`() {
        assertEquals(PersonalChallengeLevel.EASY, levelFromStringedValue("easy"))
    }

    @Test
    fun `levelFromJsonString('hard') returns HARD`() {
        assertEquals(PersonalChallengeLevel.HARD, levelFromStringedValue("hard"))
    }

    @Test(expected = NoSuchElementException::class)
    fun `levelFromJsonString('otherValue') throws exception`() {
        levelFromStringedValue("otherValue")
    }
}
