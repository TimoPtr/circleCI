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
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.network.utils.FileDownloader
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.io.File
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

class LocalAvatarCacheTest : BaseInstrumentationTest() {

    val fileDownloader: FileDownloader = mock()
    val avatarCacheWarmUp: AvatarCacheWarmUp = mock()
    val avatarCache = LocalAvatarCache(context(), fileDownloader, avatarCacheWarmUp)

    lateinit var profile: Profile

    @Before
    override fun setUp() {
        super.setUp()
        profile = ProfileBuilder.create()
            .withId(123456L)
            .withPictureUrl("https://test.picture.url")
            .withPictureLastModifier("2020-06-10T06:23:26.190095")
            .build()

        mockFileDownload()
    }

    @Test
    fun getAvatarDir_returnsAvatarDirctory() {
        assertEquals(
            LocalAvatarCache.getAvatarDir(context()),
            File(context().filesDir, "avatar")
        )
    }

    @Test
    fun getAvatarFile_whenPictureLastModifierIsNotEmpty_returnsFileNameComposedByPictureLastModifier() {
        assertEquals(
            LocalAvatarCache.getAvatarFile(
                context(),
                profile.id,
                profile.pictureUrl!!,
                profile.pictureLastModifier
            ).name,
            "123456_20200610T062326190095"
        )
    }

    @Test
    fun getAvatarFile_whenPictureLastModifierIsEmptyOrNull_returnsFileNameComposedByPictureUrl() {
        assertEquals(
            LocalAvatarCache.getAvatarFile(
                context(),
                profile.id,
                profile.pictureUrl!!,
                ""
            ).name,
            "123456_${profile.pictureUrl.hashCode().absoluteValue}"
        )
        assertEquals(
            LocalAvatarCache.getAvatarFile(
                context(),
                profile.id,
                profile.pictureUrl!!,
                null
            ).name,
            "123456_${profile.pictureUrl.hashCode().absoluteValue}"
        )
    }

    @Test
    fun getAvatarUrl_whenAvatarFileNotExists_returnsPictureUrl() {
        val avatarFile = getAvatarFile(profile)
        avatarFile.delete()

        assertEquals(
            LocalAvatarCache.getAvatarUrl(context(), profile),
            profile.pictureUrl
        )
    }

    @Test
    fun getAvatarUrl_whenAvatarFileExists_returnsPictureUrl() {
        val avatarFile = getAvatarFile(profile)
        avatarFile.mkdirs()
        avatarFile.createNewFile()

        assertEquals(
            LocalAvatarCache.getAvatarUrl(context(), profile),
            Uri.fromFile(avatarFile).toString()
        )
    }

    @Test
    fun deleteCachesForProfile_deleteAllProfileAvatarsButNotOthers() {
        val avatarDir = LocalAvatarCache.getAvatarDir(context())
        avatarDir.mkdirs()

        val avatarFile1 = File(avatarDir, "${profile.id}1234")
        val avatarFile2 = File(avatarDir, "${profile.id}_1234")
        val avatarFile3 = File(avatarDir, "1234")

        avatarFile1.createNewFile()
        avatarFile2.createNewFile()
        avatarFile3.createNewFile()

        avatarCache.deleteCachesForProfile(profile.id)

        assertFalse(avatarFile1.exists())
        assertFalse(avatarFile2.exists())
        assertTrue(avatarFile3.exists())
    }

    @Test
    fun downloadPicture_downloadSucceed() {
        val avatarDir = LocalAvatarCache.getAvatarDir(context())
        avatarDir.mkdirs()
        val expectedFile = getAvatarFile(profile)
        expectedFile.delete()

        val downloadedFile = avatarCache.downloadPicture(profile.pictureUrl!!, expectedFile.name)

        assertTrue(downloadedFile != null && downloadedFile.exists())
    }

    @Test
    fun downloadPicture_whenKeepFailing_retry3Times() {
        val avatarDir = LocalAvatarCache.getAvatarDir(context())
        avatarDir.mkdirs()
        val expectedFile = getAvatarFile(profile)
        expectedFile.delete()

        whenever(fileDownloader.download(anyString(), anyString()))
            .thenThrow(SSLException("test exception"))

        val downloadedFile = avatarCache.downloadPicture(profile.pictureUrl!!, expectedFile.name)

        verify(fileDownloader, times(3)).download(profile.pictureUrl!!, expectedFile.name)
        assertTrue(downloadedFile == null)
    }

