/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.ui.navigator

import com.kolibree.android.app.base.BaseNavigator
import com.kolibree.android.guidedbrushing.ui.GuidedBrushingTipsActivity

internal class GuidedBrushingTipsNavigator : BaseNavigator<GuidedBrushingTipsActivity>() {

    fun finish() = withOwner { finish() }
}
