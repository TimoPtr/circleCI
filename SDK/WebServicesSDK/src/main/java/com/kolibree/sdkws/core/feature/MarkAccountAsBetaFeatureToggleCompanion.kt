/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core.feature

import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.MarkAccountAsBetaFeature
import com.kolibree.android.feature.impl.TransientFeatureToggle
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.data.request.BetaData
import io.reactivex.Completable
import javax.inject.Inject

class MarkAccountAsBetaFeatureToggleCompanion @Inject constructor(
    featureToggleSet: FeatureToggleSet,
    private val kolibreeConnector: InternalKolibreeConnector
) : TransientFeatureToggle.Companion<Boolean>(featureToggleSet, MarkAccountAsBetaFeature) {

    override fun getInitialValue() = kolibreeConnector.beta

    override fun executeUpdate(value: Boolean): Completable =
        kolibreeConnector.updateBetaAccount(kolibreeConnector.accountId, BetaData(value))
}
