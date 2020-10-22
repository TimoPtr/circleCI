/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.angleandspeed.ui.mindyourspeed.di.MindYourSpeedModule
import dagger.Module

@Module(includes = [MindYourSpeedModule::class])
abstract class EspressoMindYourSpeedModule
