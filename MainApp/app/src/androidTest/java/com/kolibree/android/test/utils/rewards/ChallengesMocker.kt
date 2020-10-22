/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils.rewards

import com.kolibree.android.app.dagger.EspressoAppComponent
import com.kolibree.android.rewards.morewaystoearnpoints.model.EarnPointsChallenge
import com.kolibree.android.synchronizator.SynchronizableCatalogDataStore
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.test.mocks.rewards.ChallengeWithProgressBuilder

class ChallengesMocker(private val component: EspressoAppComponent) {

    fun populateChallengeDatabase(
        profileId: Long,
        earnPointsChallenges: List<EarnPointsChallenge>
    ) {
        val categoryName = "More Ways to Earn Points"
        val picture = "photo.jpg"

        val challengesToPopulate = earnPointsChallenges.map { challenge ->

            ChallengeWithProgressBuilder.TestChallenge(
                challenge.id.name,
                picture,
                challenge.id.name,
                challenge.id.backendId
            ).withProfileId(profileId)
                .withSmilesReward(challenge.points)
                .withProgress(66)
        }

        val challengesBuilder = ChallengeWithProgressBuilder.create()
            .withChallenges(
                categoryName,
                *(challengesToPopulate.toTypedArray())
            )
        populateChallengeDatabase(challengesBuilder)
    }

    fun populateChallengeDatabase(challengesBuilder: ChallengeWithProgressBuilder) {
        val challengeCatalog = challengesBuilder.buildChallengesCatalog()
        val challengeWithProgressCatalog = challengesBuilder.buildChallengeProgressCatalog()

        challengesCatalogDataStore().replace(challengeCatalog)
        challengeProgressDatastore().replace(challengeWithProgressCatalog)
    }

    fun truncateRewardsDatabase() {
        val emptyBuilder = ChallengeWithProgressBuilder.create()
        challengeProgressDatastore().replace(emptyBuilder.buildChallengeProgressCatalog())
        challengesCatalogDataStore().replace(emptyBuilder.buildChallengesCatalog())
    }

    private fun challengesCatalogDataStore() =
        component.challengesCatalogDatastore() as SynchronizableCatalogDataStore

    private fun challengeProgressDatastore() =
        component.challengeProgressDatastore() as SynchronizableReadOnlyDataStore
}
