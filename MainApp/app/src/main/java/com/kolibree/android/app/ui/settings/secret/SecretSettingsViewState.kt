/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret

import android.os.Parcel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.settings.secret.FeatureToggleDescriptor.Companion.descriptorOf
import com.kolibree.android.feature.Feature
import com.kolibree.android.feature.FeatureToggleCompanionSet
import com.kolibree.android.feature.FeatureToggleSet
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize

data class FeatureToggleDescriptor<T : Any>(
    val feature: Feature<out T>,
    val initialValue: T,
    val value: T,
    val displayable: Boolean,
    val displayName: String,
    // TODO add support for single-choice enums https://kolibree.atlassian.net/browse/KLTB002-9495
    // TODO add support for multi-choice enums https://kolibree.atlassian.net/browse/KLTB002-9496
    val displayValue: String? = value.toString(),
    val requiresAppRestart: Boolean
) {
    fun describesTheSameFeature(other: FeatureToggleDescriptor<*>): Boolean =
        displayName == other.displayName

    companion object {

        fun <T : Any> descriptorOf(
            feature: Feature<out T>,
            value: T = feature.initialValue
        ) =
            FeatureToggleDescriptor(
                feature = feature,
                initialValue = feature.initialValue,
                displayable = feature.displayable,
                value = value,
                displayName = feature.displayName,
                requiresAppRestart = feature.requiresAppRestart
            )
    }
}

@Parcelize
internal data class SecretSettingsViewState(
    val featureToggleDescriptors: List<FeatureToggleDescriptor<*>>
) : BaseViewState {

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> copyWithNewFeatureToggleValue(
        feature: FeatureToggleDescriptor<T>,
        value: T
    ): SecretSettingsViewState {
        val mutableDescriptors = featureToggleDescriptors.toMutableList()
        val indexOfDescriptor =
            mutableDescriptors.indexOfFirst { it.describesTheSameFeature(feature) }
        val descriptor = mutableDescriptors[indexOfDescriptor] as FeatureToggleDescriptor<T>
        mutableDescriptors[indexOfDescriptor] =
            descriptor.copy(value = value, displayValue = value.toString())
        return copy(featureToggleDescriptors = mutableDescriptors)
    }

    fun copyWithRefreshedToggles(
        featureToggles: FeatureToggleSet
    ): SecretSettingsViewState {
        return copy(featureToggleDescriptors = featureToggles.toDescriptors())
    }

    companion object : Parceler<SecretSettingsViewState> {

        fun initial(
            featureToggles: FeatureToggleSet,
            featureTogglesCompanions: FeatureToggleCompanionSet
        ): SecretSettingsViewState {

            // We need to make sure descriptors will be up-to-date before creation
            featureTogglesCompanions.forEach { companion -> companion.initialize() }

            return SecretSettingsViewState(featureToggles.toDescriptors())
        }

        override fun SecretSettingsViewState.write(parcel: Parcel, flags: Int) {
            // no-op
        }

        override fun create(parcel: Parcel) = SecretSettingsViewState(emptyList())
    }
}

private fun FeatureToggleSet.toDescriptors() =
    map { descriptorOf(it.feature, it.value) }
        .sortedBy { it.displayName }
