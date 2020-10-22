/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.ui.connect

import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.extention.showInBrowser

internal class AmazonDashConnectNavigator : BaseNavigator<AmazonDashConnectActivity>() {

    fun finish() = withOwner { finish() }

    fun open(link: String) = withOwner {
        showInBrowser(link)
    }
}
