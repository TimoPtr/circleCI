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
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import java.io.File
import javax.inject.Inject

internal class TakeExternalStoragePictureContract @Inject constructor(
    private val photoFileProvider: PhotoFileProvider
) : ActivityResultContract<File, File>() {

    override fun createIntent(context: Context, file: File): Intent {
        val photoUri = photoFileProvider.newPhotoUri(file)

        return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): File? {
        try {
            val photoFile = photoFileProvider.photoFile()

            return if (resultCode == Activity.RESULT_OK && photoFile != null) {
                photoFile
            } else {
                photoFileProvider.deletePhotoFile()

                null
            }
        } finally {
            photoFileProvider.clearStoredPath()
        }
    }
}
