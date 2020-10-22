/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.databinding.avatar

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.extensions.sanitizedUrl
import com.kolibree.android.test.BaseInstrumentationTest
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class PicassoAvatarBindingAdapterInstrumentationTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun httpUrl_returnsItself() {
        val url = "http://www.example.com"
        assertEquals(url, url.sanitizedUrl())
    }

    @Test
    fun httpsUrl_returnsItself() {
        val url = "https://www.example.com"
        assertEquals(url, url.sanitizedUrl())
    }

    @Suppress("FunctionNaming")
    @Test
    fun fileUri_returnsItself() {
        val uri = "file:///file/uri"
        assertEquals(uri, uri.sanitizedUrl())
    }

    @Test
    fun nonExistingFile_returnsNull() {
        val nonExistingPath = "/data/random"
        assertNull(nonExistingPath.sanitizedUrl())
    }

    @Test
    fun existingFile_returnsFilePrependedByFile() {
        val tmpFile = File.createTempFile("ran", "dom")

        val returnedSanitizedAvatar = tmpFile.absolutePath.sanitizedUrl()
        assertEquals("file://${tmpFile.absoluteFile}", returnedSanitizedAvatar)
    }
}
