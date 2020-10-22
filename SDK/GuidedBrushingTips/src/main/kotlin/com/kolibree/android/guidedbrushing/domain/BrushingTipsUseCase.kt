/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.domain

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.GuidedBrushingTipsFeature
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.guidedbrushing.data.BrushingTipsProvider
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@VisibleForApp
interface BrushingTipsUseCase {

    fun isBrushingTipsDisplayable(): Single<Boolean>

    fun setHasClickedNoShowAgain(): Completable
}

internal class BrushingTipsUseCaseImpl @Inject constructor(
    private val brushingTipsProvider: BrushingTipsProvider,
    private val appConfiguration: AppConfiguration,
    private val featureToggleSet: FeatureToggleSet
) : BrushingTipsUseCase {

    override fun isBrushingTipsDisplayable(): Single<Boolean> {
        return Single.fromCallable {
            appConfiguration.showGuidedBrushingTips &&
                featureToggleSet.toggleIsOn(GuidedBrushingTipsFeature)
        }.flatMap { shouldShowPrerequisites ->

            if (!shouldShowPrerequisites) {
                Single.just(false)
            } else {
                brushingTipsProvider.isScreenDisplayable()
            }
        }
    }

    override fun setHasClickedNoShowAgain(): Completable {
        return brushingTipsProvider.setNoShowAgain()
    }
}
