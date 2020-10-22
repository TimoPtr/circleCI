/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.ui.selectprofile.SelectProfileItem
import com.kolibree.android.app.ui.settings.binding.AboutItemBindingModel
import com.kolibree.android.app.ui.settings.binding.AdminSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.BirthDateItemBindingModel
import com.kolibree.android.app.ui.settings.binding.BrusherDetailsSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.BrushingDurationBindingModel
import com.kolibree.android.app.ui.settings.binding.DeleteAccountItemBindingModel
import com.kolibree.android.app.ui.settings.binding.EmailItemBindingModel
import com.kolibree.android.app.ui.settings.binding.FirstNameSettingsDetailItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GenderItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GetMyDataItemBindingModel
import com.kolibree.android.app.ui.settings.binding.GuidedBrushingSettingsBindingModel
import com.kolibree.android.app.ui.settings.binding.HandednessItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HelpItemBindingModel
import com.kolibree.android.app.ui.settings.binding.LinkAccountBindingModel
import com.kolibree.android.app.ui.settings.binding.LogOutItemBindingModel
import com.kolibree.android.app.ui.settings.binding.NotificationsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.PrivacyPolicyItemBindingModel
import com.kolibree.android.app.ui.settings.binding.RateOurAppItemBindingModel
import com.kolibree.android.app.ui.settings.binding.SecretSettingsBindingModel
import com.kolibree.android.app.ui.settings.binding.SelectProfileItemBindingModel
import com.kolibree.android.app.ui.settings.binding.SettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.ShareYourDataBindingModel
import com.kolibree.android.app.ui.settings.binding.TermsAndConditionsBindingModel
import com.kolibree.android.app.ui.settings.binding.VibrationLevelsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.WeeklyDigestItemBindingModel
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SettingsViewState(
    val adminSettingsItems: List<SettingsItemBindingModel> = emptyList(),
    val isLoading: Boolean = false,
    val isSecretSettingsEnabled: Boolean = false,
    private val currentProfile: Profile,
    val updatedProfile: Profile,
    val snackbarConfiguration: SnackbarConfiguration = SnackbarConfiguration(),
    val accountEmail: String? = null,
    val isWeeklyDigestEnabled: Boolean = false,
    val isDataCollectionAllowed: Boolean = false,
    val isAmazonDashAvailable: Boolean = false,
    val isAmazonDrsEnabled: Boolean = false,
    val allowDisablingDataSharing: Boolean = true,
    val selectProfileItems: List<SelectProfileItem> = emptyList()
) : BaseViewState {
    companion object {
        fun initial(
            isSecretSettingsEnabled: Boolean,
            profile: Profile,
            updatedProfile: Profile = profile,
            isAmazonDashAvailable: Boolean,
            allowDisablingDataSharing: Boolean
        ) = SettingsViewState(
            adminSettingsItems = ADMIN_SETTINGS_ITEMS,
            isSecretSettingsEnabled = isSecretSettingsEnabled,
            currentProfile = profile,
            updatedProfile = updatedProfile,
            isAmazonDashAvailable = isAmazonDashAvailable,
            allowDisablingDataSharing = allowDisablingDataSharing
        )
    }

    fun items(): List<SettingsItemBindingModel> {
        return selectProfileItem() +
            brusherSettingsItems() +
            adminSettingsItems +
            secretSettingsItems() +
            BOTTOM_ITEMS
    }

    private fun selectProfileItem(): List<SelectProfileItemBindingModel> = when {
        selectProfileItems.isEmpty() -> emptyList()
        else -> listOf(SelectProfileItemBindingModel(items = selectProfileItems))
    }

    fun visibleItems(): List<SettingsItemBindingModel> = items().filter { it.isVisible() }

    val hasProfileChanged: Boolean
        get() = updatedProfile != currentProfile

    fun hasProfileChangedFrom(profile: Profile): Boolean =
        profile != currentProfile

    private fun secretSettingsItems(): List<SettingsItemBindingModel> = when {
        isSecretSettingsEnabled -> SECRET_SETTINGS_ITEMS
        else -> emptyList()
    }

    @VisibleForTesting
    fun brusherSettingsItems(): List<SettingsItemBindingModel> {
        val brusherSettingsItems = mutableListOf(
            BrusherDetailsSettingsItemBindingModel(
                updatedProfile.firstName ?: currentProfile.firstName
            ),
            FirstNameSettingsDetailItemBindingModel(
                updatedProfile.firstName ?: currentProfile.firstName
            ),
            BirthDateItemBindingModel(updatedProfile.birthday),
            GenderItemBindingModel(updatedProfile.gender),
            HandednessItemBindingModel(updatedProfile.handedness),
            EmailItemBindingModel(accountEmail),
            BrushingDurationBindingModel(updatedProfile.brushingGoalDuration),
            WeeklyDigestItemBindingModel(isWeeklyDigestEnabled)
        )

        if (allowDisablingDataSharing) {
            brusherSettingsItems.add(ShareYourDataBindingModel(isDataCollectionAllowed))
        }

        if (isAmazonDashAvailable) {
            brusherSettingsItems.add(LinkAccountBindingModel(isAmazonDrsEnabled))
        }

        return brusherSettingsItems
    }
}

internal val ADMIN_SETTINGS_ITEMS: List<SettingsItemBindingModel> = listOf(
    AdminSettingsItemBindingModel,
    GuidedBrushingSettingsBindingModel,
    NotificationsItemBindingModel,
    VibrationLevelsItemBindingModel(),
    GetMyDataItemBindingModel,
    AboutItemBindingModel,
    HelpItemBindingModel,
    RateOurAppItemBindingModel,
    TermsAndConditionsBindingModel,
    PrivacyPolicyItemBindingModel
)

internal val BOTTOM_ITEMS = listOf(
    LogOutItemBindingModel,
    DeleteAccountItemBindingModel
)

internal val SECRET_SETTINGS_ITEMS: List<SettingsItemBindingModel> = listOf(
    SecretSettingsBindingModel
)
