/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge.mapper

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.personalchallenge.data.mapper.levelFromStringedValue
import com.kolibree.android.rewards.personalchallenge.data.mapper.objectiveFromJsonString
import com.kolibree.android.rewards.personalchallenge.data.mapper.periodFromStringedData
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringify
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyDuration
import com.kolibree.android.rewards.personalchallenge.data.mapper.stringifyUnit
import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.rewards.synchronization.personalchallenge.model.ProfilePersonalChallengeSynchronizableItem
import java.util.UUID
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.ZonedDateTime

class ProfilePersonalChallengeItemMapperKtTest : BaseUnitTest() {
    @Test
    fun `toPersistentEntity`() {
        val expectedProfileId = 4L
        val expectedBackendId = 3L

        val expectedCompletionDate = ZonedDateTime.now().minusMinutes(1)
        val expectedCreationDate = expectedCompletionDate.minusDays(3)
        val expectedUpdateDate = expectedCompletionDate.minusMinutes(3)

        val period = PersonalChallengePeriod.SEVEN_DAYS
        val level = PersonalChallengeLevel.HARD
        val type = PersonalChallengeType.COVERAGE

        val expectedUuid = UUID.randomUUID()

        val expectedProgress = 40

        val challenge = V1PersonalChallenge(
            objectiveType = objectiveFromJsonString(type.stringify()),
            difficultyLevel = levelFromStringedValue(level.stringify()),
            period = periodFromStringedData(period.stringifyDuration(), period.stringifyUnit()),
            creationDate = expectedCreationDate,
            completionDate = expectedCompletionDate,
            progress = expectedProgress
        )

        val item = ProfilePersonalChallengeSynchronizableItem(
            backendId = expectedBackendId,
            kolibreeId = expectedProfileId,
            challenge = challenge,
            updatedAt = expectedUpdateDate,
            uuid = expectedUuid
        )

        val expectedId = 6565L

        val expectedEntity = PersonalChallengeEntity(
            id = expectedId,
            backendId = expectedBackendId,
            profileId = expectedProfileId,
            progress = challenge.progress,
            completionDate = challenge.completionDate,
            creationDate = challenge.creationDate,
            updateDate = expectedUpdateDate,
            duration = challenge.period.stringifyDuration(),
            durationUnit = challenge.period.stringifyUnit(),
            difficultyLevel = challenge.difficultyLevel.stringify(),
            objectiveType = challenge.objectiveType.stringify(),
            uuid = expectedUuid
        )

        assertEquals(expectedEntity, item.toPersistentEntity(expectedId))
    }
}
