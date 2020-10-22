/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import com.kolibree.android.app.ui.selectprofile.SelectProfileInteraction
import com.kolibree.android.app.ui.settings.binding.HeaderFormattedValueSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HeaderSwitchSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.HeaderValueSettingsItemBindingModel
import com.kolibree.android.app.ui.settings.binding.TextIconSettingsItemBindingModel

internal interface SettingsInteraction : TextIconSettingsItemInteraction,
    HeaderValueSettingsItemInteraction,
    HeaderFormattedValueSettingsItemInteraction,
    LogoutInteraction,
    DeleteAccountInteraction,
    HeaderSwitchSettingsItemInteraction,
    LinkAccountInteraction,
    SelectProfileInteraction

internal interface TextIconSettingsItemInteraction {
    fun onItemClick(item: TextIconSettingsItemBindingModel)
}

internal interface HeaderValueSettingsItemInteraction {
    fun onItemClick(item: HeaderValueSettingsItemBindingModel)
}

internal interface HeaderFormattedValueSettingsItemInteraction {
    fun onItemClick(item: HeaderFormattedValueSettingsItemBindingModel<*>)
}

internal interface LogoutInteraction {
    fun onLogoutClick()
}

internal interface DeleteAccountInteraction {
    fun onDeleteAccountClick()
}

internal interface HeaderSwitchSettingsItemInteraction {
    fun onItemToggle(isEnabled: Boolean, item: HeaderSwitchSettingsItemBindingModel)
    fun onItemClick(item: HeaderSwitchSettingsItemBindingModel)
}

internal interface LinkAccountInteraction {
    fun onLinkAmazon()
}
