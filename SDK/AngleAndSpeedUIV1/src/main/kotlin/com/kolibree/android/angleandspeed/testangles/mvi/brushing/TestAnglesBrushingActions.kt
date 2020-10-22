/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing

import com.kolibree.android.angleandspeed.testangles.mvi.TestAnglesAction

internal sealed class TestAnglesBrushingAction : TestAnglesAction

internal object OpenIncisorBrushing : TestAnglesBrushingAction()

internal object OpenConfirmation : TestAnglesBrushingAction()
