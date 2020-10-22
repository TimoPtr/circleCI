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
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.exifinterface.media.ExifInterface
import com.kolibree.android.failearly.FailEarly
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ImageCaptor @Inject constructor(private val tempFileCreator: TempFileCreator) {
    private var preferredLauncher: ActivityResultLauncher<File>? = null
    private var fallbackLauncher: ActivityResultLauncher<Void>? = null

    fun prepareCaptureBitmap(
        coroutineScope: CoroutineScope,
        activityResultCaller: ActivityResultCaller,
        contract: TakeExternalStoragePictureContract,
        callback: (Bitmap?) -> Unit
    ) {
        preparePreferred(
            activityResultCaller = activityResultCaller,
            contract = contract,
            callback = ActivityResultCallback { file ->
                coroutineScope.launch {
                    callback(correctImage(file))
                }
            }
        )

        prepareFallback(activityResultCaller, ActivityResultCallback { bitmap -> callback(bitmap) })
    }

    private fun preparePreferred(
        activityResultCaller: ActivityResultCaller,
        contract: TakeExternalStoragePictureContract,
        callback: ActivityResultCallback<File?>
    ) {
        preferredLauncher = activityResultCaller.registerForActivityResult(
            contract,
            callback
        )
    }

    private fun prepareFallback(
        activityResultCaller: ActivityResultCaller,
        callback: ActivityResultCallback<Bitmap>
    ) {
        fallbackLauncher = activityResultCaller.registerForActivityResult(
            ActivityResultContracts.TakePicturePreview(),
            callback
        )
    }

    fun captureBitmap() {
        if (!launchPreferred()) {
            launchFallback()
        }
    }

    private fun launchPreferred(): Boolean {
        val imageFile = tempFileCreator.createTempFile()

        if (imageFile != null) {
            preferredLauncher?.let { launcher ->
                launcher.launch(imageFile)

                return true
            }
        }

        return false
    }

    private fun launchFallback() {
        fallbackLauncher?.launch(null)
    }

    private suspend fun correctImage(file: File?): Bitmap? {
        return file?.let { bitmapFile ->
            withContext(Dispatchers.IO) {
                val exif = loadExif(bitmapFile)
                val source = loadDownscaledBitmap(bitmapFile, exif)
                exif?.createCorrectionMatrix()?.let {
                    try {
                        source?.transform(it)
                    } catch (e: OutOfMemoryError) {
                        FailEarly.fail(
                            "OutOfMemoryError transforming bitmap, returning raw bitmap",
                            e
                        )
                        null
                    } catch (e: IllegalArgumentException) {
                        FailEarly.fail(
                            "IllegalArgumentException transforming bitmap, returning raw bitmap",
                            e
                        )
                        null
                    }
                } ?: source
            }
        }
    }

    private fun loadExif(file: File): ExifInterface? {
        return try {
            ExifInterface(file)
        } catch (e: IOException) {
            FailEarly.fail("Error reading EXIF data. No transform will be attempted", e)
            null
        }
    }

    private fun loadDownscaledBitmap(file: File, exif: ExifInterface?): Bitmap? {
        val bitmapOptions = BitmapFactory.Options().apply {
            val width = exif?.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0) ?: 0
            val height = exif?.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0) ?: 0
            inSampleSize = if (width > LARGE_DIMENSION || height > LARGE_DIMENSION)
                LARGE_SCALING else SMALL_SCALING
        }
        return try {
            BitmapFactory.decodeFile(file.absolutePath, bitmapOptions)
        } catch (e: IOException) {
            FailEarly.fail("Error decoding bitmap from file. An error will be shown.", e)
            null
        }
    }

    private fun ExifInterface.createCorrectionMatrix(): Matrix =
        Matrix().also { matrix ->
            matrix.setRotate(rotationDegrees.toFloat())
            if (isFlipped) {
                matrix.postScale(-1f, 1f)
            }
        }

    private fun Bitmap.transform(matrix: Matrix) =
        if (matrix.isIdentity) {
            this
        } else {
            Bitmap.createBitmap(this, 0, 0, width, height, matrix, true).also {
                recycle()
            }
        }

    companion object {
        private const val LARGE_DIMENSION = 2000
        private const val LARGE_SCALING = 8
        private const val SMALL_SCALING = 4
    }
}
