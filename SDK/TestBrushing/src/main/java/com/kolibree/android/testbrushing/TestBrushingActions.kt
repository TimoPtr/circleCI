/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseAction

@VisibleForApp
sealed class TestBrushingActions : BaseAction {

    @VisibleForApp
    data class ShowError(val error: Error) : TestBrushingActions()
}
