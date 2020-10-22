/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core

import android.content.Context
import android.net.Uri
import android.webkit.URLUtil
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.network.utils.FileDownloader
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import timber.log.Timber

private var cacheContext = newSingleThreadContext("LocalAvatarCacheContext")
private const val DOWNLOAD_MAX_RETRY = 3

/**
 * AvatarCache implementation with Picasso
 */
@VisibleForApp
class LocalAvatarCache @Inject constructor(
    private val context: Context,
    private val fileDownloader: FileDownloader,
    private val avatarCacheWarmUp: AvatarCacheWarmUp
) :
    AvatarCache {

    @VisibleForTesting
    var cacheJob: Job? = null

    /**
     * Caches the avatar so that we can later ignore if the link has expired
     */
    @WorkerThread
    override fun cache(profileId: Long, pictureUrl: String?, pictureLastModifier: String?) {
        if (pictureUrl != null && URLUtil.isNetworkUrl(pictureUrl)) {
            cacheJob = GlobalScope.launch(cacheContext) {
                val cacheFile = getAvatarFile(context, profileId, pictureUrl, pictureLastModifier)

                if (cacheFile.exists()) {
                    warmUpCache(cacheFile)
                    return@launch
                }

                deleteCachesForProfile(profileId)
                val tmpFile = downloadPicture(pictureUrl, cacheFile.name)
                copyDownloadedFileToCacheFileAndDelete(tmpFile, cacheFile)
                warmUpCache(cacheFile)
            }
        }
    }

    // Delete existing cached avatars for the same profileId.
    @VisibleForTesting
    fun deleteCachesForProfile(profileId: Long) {
        getAvatarDir(context)
            .listFiles { _, name -> name.startsWith(profileId.toString()) }
            ?.map { it.delete() }
    }

    @VisibleForTesting
    fun downloadPicture(pictureUrl: String, tmpFileName: String): File? {
        var tmpFile: File? = null
        for (i in 1..DOWNLOAD_MAX_RETRY) {
            if (tmpFile != null && tmpFile.exists()) {
                break
            }
            tmpFile = try {
                fileDownloader.download(pictureUrl, tmpFileName)
            } catch (e: IOException) {
                Timber.i(e, "download picture failed for $i times")
                null
            }
        }
        return tmpFile
    }

    @VisibleForTesting
    fun copyDownloadedFileToCacheFileAndDelete(tmpFile: File?, cacheFile: File) {
        if (tmpFile != null && tmpFile.exists()) {
            try {
                tmpFile.copyTo(cacheFile, true)
            } finally {
                tmpFile.delete()
            }
        }
    }

    @VisibleForTesting
    fun warmUpCache(file: File) = avatarCacheWarmUp.cache(Uri.fromFile(file).toString())

    override fun getAvatarUrl(profile: IProfile): String? = getAvatarUrl(context, profile)

    @VisibleForApp
    companion object {
        private const val AVATAR_DIR = "avatar"

        fun getAvatarUrl(context: Context, profile: IProfile): String? {
            val pictureUrl = profile.pictureUrl
            if (pictureUrl != null && URLUtil.isNetworkUrl(pictureUrl)) {
                val avatarFile = getAvatarFile(context, profile.id, pictureUrl, profile.pictureLastModifier)
                if (avatarFile.exists()) {
                    return Uri.fromFile(avatarFile).toString()
                }
            }
            return pictureUrl
        }

        @VisibleForTesting
        fun getAvatarDir(context: Context): File {
            return File(context.filesDir, AVATAR_DIR)
        }

        @VisibleForTesting
        fun getAvatarFile(
            context: Context,
            profileId: Long,
            pictureUrl: String,
            pictureLastModifier: String?
        ): File {
            val suffix = if (pictureLastModifier != null && pictureLastModifier.isNotBlank()) {
                pictureLastModifier.filter { it.isLetterOrDigit() }
            } else {
                pictureUrl.hashCode().absoluteValue.toString()
            }
            return File(
                getAvatarDir(context),
                "${profileId}_$suffix"
            )
        }
    }
}
