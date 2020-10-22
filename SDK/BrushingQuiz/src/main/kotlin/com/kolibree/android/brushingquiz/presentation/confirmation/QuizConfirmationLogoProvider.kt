/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode

@Keep
interface QuizConfirmationLogoProvider {

    @DrawableRes
    fun provide(brushingMode: BrushingMode): Int
}

internal object DefaultQuizConfirmationLogoProvider : QuizConfirmationLogoProvider {

    @DrawableRes
    override fun provide(brushingMode: BrushingMode): Int = R.drawable.ic_brushing_program_logo
}
