/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.extention.openAppNotificationSettings
import javax.inject.Inject

internal class NotificationsNavigator : BaseNavigator<NotificationsActivity>() {

    fun showNotificationsDisabledDialog() = withOwner {
        this.showNotificationsDisabledDialog()
    }

    fun openAppNotificationSettings() = withOwner {
        this.openAppNotificationSettings()
    }

    fun closeScreen() = withOwner {
        finish()
    }

    class Factory @Inject constructor() : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NotificationsNavigator() as T
    }
}
