/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.ui.dialog.LostConnectionDialog
import com.kolibree.android.sdk.disconnection.LostConnectionHandlerModule
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module(includes = [LostConnectionHandlerModule::class, LostConnectionDialogModule::class])
abstract class LostConnectionModule

@Module
abstract class LostConnectionDialogModule {

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun contributeLostConnectionDialog(): LostConnectionDialog

    companion object {
        @Provides
        fun providesLostConnectionDialogProgressIndicatorAnimation(): Animation {
            return AlphaAnimation(START_ALPHA, END_ALPHA).apply {
                duration = LostConnectionDialog.FADE_IN_DURATION
                repeatMode = AlphaAnimation.INFINITE
                repeatCount = AlphaAnimation.INFINITE
            }
        }
    }
}

private const val START_ALPHA = 1f
private const val END_ALPHA = 0.1f
