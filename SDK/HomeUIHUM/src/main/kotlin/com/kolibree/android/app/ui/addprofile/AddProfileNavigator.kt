/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.addprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.tracker.Analytics
import javax.inject.Inject

internal class AddProfileNavigator : BaseNavigator<AddProfileActivity>() {

    fun closeScreen() = withOwner {
        Analytics.send(AddProfileAnalytics.goBack())
        finish()
    }

    internal class Factory @Inject constructor() : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AddProfileNavigator() as T
    }
}
