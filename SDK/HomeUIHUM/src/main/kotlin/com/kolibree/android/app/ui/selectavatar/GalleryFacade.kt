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
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.utils.AvatarUtils
import com.kolibree.sdkws.Constants
import com.kolibree.sdkws.utils.ApiSDKUtils
import java.io.InputStream
import javax.inject.Inject
import okhttp3.internal.Util
import timber.log.Timber

/*
copied from ChangeAvatarActivity
 */
internal class GalleryFacade
@Inject constructor(
    private val context: ApplicationContext,
    private val apiSDKUtils: ApiSDKUtils
) {

    // Extract bitmap from gallery uri
    fun loadFromGallery(uri: Uri): Bitmap? {
        var stream: InputStream? = null
        return try {
            // Get image orientation
            stream = context.contentResolver.openInputStream(uri)
            if (stream == null) {
                Timber.e("Could not open gallery picture with Uri: %s", uri.toString())
                return null
            }
            val ei = ExifInterface(stream)
            val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            // Load image
            val fileDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r")

            // get source size
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            if (fileDescriptor != null) {
                BitmapFactory.decodeFileDescriptor(
                    fileDescriptor.fileDescriptor,
                    null,
                    options
                )
            }
            val imageHeight = options.outHeight
            val imageWidth = options.outWidth

            // Calculate sample size
            var inSampleSize = 1
            if (imageHeight > Constants.AVATAR_SIZE_PX || imageWidth > Constants.AVATAR_SIZE_PX) {
                val halfHeight = imageHeight / 2
                val halfWidth = imageWidth / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize > Constants.AVATAR_SIZE_PX &&
                    halfWidth / inSampleSize > Constants.AVATAR_SIZE_PX
                ) {
                    inSampleSize *= 2
                }
            }
            val loadOptions = BitmapFactory.Options()
            loadOptions.inSampleSize = inSampleSize
            if (fileDescriptor == null) {
                return null
            }
            val bitmap = BitmapFactory.decodeFileDescriptor(
                fileDescriptor.fileDescriptor,
                null,
                loadOptions
            )
            val avatar: Bitmap? = apiSDKUtils.kolibrizeAvatar(bitmap)
            bitmap.recycle()

            // Finally rotate and flip it if needed
            avatar?.let { it -> AvatarUtils.fixOrientation(it, orientation) }
        } catch (e: Exception) {
            Timber.e(e)
            null
        } finally {
            Util.closeQuietly(stream)
        }
    }
}
