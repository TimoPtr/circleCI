/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.extensions

import android.view.View
import androidx.annotation.Keep
import com.kolibree.android.utils.DebouncedClickListener
import org.threeten.bp.Duration

@Keep
fun View.setOnDebouncedClickListener(
    debounceDuration: Duration? = null,
    click: (v: View) -> Unit
) = setOnClickListener(DebouncedClickListener(debounceDuration, click))
