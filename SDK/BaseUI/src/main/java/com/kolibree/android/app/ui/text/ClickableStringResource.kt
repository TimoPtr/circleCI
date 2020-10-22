/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.text

import android.view.View
import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
data class ClickableStringResource(
    @StringRes val stringResource: Int,
    val textPaintModifiers: TextPaintModifiers? = null,
    val onClickListener: View.OnClickListener
)
