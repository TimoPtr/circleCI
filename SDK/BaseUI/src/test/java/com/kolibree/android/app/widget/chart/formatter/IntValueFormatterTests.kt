/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.chart.formatter

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.widget.chart.formatter.IntValueFormatter
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class IntValueFormatterTests : BaseUnitTest() {
    @Test
    fun `getFormattedValue convert float values to Int to String`() {
        assertEquals("17", IntValueFormatter().getFormattedValue(17.562f))
    }
}
