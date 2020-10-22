/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.welcome

import com.kolibree.android.app.ui.settings.secret.persistence.InstallationFlags
import com.kolibree.android.app.ui.settings.secret.persistence.InstallationFlags.Flag.SHOULD_WIPE_BRUSHINGS_BEFORE_SHOWING_CALENDAR
import com.kolibree.android.app.ui.settings.secret.persistence.InstallationFlags.Flag.SHOULD_WIPE_BRUSHINGS_TO_MAKE_SURE_DATA_ARE_NOT_CORRUPTED
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository
import io.reactivex.Completable
import javax.inject.Inject

interface ExecutePresyncInstallationFlagsUseCase {

    fun executeInstallationFlags(): Completable

    companion object {

        fun noOp(): ExecutePresyncInstallationFlagsUseCase =
            object : ExecutePresyncInstallationFlagsUseCase {

                override fun executeInstallationFlags() = Completable.complete()
            }
    }
}

internal class ExecutePresyncInstallationFlagsUseCaseImpl @Inject constructor(
    private val installationFlags: InstallationFlags,
    private val brushingsRepository: BrushingsRepository,
    private val aggregatedStatsRepository: AggregatedStatsRepository
) : ExecutePresyncInstallationFlagsUseCase {

    override fun executeInstallationFlags(): Completable =
        if (shouldBrushingsAndAggregStatsBeWiped()) {
            brushingsRepository
                .deleteAll()
                .andThen(aggregatedStatsRepository.truncate())
                .doOnComplete { setBrushingsAndAggregStatsShouldNotBeWipedAnymore() }
        } else {
            Completable.complete()
        }

    private fun shouldBrushingsAndAggregStatsBeWiped(): Boolean =
        installationFlags.needsToBeHandled(SHOULD_WIPE_BRUSHINGS_TO_MAKE_SURE_DATA_ARE_NOT_CORRUPTED) ||
            installationFlags.needsToBeHandled(SHOULD_WIPE_BRUSHINGS_BEFORE_SHOWING_CALENDAR)

    private fun setBrushingsAndAggregStatsShouldNotBeWipedAnymore() {
        installationFlags.setHandled(SHOULD_WIPE_BRUSHINGS_BEFORE_SHOWING_CALENDAR)
        installationFlags.setHandled(SHOULD_WIPE_BRUSHINGS_TO_MAKE_SURE_DATA_ARE_NOT_CORRUPTED)
    }
}
