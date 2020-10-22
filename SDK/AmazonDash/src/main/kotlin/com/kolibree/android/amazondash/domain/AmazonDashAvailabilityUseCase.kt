/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.feature.AmazonDashFeature
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.toggleForFeature
import io.reactivex.Flowable
import javax.inject.Inject

@VisibleForApp
interface AmazonDashAvailabilityUseCase {

    fun isAvailable(): Flowable<Boolean>
}

internal class AmazonDashAvailabilityUseCaseImpl @Inject constructor(
    private val featureToggles: FeatureToggleSet
) : AmazonDashAvailabilityUseCase {

    override fun isAvailable(): Flowable<Boolean> {
        return Flowable.just(featureToggles.toggleForFeature(AmazonDashFeature).value)
    }
}