    @Test
    fun downloadPicture_whenSucceedAfterRetry_proceedSuccessfully() {
        val avatarDir = LocalAvatarCache.getAvatarDir(context())
        avatarDir.mkdirs()
        val expectedFile = getAvatarFile(profile)
        expectedFile.delete()

        val tmpFile = File(context().cacheDir, "tmp.file")
        tmpFile.delete()
        tmpFile.createNewFile()
        whenever(fileDownloader.download(anyString(), anyString()))
            .thenThrow(SSLException("test exception"))
            .thenThrow(SocketTimeoutException())
            .thenReturn(tmpFile)

        val downloadedFile = avatarCache.downloadPicture(profile.pictureUrl!!, expectedFile.name)

        verify(fileDownloader, times(3)).download(profile.pictureUrl!!, expectedFile.name)
        assertTrue(downloadedFile != null && downloadedFile.exists())
    }

    @Test
    fun copyDownloadedFileToCacheFileAndDelete_whenFileExist_thenCopySucceed() {
        val tmpFile = File(context().cacheDir, "tmp.file")
        tmpFile.delete()
        tmpFile.createNewFile()

        val avatarFile = getAvatarFile(profile)
        avatarFile.delete()

        avatarCache.copyDownloadedFileToCacheFileAndDelete(tmpFile, avatarFile)

        assertTrue(avatarFile.exists())
    }

    @Test
    fun copyDownloadedFileToCacheFileAndDelete_whenFileNotExist_thenNoCopy() {
        val tmpFile = File(context().cacheDir, "tmp.file")
        tmpFile.delete()

        val avatarFile = getAvatarFile(profile)
        avatarFile.delete()

        avatarCache.copyDownloadedFileToCacheFileAndDelete(tmpFile, avatarFile)

        assertFalse(avatarFile.exists())
    }

    @Test
    fun cache_whenPictureUrlIsNotANetworkUrl_doesNotCrash() {
        val avatarFile = getAvatarFile(profile)
        avatarFile.delete()

        val testPictureUrl = "invalid.url"
        avatarCache.cache(profile.id, testPictureUrl, profile.pictureLastModifier)
    }

    @Test
    fun cacheWithValidUrl_whenCacheFileExist_callWarmUp() {
        val avatarFile = getAvatarFile(profile)
        avatarFile.delete()
        avatarFile.createNewFile()

        runBlocking {
            avatarCache.cache(profile.id, profile.pictureUrl, profile.pictureLastModifier)
            avatarCache.cacheJob?.join()
        }

        verify(avatarCacheWarmUp).cache(getAvatarUrl(profile))
    }

    @Test
    fun cacheWithValidUrl_whenAvatarDirDoesNotExist_downloadSucceed() {
        val avatarFile = getAvatarFile(profile)
        avatarFile.delete()

        val avatarDir = LocalAvatarCache.getAvatarDir(context())
        avatarDir.deleteRecursively()

        runBlocking {
            avatarCache.cache(profile.id, profile.pictureUrl, profile.pictureLastModifier)
            avatarCache.cacheJob?.join()
        }

        assertTrue(avatarDir.exists())
        assertTrue(avatarFile.exists())
        verify(avatarCacheWarmUp).cache(getAvatarUrl(profile))
    }

    @Test
    fun cacheWithValidUrl_whenCacheFileDoesNotExist_deleteAllOtherCachesAndDownloadSucceed() {
        val avatarDir = LocalAvatarCache.getAvatarDir(context())
        avatarDir.mkdirs()

        // create other files to test
        val avatarFile1 = File(avatarDir, "${profile.id}1234")
        val avatarFile2 = File(avatarDir, "${profile.id}_1234")
        val avatarFile3 = File(avatarDir, "1234")
        avatarFile1.createNewFile()
        avatarFile2.createNewFile()
        avatarFile3.createNewFile()

        // delete file will be cached
        val avatarFile = getAvatarFile(profile)
        avatarFile.delete()

        runBlocking {
            avatarCache.cache(profile.id, profile.pictureUrl, profile.pictureLastModifier)
            avatarCache.cacheJob?.join()
        }

        // check cache succeed
        assertTrue(avatarDir.exists())
        assertTrue(avatarFile.exists())
        // check other files are deleted accordingly
        assertFalse(avatarFile1.exists())
        assertFalse(avatarFile2.exists())
        assertTrue(avatarFile3.exists())
        // check cache warmup
        verify(avatarCacheWarmUp).cache(getAvatarUrl(profile))
    }

