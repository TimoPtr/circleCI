/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class TakeExternalStoragePictureContractTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val photoFileProvider: PhotoFileProvider = mock()

    private val contract = TakeExternalStoragePictureContract(photoFileProvider)

    @Test
    fun createIntent_returns_intent_with_photoFileProvider_Uri_as_extra() {
        val file: File = mock()
        val expectedPhotoUri: Uri = Uri.EMPTY

        whenever(photoFileProvider.newPhotoUri(file)).thenReturn(expectedPhotoUri)

        val intent = contract.createIntent(context(), file)

        assertEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent.action)

        assertEquals(expectedPhotoUri, intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT))
    }

    /*
    parseResult
     */
    @Test
    fun parseResult_whenActivityResultIsNotOk_returnsNullAndDeletesFileAndClearsStoredPathFile() {
        assertNull(contract.parseResult(Activity.RESULT_CANCELED, Intent()))

        verify(photoFileProvider).deletePhotoFile()
        verify(photoFileProvider).clearStoredPath()
    }

    @Test
    fun parseResult_whenActivityResultIsOk_andPhotoProviderReturnsNull_returnsNullAndDeletesFileAndClearsStoredPathFile() {
        assertNull(photoFileProvider.photoFile())

        assertNull(contract.parseResult(Activity.RESULT_OK, Intent()))

        verify(photoFileProvider).deletePhotoFile()
        verify(photoFileProvider).clearStoredPath()
    }

    @Test
    fun parseResult_whenActivityResultIsOk_andPhotoProviderReturnsNotNull_returnsFileAndClearsStoredPathFile() {
        val file: File = mock()
        whenever(photoFileProvider.photoFile()).thenReturn(file)

        assertEquals(file, contract.parseResult(Activity.RESULT_OK, Intent()))

        verify(photoFileProvider).clearStoredPath()
    }
}
