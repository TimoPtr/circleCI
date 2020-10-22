/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.brushingquiz

import androidx.annotation.DrawableRes
import com.kolibree.android.brushingquiz.presentation.confirmation.QuizConfirmationLogoProvider
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import javax.inject.Inject

internal class QuizConfirmationLogoProviderImpl
@Inject constructor() : QuizConfirmationLogoProvider {

    @DrawableRes
    override fun provide(brushingMode: BrushingMode): Int = when (brushingMode) {
        BrushingMode.Slow -> R.drawable.ic_brushing_program_logo1
        BrushingMode.Regular -> R.drawable.ic_brushing_program_logo2
        BrushingMode.Strong -> R.drawable.ic_brushing_program_logo3
        BrushingMode.Polishing, BrushingMode.UserDefined -> R.drawable.ic_brushing_program_logo3
    }
}
