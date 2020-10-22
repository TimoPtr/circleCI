/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST
import android.media.MediaMetadataRetriever.METADATA_KEY_TITLE
import android.net.Uri
import android.provider.OpenableColumns
import com.kolibree.android.annotation.VisibleForApp
import java.io.File
import timber.log.Timber

@VisibleForApp
object MusicUtils {

    fun getCoachMusicFileInfo(
        context: Context,
        stringProvider: MusicHintProvider,
        uri: Uri?
    ): String = uri?.let {
        getArtistAndTitle(context, uri)
            ?.let { (artist, title) -> "$artist - $title" }
            ?: getFileName(context, uri)
    } ?: context.getString(stringProvider.provideNoFileString())

    @Suppress("TooGenericExceptionCaught")
    private fun getArtistAndTitle(context: Context, uri: Uri): Pair<String, String>? {
        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(context, uri)
            val artist = retriever.extractMetadata(METADATA_KEY_ARTIST)
            val title = retriever.extractMetadata(METADATA_KEY_TITLE)
            when {
                artist == null || title == null -> null
                else -> artist to title
            }
        } catch (e: Exception) {
            Timber.w(e, "Error while trying to get the artist and title")
            null
        } finally {
            retriever.release()
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? = when (uri.scheme) {
        ContentResolver.SCHEME_FILE -> uri.path?.let { path -> File(path).name }
        ContentResolver.SCHEME_CONTENT -> getCursorContent(context, uri)
        else -> null
    }

    private fun getCursorContent(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, null, null, null, null).use { cursor ->
            cursor?.takeIf(Cursor::moveToFirst)?.run {
                getString(getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }.getOrElse {
        Timber.w(it, "Error while trying to get the name of the file")
        null
    }
}
