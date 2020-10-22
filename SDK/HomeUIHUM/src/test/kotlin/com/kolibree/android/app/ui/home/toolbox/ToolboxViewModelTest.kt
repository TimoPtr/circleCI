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
import androidx.lifecycle.LiveData
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.navigation.HomeNavigator
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ToolboxViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: ToolboxViewModel

    private val navigator: HomeNavigator = mock()
    private val factory: ToolboxConfiguration.Factory = mock()

    override fun setup() {
        super.setup()
        viewModel = ToolboxViewModel(null, navigator, factory)
    }

    private val viewState: ToolboxViewState
        get() = viewModel.getViewState()!!

    @Test
    fun `should start in hidden state`() {
        val isVisible = viewState.toolboxVisible
        assertFalse(isVisible)
    }

    @Test
    fun `should display configuration`() {
        viewModel.show(testConfiguration)

        val expectedViewState = ToolboxViewState.fromConfiguration(testConfiguration)
        assertEquals(expectedViewState, viewState)
    }

    @Test
    fun `should hide after button click`() {
        viewModel.show(testConfiguration)
        assertTrue(viewState.toolboxVisible)

        viewModel.onDetailsClick()
        assertFalse(viewState.toolboxVisible)

        viewModel.show(testConfiguration)
        assertTrue(viewState.toolboxVisible)

        viewModel.onConfirmClick()
        assertFalse(viewState.toolboxVisible)
    }

    @Test
    fun `should hide after back pressed`() {
        var wasHandled = viewModel.onBackPressed()
        assertFalse(wasHandled)

        viewModel.show(testConfiguration)

        wasHandled = viewModel.onBackPressed()
        assertTrue(wasHandled)
        assertFalse(viewState.toolboxVisible)
    }

    @Test
    fun `should navigate when intent available`() {
        viewModel.show(testConfiguration)
        viewModel.onDetailsClick()

        verify(navigator).navigateTo(any())
        clearInvocations(navigator)

        viewModel.show(testConfiguration)
        viewModel.onConfirmClick()

        verify(navigator).navigateTo(any())
    }

    @Test
    fun `shouldn't navigate when intent not available`() {
        val configuration = testConfiguration.copy(
            detailsButton = testConfiguration.detailsButton!!.copy(intent = null),
            confirmButton = testConfiguration.confirmButton!!.copy(intent = null)
        )

        viewModel.show(configuration)
        viewModel.onDetailsClick()

        verify(navigator, never()).navigateTo(any())

        viewModel.show(configuration)
        viewModel.onConfirmClick()

        verify(navigator, never()).navigateTo(any())
    }

    @Test
    fun `should set icon visibility`() {
        viewModel.iconVisible.assertValue(
            forConfiguration = testConfiguration,
            expected = true
        )

        viewModel.iconVisible.assertValue(
            forConfiguration = testConfiguration.copy(iconRes = null),
            expected = false
        )
    }

    @Test
    fun `should set subtitle visibility`() {
        viewModel.subTitleVisible.assertValue(
            forConfiguration = testConfiguration,
            expected = true
        )

        viewModel.subTitleVisible.assertValue(
            forConfiguration = testConfiguration.copy(subTitle = null),
            expected = false
        )
    }

    @Test
    fun `should set title visibility`() {
        viewModel.titleVisible.assertValue(
            forConfiguration = testConfiguration,
            expected = true
        )

        viewModel.titleVisible.assertValue(
            forConfiguration = testConfiguration.copy(title = null),
            expected = false
        )
    }

    @Test
    fun `should set body visibility`() {
        viewModel.bodyVisible.assertValue(
            forConfiguration = testConfiguration,
            expected = true
        )

        viewModel.bodyVisible.assertValue(
            forConfiguration = testConfiguration.copy(body = null),
            expected = false
        )
    }

    @Test
    fun `should set details button visibility`() {
        viewModel.detailsButtonVisible.assertValue(
            forConfiguration = testConfiguration,
            expected = true
        )

        viewModel.detailsButtonVisible.assertValue(
            forConfiguration = testConfiguration.copy(detailsButton = null),
            expected = false
        )
    }

    @Test
    fun `should set pulsing dot visibility`() {
        viewModel.pulsingDotVisible.assertValue(
            forConfiguration = testConfiguration,
            expected = true
        )

        viewModel.pulsingDotVisible.assertValue(
            forConfiguration = testConfiguration.copy(pulsingDotVisible = false),
            expected = false
        )
    }

    @Test
    fun `should set confirm button visibility`() {
        viewModel.confirmButtonVisible.assertValue(
            forConfiguration = testConfiguration,
            expected = true
        )

        viewModel.confirmButtonVisible.assertValue(
            forConfiguration = testConfiguration.copy(confirmButton = null),
            expected = false
        )
    }

    private fun LiveData<Boolean>.assertValue(
        forConfiguration: ToolboxConfiguration,
        expected: Boolean
    ) {
        val observer = test()
        viewModel.show(forConfiguration)
        assertEquals(expected, observer.value())
    }

    private val testConfiguration = ToolboxConfiguration(
        analyticsName = "test_analytics",
        iconRes = 1,
        subTitle = "mock subtitle",
        title = "mock title",
        body = "mock body",
        pulsingDotVisible = true,
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
