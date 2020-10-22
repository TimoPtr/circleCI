/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.test.BaseInstrumentationTest
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PhotoFileProviderPreferencesTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val fileCreator = ExternalTempFileCreator(InstrumentationRegistry.getInstrumentation().context)

    private val provider =
        PhotoFileProviderPreferences(
            ApplicationContext(context()),
            "com.kolibree.android.homeui.hum.test.fileprovider"
        )

    private var file: File? = null

    override fun setUp() {
        super.setUp()

        provider.clearStoredPath()
    }

    override fun tearDown() {
        super.tearDown()

        file?.delete()
        provider.clearStoredPath()
    }

    @Test
    fun newPhotoUri_returnsUriAndStoresFilePath() {
        val file = file()

        assertNotNull(provider.newPhotoUri(file))

        assertEquals(file, provider.photoFile())
    }

    @Test
    fun whenClearStoredPath_photoFileReturnsNull() {
        val file = file()

        assertNotNull(provider.newPhotoUri(file))

        provider.clearStoredPath()

        assertNull(provider.photoFile())
    }

    @Test
    fun whenDeletePhotoFile_photoFileReturnsNull() {
        val file = file()

        assertNotNull(provider.newPhotoUri(file))

        provider.deletePhotoFile()

        assertNull(provider.photoFile())
    }

    @Test
    fun whenDeletePhotoFile_photoFileIsDeleted() {
        val file = file()

        assertNotNull(provider.newPhotoUri(file))

        assertTrue(file.exists())

        provider.deletePhotoFile()

        assertFalse(file.exists())
    }

    @Test
    fun whenNewPhotoUriWasNeverInvoked_photoFileIsNull() {
        assertNull(provider.photoFile())
    }

    private fun file(): File = fileCreator.createTempFile()!!.also { file = it }
}
