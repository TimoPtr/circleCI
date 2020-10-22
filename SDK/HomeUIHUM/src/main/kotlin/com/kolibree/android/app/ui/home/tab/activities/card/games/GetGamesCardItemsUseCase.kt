/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.activities.card.games

import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.ShowGamesCardFeature
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.homeui.hum.R
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

internal interface GetGamesCardItemsUseCase {

    fun getItems(): Flowable<List<GamesCardItem>>
}

@SuppressWarnings("all") // this is just a mock
internal class MockGetGamesCardItemsUseCaseImpl @Inject constructor(
    private val featureToggleSet: FeatureToggleSet
) : GetGamesCardItemsUseCase {

    override fun getItems(): Flowable<List<GamesCardItem>> {
        return if (!cardEnabled()) Flowable.empty()
        else Flowable.interval(1, TimeUnit.SECONDS, Schedulers.io())
            .map { mockItems(progress = it.toInt() * 10 % 110) }
            .startWith(mockItems(progress = 0))
    }

    private fun cardEnabled(): Boolean {
        return featureToggleSet.toggleIsOn(ShowGamesCardFeature)
    }

    private fun mockItems(progress: Int): List<GamesCardItem> {
        return listOf(
            GamesCardItem(
                logoRes = R.drawable.mock_rabbids_logo,
                points = 2,
                title = "Rabbids - FREE",
                body = "Some short description about the game will be here",
                status = GamesCardItem.Status.DownloadAvailable,
                size = "23MB"
            ),
            GamesCardItem(
                logoRes = R.drawable.mock_pirates_logo,
                points = 5,
                title = "Archaeology - \$5.99",
                body = "Some short description about the game will be here",
                status = GamesCardItem.Status.DownloadInProgress(progress),
                size = "12MB"
            ),
            GamesCardItem(
                logoRes = R.drawable.mock_pirates_logo,
                points = 10,
                title = "Archaeology - \$5.99",
                body = "Some short description about the game will be here",
                status = GamesCardItem.Status.UpdateAvailable,
                size = "12MB"
            ),
            GamesCardItem(
                logoRes = R.drawable.mock_pirates_logo,
                points = 15,
                title = "Archaeology - \$5.99",
                body = "Some short description about the game will be here",
                status = GamesCardItem.Status.DownloadDone,
                size = "12234MB"
            )
        )
    }
}
