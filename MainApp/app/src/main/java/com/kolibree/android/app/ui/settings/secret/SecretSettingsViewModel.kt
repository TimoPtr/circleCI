/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.settings.secret.environment.ChangeEnvironment
import com.kolibree.android.app.ui.settings.secret.environment.ChangeEnvironmentViewModel
import com.kolibree.android.feature.Feature
import com.kolibree.android.feature.FeatureToggleCompanionSet
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.companionForFeatureByNameAndType
import com.kolibree.android.feature.toggleForFeatureByNameAndType
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import javax.inject.Inject
import timber.log.Timber

internal class SecretSettingsViewModel(
    initialViewState: SecretSettingsViewState,
    // Toggles are not parcelable (as per design). Therefore they cannot be pushed to state.
    private val featureToggles: FeatureToggleSet,
    private val featureToggleCompanions: FeatureToggleCompanionSet,
    private val changeEnvironmentViewModel: ChangeEnvironmentViewModel
) : BaseViewModel<SecretSettingsViewState, SecretSettingsBaseAction>(
    initialViewState,
    children = setOf(changeEnvironmentViewModel)
), ChangeEnvironment by changeEnvironmentViewModel {
    @Suppress("UNCHECKED_CAST")
    val binaryToggleDescriptors: LiveData<List<FeatureToggleDescriptor<Boolean>>> =
        map(viewStateLiveData) { state ->
            state?.featureToggleDescriptors
                ?.filter { it.feature.type() == Boolean::class }
                ?.map { it as FeatureToggleDescriptor<Boolean> }
                ?.filter { it.displayable }
        }

    // All other toggles
    val otherDescriptors: LiveData<List<FeatureToggleDescriptor<*>>> =
        map(viewStateLiveData) { state ->
            state?.featureToggleDescriptors
                ?.filter { it.feature.type() != Boolean::class }
                ?.filter { it.displayable }
        }

    fun <T : Any> onNewFeatureToggleValue(
        descriptor: FeatureToggleDescriptor<T>,
        newValue: T
    ) {
        @Suppress("UNCHECKED_CAST")
        if ((descriptor.feature as Feature<T>).validate(newValue).not()) {
            pushAction(ShowOperationFailedAction)
            return
        }

        updateStateWithNewFeatureToggleValue(descriptor, newValue) {
            if (descriptor.requiresAppRestart) pushAction(ShowAppRestartAction)
        }
    }

    fun <T : Any> showFeatureEditDialog(descriptor: FeatureToggleDescriptor<T>) =
        pushAction(ShowFeatureEditDialog(descriptor))

    private fun <T : Any> updateStateWithNewFeatureToggleValue(
        descriptor: FeatureToggleDescriptor<T>,
        newValue: T,
        onSuccess: () -> Unit
    ) {
        val featureToggle = featureToggles.toggleForFeatureByNameAndType(
            descriptor.displayName,
            descriptor.feature.type()
        )
        val featureCompanion = featureToggleCompanions.companionForFeatureByNameAndType(
            descriptor.displayName,
            descriptor.feature.type()
        )

        if (featureCompanion != null) {
            if (featureCompanion.featureValue == newValue) return

            val previousValue = featureCompanion.featureValue

            updateViewState { copyWithNewFeatureToggleValue(descriptor, newValue) }
            disposeOnStop {
                featureCompanion.update(newValue)
                    .subscribe({
                        updateViewState { copyWithRefreshedToggles(featureToggles) }
                        onSuccess()
                    }, { e ->
                        Timber.e(e)
                        updateViewState { copyWithNewFeatureToggleValue(descriptor, previousValue) }
                        pushAction(ShowOperationFailedAction)
                    })
            }
        } else {
            featureToggle.value = newValue
            updateViewState { copyWithNewFeatureToggleValue(descriptor, newValue) }
            onSuccess()
        }
    }

    class Factory @Inject constructor(
        private val featureToggles: FeatureToggleSet,
        private val featureToggleCompanions: FeatureToggleCompanionSet,
        private val changeEnvironmentViewModel: ChangeEnvironmentViewModel
    ) : BaseViewModel.Factory<SecretSettingsViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SecretSettingsViewModel(
                viewState ?: SecretSettingsViewState.initial(
                    featureToggles,
                    featureToggleCompanions
                ),
                featureToggles,
                featureToggleCompanions,
                changeEnvironmentViewModel
            ) as T
    }
}
