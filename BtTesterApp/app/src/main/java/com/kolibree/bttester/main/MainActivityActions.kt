/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.main

import com.kolibree.android.app.base.BaseAction

internal sealed class MainActivityAction : BaseAction

internal object OpenLegacyMainActivity : MainActivityAction()

internal object OpenSingleConnectionActivity : MainActivityAction()

internal object OpenOtaActivity : MainActivityAction()

internal object OpenFreeBrushingActivity : MainActivityAction()
