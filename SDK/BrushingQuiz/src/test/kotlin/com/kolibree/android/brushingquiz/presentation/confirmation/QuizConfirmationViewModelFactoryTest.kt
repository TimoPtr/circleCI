/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushingquiz.presentation.confirmation

import com.google.common.base.Optional
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test

class QuizConfirmationViewModelFactoryTest : BaseUnitTest() {

    @Test
    fun `if logo provider is not null, inject it to VM`() {
        val logoProvider: QuizConfirmationLogoProvider = mock()
        val factory = createFactory(logoProvider = logoProvider)

        val vm = factory.create(QuizConfirmationViewModel::class.java)

        assertEquals(logoProvider, vm.logoProvider)
    }

    @Test
    fun `if logo provider is null, inject default implementation`() {
        val factory = createFactory(logoProvider = null)

        val vm = factory.create(QuizConfirmationViewModel::class.java)

        assertEquals(DefaultQuizConfirmationLogoProvider, vm.logoProvider)
    }

    private fun createFactory(
        mode: BrushingMode = BrushingMode.Regular,
        logoProvider: QuizConfirmationLogoProvider? = null
    ) = QuizConfirmationViewModel.Factory(
        mode,
        mock(),
        mock(),
        Optional.fromNullable(logoProvider)
    )
}
