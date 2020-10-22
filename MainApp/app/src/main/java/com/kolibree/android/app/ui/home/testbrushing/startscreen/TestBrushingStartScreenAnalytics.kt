/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.testbrushing.startscreen

import com.kolibree.android.app.ui.home.HomeScreenAnalytics

internal object TestBrushingStartScreenAnalytics {

    fun main() = HomeScreenAnalytics.main() + "PopUp_TestBrushing"

    fun start() = main() + "Start"

    fun later() = main() + "Later"

    fun close() = main() + "close"
}
