/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import androidx.navigation.findNavController
import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.app.utils.navigateSafe

internal class OtaUpdateNavigator : BaseNavigator<OtaUpdateActivity>() {
    fun finishScreen() {
        withOwner {
            finish()
        }
    }

    fun navigatesToInProgress() {
        withOwner {
            findNavController(R.id.nav_host_fragment)
                .navigateSafe(R.id.action_fragment_start_ota_to_fragment_in_progress_ota)
        }
    }
}
