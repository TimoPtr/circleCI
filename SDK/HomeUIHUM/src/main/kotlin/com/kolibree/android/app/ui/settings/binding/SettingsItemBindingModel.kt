/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.binding

import android.content.Context
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.app.ui.selectprofile.SelectProfileItem
import com.kolibree.android.app.ui.settings.getResourceId
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.homeui.hum.BR
import com.kolibree.android.homeui.hum.R
import kotlinx.android.parcel.Parcelize
import me.tatarka.bindingcollectionadapter2.ItemBinding
import me.tatarka.bindingcollectionadapter2.itembindings.ItemBindingModel
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate

internal interface SettingsItemBindingModel : ItemBindingModel, Parcelable {
    fun isVisible(): Boolean = true
}

const val UNDEFINED_RESOURCE_ID = 0

internal sealed class TextIconSettingsItemBindingModel(
    @StringRes val textRes: Int,
    @DrawableRes val iconRes: Int,
    private val visible: Boolean = true
) : SettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_text_icon)
    }

    override fun isVisible(): Boolean = visible
}

internal abstract class HeaderValueSettingsItemBindingModel(
    @StringRes val headerTextRes: Int,
    @StringRes val valueRes: Int = UNDEFINED_RESOURCE_ID,
    val value: String = ""
) : SettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_header_value)
    }
}

internal abstract class HeaderFormattedValueSettingsItemBindingModel<T>(
    @StringRes val headerTextRes: Int
) : SettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_header_formatted_value)
    }

    abstract fun formattedValue(context: Context): String
}

internal abstract class HeaderValueSettingsNotClickableItemBindingModel(
    @StringRes val headerTextRes: Int,
    val value: String
) : SettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_header_value_not_clickable)
    }
}

internal abstract class HeaderSwitchSettingsItemBindingModel(
    @StringRes val headerTextRes: Int,
    @StringRes val description: Int,
    val isChecked: Boolean
) : SettingsItemBindingModel

@Parcelize
internal data class ShareYourDataBindingModel(
    val allowDataCollection: Boolean
) : HeaderSwitchSettingsItemBindingModel(
    headerTextRes = R.string.settings_share_your_data_title,
    description = R.string.settings_share_your_data_description,
    isChecked = allowDataCollection
) {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_share_your_data)
    }
}

@Parcelize
internal data class LinkAccountBindingModel(
    val isAmazonDrsEnabled: Boolean
) : SettingsItemBindingModel {

    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_link_account)
    }

    val amazonTitle: Int
        @StringRes get() = if (isAmazonDrsEnabled) {
            R.string.settings_amazon_account_linked
        } else {
            R.string.settings_link_amazon_account
        }
}

@Parcelize
internal object GuidedBrushingSettingsBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.settings_item_guided_brushing,
    iconRes = R.drawable.ic_settings_guided
)

@Parcelize
internal object AboutItemBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.settings_item_about,
    iconRes = R.drawable.ic_about_icon
)

@Parcelize
internal data class VibrationLevelsItemBindingModel(private val showSetting: Boolean = false) :
    TextIconSettingsItemBindingModel(
        textRes = R.string.settings_vibration_levels,
        iconRes = R.drawable.ic_vibration_levels,
        visible = showSetting
    )

@Parcelize
internal object GetMyDataItemBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.settings_get_my_data,
    iconRes = R.drawable.ic_getmydata
)

@Parcelize
internal object NotificationsItemBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.settings_notifications_item,
    iconRes = R.drawable.ic_notifications
)

@Parcelize
internal object TermsAndConditionsBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.settings_terms_of_use_terms,
    iconRes = R.drawable.ic_terms_icon
)

@Parcelize
internal object PrivacyPolicyItemBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.settings_terms_of_use_policy,
    iconRes = R.drawable.ic_privacy_policy_icon
)

@Parcelize
internal object HelpItemBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.settings_help_title,
    iconRes = R.drawable.ic_help_icon
)

@Parcelize
internal object RateOurAppItemBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.settings_rate_our_app,
    iconRes = R.drawable.ic_rate_our_app
)

@Parcelize
internal object SecretSettingsBindingModel : TextIconSettingsItemBindingModel(
    textRes = R.string.secret_settings,
    iconRes = R.drawable.ic_icon_secret_settings
)

internal abstract class SettingsSectionHeaderItemBindingModel(
    @StringRes val textRes: Int,
    val value: String? = null
) : SettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_section_header)
    }
}

@Parcelize
internal data class BrusherDetailsSettingsItemBindingModel(
    val name: String
) : SettingsSectionHeaderItemBindingModel(
    textRes = R.string.settings_section_brushing_details_title,
    value = name
)

@Parcelize
internal data class FirstNameSettingsDetailItemBindingModel(
    val name: String
) : HeaderValueSettingsItemBindingModel(
    headerTextRes = R.string.settings_profile_information_name,
    value = name
)

@Parcelize
internal data class BirthDateItemBindingModel(
    val birthDate: LocalDate?
) : HeaderFormattedValueSettingsItemBindingModel<LocalDate?>(
    headerTextRes = R.string.settings_born_header
) {

    override fun formattedValue(context: Context): String =
        birthDateFormatter(context.resources, birthDate)
}

@Parcelize
internal data class GenderItemBindingModel(val gender: Gender) :
    HeaderValueSettingsItemBindingModel(
        headerTextRes = R.string.settings_profile_information_gender_hint,
        valueRes = gender.getResourceId()
    )

@Parcelize
internal data class HandednessItemBindingModel(val handedness: Handedness) :
    HeaderValueSettingsItemBindingModel(
        headerTextRes = R.string.settings_profile_information_handedness_hint,
        valueRes = handedness.getResourceId()
    )

@Parcelize
internal data class EmailItemBindingModel(
    val name: String?
) : HeaderValueSettingsNotClickableItemBindingModel(
    headerTextRes = R.string.email,
    value = name ?: ""
)

@Parcelize
internal data class BrushingDurationBindingModel(
    val duration: Duration
) : HeaderFormattedValueSettingsItemBindingModel<Duration>(headerTextRes = R.string.settings_brushing_time) {

    override fun formattedValue(context: Context): String =
        durationFormatter(context.resources, duration)
}

@Parcelize
internal data class WeeklyDigestItemBindingModel(
    val weeklyDigestEnabled: Boolean
) : HeaderSwitchSettingsItemBindingModel(
    headerTextRes = R.string.settings_weekly_digest_title,
    description = R.string.settings_weekly_digest_description,
    isChecked = weeklyDigestEnabled
) {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_weekly_diggest)
    }
}

@Parcelize
internal object AdminSettingsItemBindingModel : SettingsSectionHeaderItemBindingModel(
    textRes = R.string.settings_item_admin_settings
)

@Parcelize
internal object LogOutItemBindingModel : SettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_logout)
    }
}

@Parcelize
internal object DeleteAccountItemBindingModel : SettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_delete_account)
    }
}

@Parcelize
internal data class SelectProfileItemBindingModel(
    val items: List<SelectProfileItem>
) : SettingsItemBindingModel {
    override fun onItemBind(itemBinding: ItemBinding<*>) {
        itemBinding.set(BR.item, R.layout.item_settings_select_profile)
    }
}
