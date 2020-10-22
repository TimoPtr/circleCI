/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.persistence.BasePreferencesImpl
import java.io.File
import javax.inject.Inject
import javax.inject.Qualifier

internal interface PhotoFileProvider {
    fun newPhotoUri(file: File): Uri

    fun photoFile(): File?

    fun deletePhotoFile()

    fun clearStoredPath()
}

/**
 * Handles the [File] that holds the avatar
 *
 * Launching camera is resource hungry, and some devices will destroy background processes
 * when camera is on foreground.
 *
 * We can't have a photoFile field that survives Fragment restoration because of
 * the following constraints
 * 1. ChangeAvatarViewModel dies (onCleared is invoked), thus there's no way to hold an
 * instance of TakeExternalStoragePictureContract that survives Fragment restoration
 * 2. ActivityResultContract must be re-registered on each Fragment restoration or nothing
 * will happen after coming back from camera
 */
internal class PhotoFileProviderPreferences @Inject constructor(
    private val context: ApplicationContext,
    @FileProviderAuthority private val fileProviderAuthority: String
) : BasePreferencesImpl(context), PhotoFileProvider {

    private companion object {
        private const val PHOTO_FILE_PATH_KEY = "photo_file_key"
    }

    override fun newPhotoUri(file: File): Uri {
        return FileProvider.getUriForFile(context, fileProviderAuthority, file)
            .also { storePhotoFile(file) }
    }

    private fun storePhotoFile(file: File) {
        prefs.edit {
            putString(PHOTO_FILE_PATH_KEY, file.absolutePath)
        }
    }

    override fun photoFile(): File? {
        return prefs.getString(PHOTO_FILE_PATH_KEY, null)?.let { path ->
            File(path)
        }
    }

    override fun deletePhotoFile() {
        if (photoFile()?.delete() == true) {
            clearStoredPath()
        }
    }

    override fun clearStoredPath() {
        prefsEditor.remove(PHOTO_FILE_PATH_KEY).apply()
    }
}

@MustBeDocumented
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@VisibleForApp
annotation class FileProviderAuthority
