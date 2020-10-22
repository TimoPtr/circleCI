/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.secret

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.ui.settings.secret.FeatureToggleDescriptor.Companion.descriptorOf
import com.kolibree.android.app.ui.settings.secret.SecretSettingsNavigator
import com.kolibree.android.app.ui.settings.secret.SecretSettingsViewModel
import com.kolibree.android.app.ui.settings.secret.SecretSettingsViewState
import com.kolibree.android.app.ui.settings.secret.environment.ChangeEnvironmentViewModel
import com.kolibree.android.app.ui.settings.secret.environment.ChangeEnvironmentViewState
import com.kolibree.android.feature.ConvertCe2ToPlaqlessFeature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.FeatureToggleCompanionSet
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.UseTestShopFeature
import com.kolibree.android.network.environment.CustomCredentialsManager
import com.kolibree.android.network.environment.Environment
import com.kolibree.android.network.environment.EnvironmentManager
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.utils.TestFeatureToggle
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Completable
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class SecretSettingsViewModelTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun onFeatureToggleButtonCheckChanged_testSimpleHappyPath() {
        val featureUnderTest = ConvertCe2ToPlaqlessFeature
        val toggleUnderTest = TestFeatureToggle(featureUnderTest)
        val viewModel = createViewModel(toggles = setOf(toggleUnderTest))

        assertFalse(toggleUnderTest.value)
        assertFalse(viewModel.getViewState()!!.featureToggleDescriptors[0].value as Boolean)

        viewModel.onNewFeatureToggleValue(
            descriptorOf(featureUnderTest),
            featureUnderTest.initialValue.not()
        )
        assertTrue(toggleUnderTest.value)
        assertTrue(viewModel.getViewState()!!.featureToggleDescriptors[0].value as Boolean)
    }

    @Test
    fun onFeatureToggleButtonCheckChanged_testHappyPathWithCompanion() {
        val featureUnderTest = ConvertCe2ToPlaqlessFeature
        val secondFeature = UseTestShopFeature

        val toggleUnderTest = TestFeatureToggle(featureUnderTest)
        val secondFeatureToggle = TestFeatureToggle(secondFeature)
        val featureToggles = setOf(toggleUnderTest, secondFeatureToggle)
        val companionUnderTest =
            object : FeatureToggle.Companion<Boolean>(featureToggles, featureUnderTest) {

                override fun initialize() {
                    secondFeatureToggle.value = !featureUnderTest.initialValue
                }

                override fun executeUpdate(value: Boolean): Completable {
                    secondFeatureToggle.value = !value
                    return Completable.complete()
                }
            }

        val viewModel = createViewModel(
            toggles = featureToggles,
            companions = setOf(companionUnderTest)
        )
        viewModel.onStopDisposables.ready()

        assertFalse(toggleUnderTest.value)
        assertTrue(secondFeatureToggle.value)
        assertFalse(viewModel.getViewState()!!.featureToggleDescriptors[0].value as Boolean)
        assertTrue(viewModel.getViewState()!!.featureToggleDescriptors[1].value as Boolean)

        viewModel.onNewFeatureToggleValue(
            descriptorOf(featureUnderTest),
            featureUnderTest.initialValue.not()
        )
        Thread.sleep(100)

        assertTrue(toggleUnderTest.value)
        assertFalse(secondFeatureToggle.value)
        assertTrue(viewModel.getViewState()!!.featureToggleDescriptors[0].value as Boolean)
        assertFalse(viewModel.getViewState()!!.featureToggleDescriptors[1].value as Boolean)
    }

    @Test
    fun onFeatureToggleButtonCheckChanged_testUnhappyPathWithCompanion() {
        val featureUnderTest = ConvertCe2ToPlaqlessFeature
        val secondFeature = UseTestShopFeature

        val toggleUnderTest = TestFeatureToggle(featureUnderTest)
        val secondFeatureToggle = TestFeatureToggle(secondFeature)
        val featureToggles = setOf(toggleUnderTest, secondFeatureToggle)
        val companionUnderTest =
            object : FeatureToggle.Companion<Boolean>(featureToggles, featureUnderTest) {

                override fun initialize() {
                    secondFeatureToggle.value = !featureUnderTest.initialValue
                }

                override fun executeUpdate(value: Boolean): Completable =
                    Completable.error(IllegalStateException())
            }

        val viewModel = createViewModel(
            toggles = featureToggles,
            companions = setOf(companionUnderTest)
        )

        viewModel.actionsObservable.test()

        viewModel.onStopDisposables.ready()

        assertFalse(toggleUnderTest.value)
        assertTrue(secondFeatureToggle.value)
        assertFalse(viewModel.getViewState()!!.featureToggleDescriptors[0].value as Boolean)
        assertTrue(viewModel.getViewState()!!.featureToggleDescriptors[1].value as Boolean)

        viewModel.onNewFeatureToggleValue(
            descriptorOf(featureUnderTest),
            featureUnderTest.initialValue.not()
        )
        Thread.sleep(100)

        assertFalse(toggleUnderTest.value)
        assertTrue(secondFeatureToggle.value)
        assertFalse(viewModel.getViewState()!!.featureToggleDescriptors[0].value as Boolean)
        assertTrue(viewModel.getViewState()!!.featureToggleDescriptors[1].value as Boolean)
    }

    private fun createViewModel(
        toggles: FeatureToggleSet,
        companions: FeatureToggleCompanionSet = emptySet()
    ) = SecretSettingsViewModel(
        SecretSettingsViewState.initial(
            toggles,
            companions
        ),
        toggles,
        companions,
        createChangeEnvironmentViewModel()
    )

    private fun createChangeEnvironmentViewModel(): ChangeEnvironmentViewModel {
        val changeEnvironmentViewState = ChangeEnvironmentViewState(Environment.DEV)
        val environmentManager: EnvironmentManager = mock()
        val customCredentialsManager = CustomCredentialsManager(context())
        val secretSettingsNavigator: SecretSettingsNavigator = mock()
        val jobScheduler: JobScheduler = mock()
        val clearUserContentJobInfo: JobInfo = mock()

        return ChangeEnvironmentViewModel(
            changeEnvironmentViewState,
            environmentManager,
            customCredentialsManager,
            secretSettingsNavigator,
            jobScheduler,
            clearUserContentJobInfo
        )
    }
}
