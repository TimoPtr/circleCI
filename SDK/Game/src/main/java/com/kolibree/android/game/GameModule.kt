/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import com.kolibree.android.game.gameprogress.di.GameProgressModule
import com.kolibree.android.game.shorttask.di.ShortTaskModule
import dagger.Module

@Module(includes = [GameDatabaseModule::class, GameProgressModule::class, ShortTaskModule::class])
object GameModule
