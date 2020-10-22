/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.challengeprogress

import org.threeten.bp.ZonedDateTime

internal object ChallengeProgressApiBuilder {

    fun build(challengeItems: List<ChallengesItem> = listOf()): ChallengeProgressApi {
        val catalogList: List<CatalogItem> =
            if (challengeItems.isEmpty()) {
                defaultCatalogs()
            } else {
                challengeItems
                    .groupBy { it.challengeId }
                    .map { entry ->
                        val categoryName = entry.key.toString()

                        CatalogItem(categoryId = entry.key, category = categoryName, challenges = entry.value)
                    }
            }

        val progress = Progress(language = "FR", catalog = catalogList)
        return ChallengeProgressApi(progress, 83)
    }

    private fun defaultCatalogs(): List<CatalogItem> {
        val challengeRookie1 = createChallengesItem(challengeId = 1L)
        val challengeRookie2 = createChallengesItem(challengeId = 2L)

        val challengeExpert = createChallengesItem(challengeId = 3L)

        val challengeDentist = createChallengesItem(challengeId = 4L)

        val rookieCatalog =
            CatalogItem(
                categoryId = 1,
                category = "rookie",
                challenges = listOf(challengeRookie1, challengeRookie2)
            )
        val expertCatalog =
            CatalogItem(categoryId = 2, category = "expert", challenges = listOf(challengeExpert))
        val dentistCatalog =
            CatalogItem(categoryId = 3, category = "dentist", challenges = listOf(challengeDentist))

        return listOf(rookieCatalog, expertCatalog, dentistCatalog)
    }

    fun createChallengesItem(
        challengeId: Long,
        completionTime: ZonedDateTime? = null,
        completionDetails: ChallengeCompletionDetails? = null,
        pictureUrl: String = "",
        percentage: Int = 2,
        challengeName: String = "",
        smilesReward: Int = 0
    ): ChallengesItem {
        return ChallengesItem(
            challengeId = challengeId,
            completionTime = completionTime,
            pictureUrl = pictureUrl,
            percentage = percentage,
            challengeName = challengeName,
            smilesReward = smilesReward,
            completionDetails = completionDetails
        )
    }
}
