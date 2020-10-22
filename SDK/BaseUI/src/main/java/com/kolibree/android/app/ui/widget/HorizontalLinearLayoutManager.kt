/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager

@Keep
class HorizontalLinearLayoutManager(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    theme: Int
) : LinearLayoutManager(context, HORIZONTAL, false)
