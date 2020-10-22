/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.testbrushing.di

import com.kolibree.android.app.disconnection.LostConnectionModule
import dagger.Module

@Module(
    includes = [
        TestBrushingActivityLogicModule::class,
        LostConnectionModule::class
    ]
)
abstract class TestBrushingActivityModule
