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

class PersonalChallengeEntityMapperKtTest : BaseUnitTest() {
    @Test
    fun `toSynchronizableItem includes all fields needed by SynchronizableItem`() {
        val expectedKolibreeId = 4L
        val expectedBackendId = 3L

        val expectedCompletionDate = ZonedDateTime.now().minusMinutes(1)
        val expectedCreationDate = expectedCompletionDate.minusDays(3)
        val expectedUpdateDate = expectedCompletionDate.minusMinutes(3)

        val period = PersonalChallengePeriod.SEVEN_DAYS
        val level = PersonalChallengeLevel.HARD
        val type = PersonalChallengeType.COVERAGE

        val expectedUuid = UUID.randomUUID()

        val expectedProgress = 40

        val personalChallengeEntity = PersonalChallengeEntity(
            id = 2L,
            backendId = expectedBackendId,
            profileId = expectedKolibreeId,
            progress = expectedProgress,
            completionDate = expectedCompletionDate,
            creationDate = expectedCreationDate,
            updateDate = expectedUpdateDate,
            duration = period.stringifyDuration(),
            durationUnit = period.stringifyUnit(),
            difficultyLevel = level.stringify(),
            objectiveType = type.stringify(),
            uuid = expectedUuid
        )

        val expectedChallenge = V1PersonalChallenge(
            objectiveType = objectiveFromJsonString(type.stringify()),
            difficultyLevel = levelFromStringedValue(level.stringify()),
            period = periodFromStringedData(period.stringifyDuration(), period.stringifyUnit()),
            creationDate = expectedCreationDate,
            completionDate = expectedCompletionDate,
            progress = expectedProgress
        )

        val expectedItem = ProfilePersonalChallengeSynchronizableItem(
            backendId = expectedBackendId,
            kolibreeId = expectedKolibreeId,
            challenge = expectedChallenge,
            updatedAt = expectedUpdateDate,
            uuid = expectedUuid
        )

        assertEquals(
            expectedItem, personalChallengeEntity.toSynchronizableItem()
        )
    }
}
