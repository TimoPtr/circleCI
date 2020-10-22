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
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalChallengeObjectiveMapperTest : BaseUnitTest() {

    @Test
    fun `STREAK maps to 'streak' value`() {
        assertEquals("streak", PersonalChallengeType.STREAK.stringify())
    }

    @Test
    fun `COVERAGE maps to 'coverage' value`() {
        assertEquals("coverage", PersonalChallengeType.COVERAGE.stringify())
    }

    @Test
    fun `DURATION maps to 'duration' value`() {
        assertEquals("duration", PersonalChallengeType.DURATION.stringify())
    }

    @Test
    fun `COACH_PLUS maps to 'duration' value`() {
        assertEquals("co+", PersonalChallengeType.COACH_PLUS.stringify())
    }

    @Test
    fun `OFFLINE maps to 'duration' value`() {
        assertEquals("of", PersonalChallengeType.OFFLINE.stringify())
    }

    @Test
    fun `objectiveFromJsonString('streak') returns STREAK`() {
        assertEquals(PersonalChallengeType.STREAK, objectiveFromJsonString("streak"))
    }

    @Test
    fun `objectiveFromJsonString('coverage') returns COVERAGE`() {
        assertEquals(PersonalChallengeType.COVERAGE, objectiveFromJsonString("coverage"))
    }

    @Test
    fun `objectiveFromJsonString('duration') returns DURATION`() {
        assertEquals(PersonalChallengeType.DURATION, objectiveFromJsonString("duration"))
    }

    @Test
    fun `objectiveFromJsonString('co+') returns COACH_PLUS`() {
        assertEquals(PersonalChallengeType.COACH_PLUS, objectiveFromJsonString("co+"))
    }

    @Test
    fun `objectiveFromJsonString('of') returns OFFLINE`() {
        assertEquals(PersonalChallengeType.OFFLINE, objectiveFromJsonString("of"))
    }

    @Test(expected = NoSuchElementException::class)
    fun `objectiveFromJsonString('otherValue') throws exception`() {
        objectiveFromJsonString("otherValue")
    }
}
