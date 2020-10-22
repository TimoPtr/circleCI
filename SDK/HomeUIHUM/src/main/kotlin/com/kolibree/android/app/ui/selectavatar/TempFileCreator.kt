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
import android.os.Environment
import com.kolibree.android.failearly.FailEarly
import java.io.File
import java.io.IOException

internal interface TempFileCreator {
    fun createTempFile(): File?
}

internal class ExternalTempFileCreator(private val context: Context) : TempFileCreator {

    override fun createTempFile(): File? =
        try {
            createTempImageFile(context)
        } catch (e: IOException) {
            FailEarly.fail("Unable to create temp image file, reverting to old behaviour", e)
            null
        }

    @Throws(IOException::class)
    private fun createTempImageFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("Image_", ".jpg", storageDir)
    }
}
