/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.base

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseAction

/** Checkup [BaseAction] implementation */
@VisibleForApp
sealed class CheckupActions : BaseAction {

    @VisibleForApp
    object FinishOk : CheckupActions()

    @VisibleForApp
    object FinishCancel : CheckupActions()

    @VisibleForApp
    object ConfirmDeletion : CheckupActions()
}