    @Test
    fun cacheWithValidUrl_whenCallInMultiThreads_downloadSucceedInOrder() {
        val avatarDir = LocalAvatarCache.getAvatarDir(context())
        avatarDir.mkdirs()

        val avatarFile = getAvatarFile(profile)
        avatarFile.delete()

        runBlocking {
            var job1 = launch(Dispatchers.Default) {
                avatarCache.cache(profile.id, profile.pictureUrl, profile.pictureLastModifier)
                avatarCache.cacheJob?.join()
            }
            var job2 = launch(Dispatchers.Default) {
                avatarCache.cache(profile.id, profile.pictureUrl, profile.pictureLastModifier)
                avatarCache.cacheJob?.join()
            }
            job1.join()
            job2.join()
        }

        inOrder(fileDownloader, avatarCacheWarmUp) {
            verify(fileDownloader).download(profile.pictureUrl!!, avatarFile.name)
            verify(avatarCacheWarmUp, times(2)).cache(getAvatarUrl(profile))
        }
        assertTrue(avatarFile.exists())
    }

    @Test
    fun cacheWithDifferentUrl_whenCallInMultiThreads_downloadSucceedInOrder() {
        val avatarDir = LocalAvatarCache.getAvatarDir(context())
        avatarDir.mkdirs()

        val profile1 = ProfileBuilder.create()
            .withId(123456L)
            .withPictureUrl("https://test.picture.url")
            .withPictureLastModifier("2020-06-10T06:23:26.190095")
            .build()
        val profile2 = ProfileBuilder.create()
            .withId(123456L)
            .withPictureUrl("https://test.picture.url.2")
            .withPictureLastModifier("2020-07-10T07:26:45.782147")
            .build()

        val avatarFile1 = getAvatarFile(profile1)
        avatarFile1.delete()
        val avatarFile2 = getAvatarFile(profile2)
        avatarFile2.delete()

        val tmpFile1 = File(context().cacheDir, "tmp1.file")
        tmpFile1.delete()
        tmpFile1.createNewFile()
        whenever(fileDownloader.download(profile1.pictureUrl!!, avatarFile1.name)).thenReturn(tmpFile1)

        val tmpFile2 = File(context().cacheDir, "tmp2.file")
        tmpFile2.delete()
        tmpFile2.createNewFile()
        whenever(fileDownloader.download(profile2.pictureUrl!!, avatarFile2.name)).thenReturn(tmpFile2)

        runBlocking {
            var job1 = launch(Dispatchers.Default) {
                avatarCache.cache(profile1.id, profile1.pictureUrl, profile1.pictureLastModifier)
                avatarCache.cacheJob?.join()
            }
            var job2 = launch(Dispatchers.Default) {
                avatarCache.cache(profile2.id, profile2.pictureUrl, profile2.pictureLastModifier)
                avatarCache.cacheJob?.join()
            }
            job1.join()
            job2.join()
        }

        inOrder(fileDownloader, avatarCacheWarmUp) {
            verify(fileDownloader).download(profile1.pictureUrl!!, avatarFile1.name)
            verify(avatarCacheWarmUp).cache(Uri.fromFile(avatarFile1).toString())
            verify(fileDownloader).download(profile2.pictureUrl!!, avatarFile2.name)
            verify(avatarCacheWarmUp).cache(Uri.fromFile(avatarFile2).toString())
        }
        assertTrue(avatarFile2.exists())
    }

    private fun getAvatarFile(profile: IProfile) =
        LocalAvatarCache.getAvatarFile(context(), profile.id, profile.pictureUrl!!, profile.pictureLastModifier)

    private fun getAvatarUrl(profile: IProfile) =
        LocalAvatarCache.getAvatarUrl(context(), profile)

    private fun mockFileDownload() {
        val tmpFile = File(context().cacheDir, "tmp.file")
        tmpFile.delete()
        tmpFile.createNewFile()

        whenever(fileDownloader.download(anyString(), anyString())).thenReturn(tmpFile)
    }

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext
}
