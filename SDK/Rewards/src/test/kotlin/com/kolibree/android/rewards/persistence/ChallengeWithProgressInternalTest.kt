/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.persistence

import com.kolibree.android.clock.TrustedClock
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class ChallengeWithProgressInternalTest {
    @Test
    fun `shouldShowProgress returns false for Challenge with percentage 0`() {
        val challengeWithProgress = ChallengeWithProgressInternal(
            id = 0L,
            name = "",
            description = "",
            pictureUrl = "",
            category = "",
            greetingMessage = "",
            smilesReward = 0,
            percentage = 0,
            completionTime = null,
            profileId = 1,
            action = null,
            completionDetails = null
        )

        assertFalse(challengeWithProgress.shouldShowProgress())
    }

    @Test
    fun `shouldShowProgress returns false for Challenge with completionTime not null`() {
        val challengeWithProgress = ChallengeWithProgressInternal(
            id = 0L,
            name = "",
            description = "",
            pictureUrl = "",
            category = "",
            greetingMessage = "",
            smilesReward = 0,
            percentage = 0,
            completionTime = TrustedClock.getNowZonedDateTime(),
            profileId = 1,
            action = null,
            completionDetails = null
        )

        assertFalse(challengeWithProgress.shouldShowProgress())
    }

    @Test
    fun `shouldShowProgress returns true for Challenge with completionTime null and progress greater than 0`() {
        val challengeWithProgress = ChallengeWithProgressInternal(
            id = 0L,
            name = "",
            description = "",
            pictureUrl = "",
            category = "",
            greetingMessage = "",
            smilesReward = 0,
            percentage = 10,
            completionTime = null,
            profileId = 1,
            action = null,
            completionDetails = null
        )

        assertTrue(challengeWithProgress.shouldShowProgress())
    }
}
