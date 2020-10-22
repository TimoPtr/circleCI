/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import com.kolibree.android.app.base.BaseAction

internal sealed class ToothbrushSettingsActions : BaseAction {
    object SomethingWrongHappened : ToothbrushSettingsActions()
    data class ShowEditBrushNameDialog(val currentName: String) : ToothbrushSettingsActions()
    object ShowForgetToothbrushDialog : ToothbrushSettingsActions()
    data class ConnectNewToothbrush(val toothbrushName: String) : ToothbrushSettingsActions()
}
