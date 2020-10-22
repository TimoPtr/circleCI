/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbox

import android.content.Intent
import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ToolboxViewStateTest : BaseUnitTest() {

    @Test
    fun `should create view state from configuration`() {
        val viewState = ToolboxViewState.fromConfiguration(testConfiguration)

        assertTrue(viewState.toolboxVisible)
        assertEquals(testConfiguration.iconRes, viewState.iconRes)
        assertEquals(testConfiguration.subTitle, viewState.subTitle)
        assertEquals(testConfiguration.title, viewState.title)
        assertEquals(testConfiguration.body, viewState.body)
        assertEquals(testConfiguration.detailsButton!!.text, viewState.detailsButton)
        assertEquals(testConfiguration.confirmButton!!.text, viewState.confirmButton)
        assertEquals(testConfiguration.titleTextAppearance, viewState.titleTextAppearance)
    }

    private val testConfiguration = ToolboxConfiguration(
        analyticsName = "test_analytics",
        iconRes = 1,
        subTitle = "mock subtitle",
        title = "mock title",
        body = "mock body",
        detailsButton = ToolboxConfiguration.Button(
            text = "mock details button",
            analytics = "test_details_button",
            intent = Intent(Intent.ACTION_RUN)
        ),
        confirmButton = ToolboxConfiguration.Button(
            text = "mock confirm button",
            analytics = "test_details_button",
            intent = Intent(Intent.ACTION_ANSWER)
        )
    )
}
