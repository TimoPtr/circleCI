/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.middleware

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.lifecycle.LifecycleDisposableScopeOwner
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.unity.BaseGameMiddlewareInstrumentationTest
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.kml.ShortVector
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResourcesInteractorImplTest : BaseGameMiddlewareInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val lifecycle: Lifecycle = mock()
    private val lifecycleDisposableScopeOwner =
        LifecycleDisposableScopeOwner(lifecycle)
    private val angleProvider: AngleProvider = mock()
    private val speedProvider: KpiSpeedProvider = mock()
    private val transitionProvider: TransitionProvider = mock()
    private val rnnWeightProvider: RnnWeightProvider = mock()
    private val thresholdProvider: ThresholdProvider = mock()
    private val zoneValidatorProvider: ZoneValidatorProvider = mock()
    private val kolibreeAppVersions: KolibreeAppVersions = KolibreeAppVersions("1.1.9", "2")

    private val resourcesInteractor: ResourcesInteractorImpl by lazy {
        ResourcesInteractorImpl(
            lifecycleDisposableScopeOwner,
            angleProvider,
            speedProvider,
            transitionProvider,
            rnnWeightProvider,
            thresholdProvider,
            zoneValidatorProvider,
            kolibreeAppVersions
        )
    }

    @Test
    fun getKpiAnglesJson_returnsKPIAngle_fromAngleProvider() {
        val kpiAngle = "SAMPLE_ANGLE_JSON"
        doReturn(kpiAngle).whenever(angleProvider).getKPIAngle()
        assertEquals(kpiAngle, resourcesInteractor.kpiAnglesJson)
    }

    @Test
    fun getKpiSpeedJson_returnsKpiSpeed_froKpiSpeedProvider() {
        val kpiSpeed = "SAMPLE_SPEED_JSON"
        doReturn(kpiSpeed).whenever(speedProvider).getKpiSpeed()
        assertEquals(kpiSpeed, resourcesInteractor.kpiSpeedJson)
    }

    @Test
    fun getSupervisedAnglesJson_returnsSupervisedAngle_fromAngleProvider() {
        val kpiSupervisedAngle = "SAMPLE_SUPERVISED_ANGLE_JSON"
        doReturn(kpiSupervisedAngle).whenever(angleProvider).getSupervisedAngle()
        assertEquals(kpiSupervisedAngle, resourcesInteractor.supervisedAnglesJson)
    }

    @Test
    fun getTransitionsJson_returnsTransition_fromTransitionProvider() {
        val transition = "SAMPLE_TRANSITION_JSON"
        doReturn(transition).whenever(transitionProvider).getTransition()
        assertEquals(transition, resourcesInteractor.transitionsJson)
    }

    @Test
    fun getRnnWeight_returnsRnnWeight_fromRnnWeightProvider() {
        val rnnWeights = listOf<Short>(1, 2, 3, 4, 5)
        doReturn(ShortVector(rnnWeights)).whenever(rnnWeightProvider).getRnnWeight()
        assertEquals(rnnWeights.size, resourcesInteractor.rnnWeight.size)
        assertTrue(rnnWeights.containsAll(resourcesInteractor.rnnWeight))
    }

    @Test
    fun getThresholdBalancingJson_returnsThreshold_from_thresholdProvider() {
        val thresholdBalancing = "Balancing"
        doReturn(thresholdBalancing).whenever(thresholdProvider).getThresholdBalancing()
        assertEquals(thresholdBalancing, resourcesInteractor.thresholdBalancingJson)
    }

    @Test
    fun getZoneValidatorJson_returnsZoneValidator_from_ZoneValidatorProvider() {
        val zoneValidator = "ZoneValidator"
        doReturn(zoneValidator).whenever(zoneValidatorProvider).getZoneValidator()
        assertEquals(zoneValidator, resourcesInteractor.zoneValidatorJson)
    }

    @Test
    fun resourceInteractor_properlyHandlesLifecycleDisposableScopes() {
        testInteractorLifecycle(resourcesInteractor, lifecycleDisposableScopeOwner)
    }

    @Test
    fun getAppVersion_returnsKolibreeAppVersions_appVersion() {
        assertEquals(kolibreeAppVersions.appVersion, resourcesInteractor.appVersion)
    }

    @Test
    fun getDate_returns_current_date() {
        TrustedClock.setFixedDate()

        assertEquals(dateFormatter.format(TrustedClock.getNowOffsetDateTime()), resourcesInteractor.date)
    }
}
