/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.fakebrushings

import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.game.BrushingCreatorModule
import dagger.Binds
import dagger.Module

@Module(includes = [BrushingCreatorModule::class])
internal abstract class CreateFakeBrushingModule {

    @Binds
    abstract fun bindAppCompatActivity(activity: CreateFakeBrushingActivity): AppCompatActivity
}
