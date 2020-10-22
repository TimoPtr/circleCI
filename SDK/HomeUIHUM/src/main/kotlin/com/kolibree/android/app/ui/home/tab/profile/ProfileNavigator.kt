/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile

import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.ui.selectavatar.SelectAvatarDialogFragment

internal interface ProfileNavigator {
    fun showChooseAvatar()
}

internal class ProfileNavigatorViewModel : BaseNavigator<ProfileFragment>(), ProfileNavigator {

    override fun showChooseAvatar() {
        withOwner {
            SelectAvatarDialogFragment.showIfNotPresent(childFragmentManager)
        }
    }
}
