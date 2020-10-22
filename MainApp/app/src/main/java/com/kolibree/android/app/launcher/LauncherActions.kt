/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.launcher

import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.update.AppUpdateRequested

internal sealed class LauncherActions : BaseAction {
    class OnUpdateNeeded(val request: AppUpdateRequested) : LauncherActions()
}
