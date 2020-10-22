/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.challengeprogress

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.models.ChallengeProgressEntity
import com.kolibree.android.rewards.persistence.ChallengeProgressProfileCatalogInternal
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ChallengeProgressProfileInternalMapperTest : BaseUnitTest() {

    @Test
    fun `map ChallengeProgressApi to ChallengeProgressProfileCatalogInternal`() {
        val challengeProgressApi = ChallengeProgressApiBuilder.build()

        val profileId = 65L

        val challengeProgressProfileCatalogInternal =
            ChallengeProgressProfileInternalMapper.toChallengeProgressProfileInternal(challengeProgressApi, profileId)

        val joinedLists = challengeProgressApi.progress.catalog.map { it.challenges }.flatten()

        val expectedChallengeProgressProfileInternal = ChallengeProgressProfileCatalogInternal().apply {
            addAll(joinedLists.map {
                ChallengeProgressEntity(
                    challengeId = it.challengeId,
                    profileId = profileId,
                    completionTime = it.completionTime,
                    completionDetails = it.completionDetails,
                    percentage = it.percentage
                )
            })
        }

        assertEquals(expectedChallengeProgressProfileInternal, challengeProgressProfileCatalogInternal)
    }
}
