/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.toothbrush.battery.di.BatteryLevelCoreModule
import dagger.Module

@Module(includes = [BatteryLevelCoreModule::class])
class EspressoBatteryLevelModule
