/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.base.legacy

import android.annotation.SuppressLint
import com.kolibree.android.sba.testbrushing.base.ViewAction

@SuppressLint("DeobfuscatedPublicSdkClass")
@Deprecated("Use BaseTestBrushingViewState from MVI package")
abstract class LegacyBaseTestBrushingViewState(open val action: ViewAction)
