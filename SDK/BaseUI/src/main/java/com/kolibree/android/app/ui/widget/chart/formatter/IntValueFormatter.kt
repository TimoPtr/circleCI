/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget.chart.formatter

import com.github.mikephil.charting.formatter.ValueFormatter
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
class IntValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String = value.toInt().toString()

    @VisibleForApp
    companion object {
        @JvmStatic
        fun create(): IntValueFormatter = IntValueFormatter()
    }
}
