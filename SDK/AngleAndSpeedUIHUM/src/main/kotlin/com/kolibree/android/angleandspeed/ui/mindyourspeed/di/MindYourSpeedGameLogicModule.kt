/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.ui.mindyourspeed.di

import android.view.ContextThemeWrapper
import com.kolibree.android.angleandspeed.common.logic.AngleAndSpeedModule
import com.kolibree.android.angleandspeed.ui.R
import com.kolibree.android.angleandspeed.ui.mindyourspeed.MindYourSpeedActivity
import com.kolibree.android.app.disconnection.LostConnectionDialogController
import com.kolibree.android.game.BrushingCreatorModule
import com.kolibree.android.game.bi.AvroCreatorModule
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import dagger.Module
import dagger.Provides

@Module(
    includes = [
        AngleAndSpeedModule::class,
        MindYourSpeedLostConnectionDialogModule::class,
        RnnWeightProviderModule::class,
        KpiSpeedProviderModule::class,
        AvroCreatorModule::class,
        BrushingCreatorModule::class
    ]
)
object MindYourSpeedGameLogicModule

@Module
object MindYourSpeedLostConnectionDialogModule {

    @Provides
    internal fun provideHumLostConnectionDialogController(
        activity: MindYourSpeedActivity
    ): LostConnectionDialogController {
        return LostConnectionDialogController(
            ContextThemeWrapper(activity, R.style.ThemeOverlay_Dialog_Inverse),
            activity
        )
    }
}
