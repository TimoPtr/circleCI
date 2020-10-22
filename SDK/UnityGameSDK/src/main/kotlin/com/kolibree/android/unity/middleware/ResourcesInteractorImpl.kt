/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.middleware

import android.annotation.SuppressLint
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.lifecycle.LifecycleDisposableScope
import com.kolibree.android.app.lifecycle.LifecycleDisposableScopeOwner
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.game.middleware.ResourcesInteractor
import com.kolibree.game.middleware.ShortVector
import org.threeten.bp.format.DateTimeFormatter

@SuppressLint("ExperimentalClassUse")
internal class ResourcesInteractorImpl(
    private val lifecycleDisposableScopeOwner: LifecycleDisposableScopeOwner,
    private val angleProvider: AngleProvider,
    private val speedProvider: KpiSpeedProvider,
    private val transitionProvider: TransitionProvider,
    private val rnnWeightProvider: RnnWeightProvider,
    private val thresholdProvider: ThresholdProvider,
    private val zoneValidatorProvider: ZoneValidatorProvider,
    private val kolibreeAppVersions: KolibreeAppVersions
) : ResourcesInteractor(), LifecycleDisposableScope by lifecycleDisposableScopeOwner {

    init {
        lifecycleDisposableScopeOwner.monitoredClassName = javaClass.simpleName
    }

    override fun getKpiAnglesJson(): String = angleProvider.getKPIAngle()

    override fun getKpiSpeedJson(): String = speedProvider.getKpiSpeed()

    override fun getSupervisedAnglesJson(): String = angleProvider.getSupervisedAngle()

    override fun getTransitionsJson(): String = transitionProvider.getTransition()

    override fun getRnnWeight(): ShortVector = ShortVector(rnnWeightProvider.getRnnWeight())

    override fun getThresholdBalancingJson(): String = thresholdProvider.getThresholdBalancing()

    override fun getZoneValidatorJson(): String = zoneValidatorProvider.getZoneValidator()

    override fun getAppVersion(): String = kolibreeAppVersions.appVersion

    override fun getDate(): String = dateFormatter.format(TrustedClock.getNowOffsetDateTime())
}

private const val BRUSHING_SESSION_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"

@VisibleForApp
internal val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern(BRUSHING_SESSION_DATETIME_PATTERN)
