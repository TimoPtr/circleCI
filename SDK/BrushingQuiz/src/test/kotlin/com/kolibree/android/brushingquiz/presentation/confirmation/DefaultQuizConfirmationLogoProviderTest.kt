/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.brushingquiz.R
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import junit.framework.TestCase.assertEquals
import org.junit.Test

class DefaultQuizConfirmationLogoProviderTest : BaseUnitTest() {

    @Test
    fun `use the same logo for each mode`() {
        BrushingMode.values().forEach { brushingMode ->
            assertEquals(
                R.drawable.ic_brushing_program_logo,
                DefaultQuizConfirmationLogoProvider.provide(brushingMode)
            )
        }
    }
}
