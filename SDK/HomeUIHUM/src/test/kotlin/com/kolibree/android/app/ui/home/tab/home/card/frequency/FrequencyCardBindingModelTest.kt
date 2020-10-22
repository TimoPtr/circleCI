/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import android.view.View
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.card.DynamicCardPosition
import com.kolibree.android.app.ui.home.tab.view.WeekDayLabels
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class FrequencyCardBindingModelTest : BaseUnitTest() {

    @Test
    fun `shows pulsing dot if available`() {
        assertEquals(View.GONE, createInstance(pulsingDotVisible = false).pulsingDotVisibility())
        assertEquals(View.VISIBLE, createInstance(pulsingDotVisible = true).pulsingDotVisibility())
    }

    private fun createInstance(
        pulsingDotVisible: Boolean = false
    ) = FrequencyCardBindingModel(
        FrequencyCardViewState(
            visible = true,
            position = DynamicCardPosition.ZERO,
            weekDayLabels = WeekDayLabels.create(Locale.getDefault()),
            monthsData = emptyList(),
            currentMonthFromNow = 0,
            pulsingDotVisible = pulsingDotVisible,
            profileId = null
        )
    )
}
