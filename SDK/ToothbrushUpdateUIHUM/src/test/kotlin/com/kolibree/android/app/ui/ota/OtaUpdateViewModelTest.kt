/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.android.app.Error
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.test.lifecycleTester
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class OtaUpdateViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: OtaUpdateViewModel

    private val serviceProvider: ServiceProvider = mock()

    override fun setup() {
        super.setup()

        viewModel = OtaUpdateViewModel(null, serviceProvider)
    }

    @Test
    fun `when the activity is created service is keep alive`() {
        whenever(serviceProvider.connectStream()).thenReturn(Observable.never())
        viewModel.lifecycleTester().pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        verify(serviceProvider).connectStream()
        assertEquals(1, viewModel.onDestroyDisposables.compositeDisposable.size())
    }

    @Test
    fun `showError update viewState with the error`() {
        val error = Error.from("hello")
        viewModel.showError(error)

        assertEquals(
            SnackbarConfiguration(true, error),
            viewModel.getViewState()?.snackbarConfiguration
        )
    }

    @Test
    fun `showProgress update viewState with boolean value`() {
        viewModel.showProgress(true)

        assertTrue(viewModel.getViewState()!!.progressVisible)
        viewModel.showProgress(false)

        assertFalse(viewModel.getViewState()!!.progressVisible)
    }

    @Test
    fun `progressVisible liveData emit only one item and distinctUntilChanged`() {
        val observer = viewModel.progressVisible.test()

        viewModel.showProgress(false)

        observer.assertValue(false)

        viewModel.showProgress(false)

        observer.assertHistorySize(1)

        viewModel.showProgress(true)

        observer.assertValue(true)
    }

    @Test
    fun `hideError nullify the error`() {
        viewModel.updateViewState {
            copy(snackbarConfiguration = SnackbarConfiguration())
        }

        viewModel.hideError()

        assertNull(viewModel.getViewState()!!.snackbarConfiguration)
    }
}
