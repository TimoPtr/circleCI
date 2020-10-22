/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.graphics.Bitmap
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq

@ExperimentalCoroutinesApi
class ImageCaptorTest : BaseUnitTest() {

    private val contract: TakeExternalStoragePictureContract = mock()
    private val tempFileCreator: TempFileCreator = mock()
    private val activityResultCaller: ActivityResultCaller = mock()
    private val voidActivityResultLauncher: ActivityResultLauncher<Void> = mock()
    private val fileActivityResultLauncher: ActivityResultLauncher<File> = mock()
    private val file: File = mock()

    private val testCoroutineScope = TestCoroutineScope()

    private val fakeCallback: TakePictureCallback = {}

    private val imageCaptor = ImageCaptor(tempFileCreator)

    @Before
    fun doSetup() {
        whenever(
            activityResultCaller.registerForActivityResult(
                any(ActivityResultContracts.TakePicturePreview::class.java),
                any()
            )
        ).thenReturn(voidActivityResultLauncher)

        whenever(
            activityResultCaller.registerForActivityResult(
                eq(contract),
                any()
            )
        ).thenReturn(fileActivityResultLauncher)
    }

    /*
    prepareCaptureBitmap
     */

    @Test
    fun `preferred method is prepared`() {
        invokePrepareCapture()

        verify(activityResultCaller)
            .registerForActivityResult(
                eq(contract),
                any()
            )
    }

    @Test
    fun `fallback method is prepared`() {
        invokePrepareCapture()

        verify(activityResultCaller)
            .registerForActivityResult(
                any(ActivityResultContracts.TakePicturePreview::class.java),
                any()
            )
    }

    /*
    captureBitmap
     */

    @Test
    fun `when temp file can be created then preferred capture is used`() {
        whenever(tempFileCreator.createTempFile()).thenReturn(file)

        invokePrepareCapture()

        imageCaptor.captureBitmap()

        verify(fileActivityResultLauncher).launch(file)

        verifyNoMoreInteractions(voidActivityResultLauncher)
    }

    @Test
    fun `when temp file cannot be created then fallback capture is used`() {
        whenever(tempFileCreator.createTempFile()).thenReturn(null)

        invokePrepareCapture()

        imageCaptor.captureBitmap()

        verifyNoMoreInteractions(fileActivityResultLauncher)

        verify(voidActivityResultLauncher).launch(null)
    }

    /*
    utils
     */

    private fun invokePrepareCapture() {
        imageCaptor.prepareCaptureBitmap(
            coroutineScope = testCoroutineScope,
            activityResultCaller = activityResultCaller,
            contract = contract,
            callback = fakeCallback
        )
    }
}

typealias TakePictureCallback = (Bitmap?) -> Unit
