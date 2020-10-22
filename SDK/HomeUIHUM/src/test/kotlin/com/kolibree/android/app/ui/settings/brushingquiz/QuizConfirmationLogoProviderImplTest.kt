/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.brushingquiz

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import junit.framework.TestCase.assertEquals
import org.junit.Test

class QuizConfirmationLogoProviderImplTest : BaseUnitTest() {

    @Test
    fun `use dedicated logo for slow mode`() {
        assertEquals(
            R.drawable.ic_brushing_program_logo1,
            QuizConfirmationLogoProviderImpl().provide(BrushingMode.Slow)
        )
    }

    @Test
    fun `use dedicated logo for regular mode`() {
        assertEquals(
            R.drawable.ic_brushing_program_logo2,
            QuizConfirmationLogoProviderImpl().provide(BrushingMode.Regular)
        )
    }

    @Test
    fun `use dedicated logo for strong mode`() {
        assertEquals(
            R.drawable.ic_brushing_program_logo3,
            QuizConfirmationLogoProviderImpl().provide(BrushingMode.Strong)
        )
    }

    @Test
    fun `Polishing uses strong mode's logo`() {
        assertEquals(
            R.drawable.ic_brushing_program_logo3,
            QuizConfirmationLogoProviderImpl().provide(BrushingMode.Polishing)
        )
    }

    @Test
    fun `UserDefined uses strong mode's logo`() {
        assertEquals(
            R.drawable.ic_brushing_program_logo3,
            QuizConfirmationLogoProviderImpl()
                .provide(BrushingMode.UserDefined)
        )
    }
}
