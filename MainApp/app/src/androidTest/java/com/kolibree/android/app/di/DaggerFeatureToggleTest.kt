/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.di

import android.app.Application
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.BuildConfig
import com.kolibree.android.app.dagger.AppComponent
import com.kolibree.android.app.dagger.DaggerAppComponent
import com.kolibree.android.feature.Feature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.impl.ConstantFeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.network.NetworkLogFeatureToggle
import com.kolibree.android.sdk.KolibreeAndroidSdk
import com.kolibree.android.sdk.dagger.SdkComponent
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.reflect.KClass
import org.junit.Test

/**
 * This test checks if Dagger contains all features that we expect to have in our application.
 * Since we inject features into set via dagger, we cannot be sure if we have
 * all the features we need before runtime. That's why we need to test it.
 *
 * This test checks non-Espresso Dagger setup only.
 */
abstract class DaggerFeatureToggleTest : BaseInstrumentationTest() {

    abstract val expectedPersistentFeatures: Set<Feature<*>>

    abstract val constantFeatures: Set<Feature<*>>

    abstract val buildTypeDependentFeatures: Set<Feature<*>>

    abstract val transientFeatures: Set<Feature<*>>

    private fun allExpectedFeatures() =
        expectedPersistentFeatures + buildTypeDependentFeatures + constantFeatures + transientFeatures

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val application: Application = context().applicationContext as Application

    private lateinit var sdkComponent: SdkComponent

    private lateinit var appComponent: AppComponent

    override fun setUp() {
        super.setUp()
        sdkComponent = KolibreeAndroidSdk.init(
            context = context()
        )
        appComponent = DaggerAppComponent.builder()
            .context(application)
            .sdkComponent(sdkComponent)
            .build()
    }

    /**
     * If this test fails, you probably added new feature toggle to Dagger via @IntoSet.
     *
     * To make it right, please make sure to:
     * - add your new feature to `expectedFeatures` set in this class
     * - make sure you also added the toggle to Espresso setup in EspressoFeatureToggleModule
     */
    @Test
    fun makeSureWeHaveAllExpectedFeatureToggles() {
        val actualFeatures = appComponent.featureToggles().map { toggle -> toggle.feature }.toSet()

        assertEquals(
            onFailureMessage(allExpectedFeatures(), actualFeatures),
            allExpectedFeatures().size, actualFeatures.size
        )
        assertTrue(
            onFailureMessage(allExpectedFeatures(), actualFeatures),
            actualFeatures.containsAll(allExpectedFeatures())
        )
    }

    /**
     * If this test fails, you created a toggle that is not persistent.
     * Please review your Dagger configuration and make sure that you
     * inject PersistentFeatureToggle for your feature.
     */
    @Test
    fun makeSureAllExpectedTogglesArePersistent() {
        val persistentFeatures = appComponent.featureToggles()
            .filterIsInstance<PersistentFeatureToggle<*>>()
            .map { it.feature }

        assertEquals(
            onFailureMessage(expectedPersistentFeatures, persistentFeatures),
            expectedPersistentFeatures.size,
            persistentFeatures.count()
        )

        assertTrue(
            onFailureMessage(expectedPersistentFeatures, persistentFeatures),
            expectedPersistentFeatures.containsAll(persistentFeatures)
        )
    }

    @Test
    fun makeSureNetworkLogToggleIsPersistentOnlyInDebug() {
        makeSureToggleIsPersistentOnlyInDebug(NetworkLogFeatureToggle::class)
    }

    private inline fun <reified T : FeatureToggle<*>> makeSureToggleIsPersistentOnlyInDebug(
        toggleClass: KClass<T>
    ) {
        val toggle = appComponent.featureToggles()
            .filterIsInstance<T>()
            .first()
        val implementation = toggleClass.members
            .first { it.name == "implementation" }
            .call(toggle) as FeatureToggle<*>

        if (BuildConfig.DEBUG) {
            assertTrue(implementation is PersistentFeatureToggle)
        } else {
            assertTrue(implementation is ConstantFeatureToggle)
            if (implementation.feature.type() == Boolean::class)
                assertFalse(implementation.value as Boolean)
        }
    }

    private fun onFailureMessage(
        expected: Set<Feature<*>>,
        actual: Collection<Feature<*>>
    ): String {
        return "Expected: ${expected.printSortedClassNames()}\nWas: ${actual.printSortedClassNames()}"
    }

    private fun Collection<Feature<*>>.printSortedClassNames(): List<String> {
        return sortedBy { it.displayName }.map { it.javaClass.simpleName }
    }
}
