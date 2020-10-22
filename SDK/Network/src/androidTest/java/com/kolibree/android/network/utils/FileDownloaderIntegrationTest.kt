/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.network.utils

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.io.File
import java.io.IOException
import java.net.SocketException
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import okhttp3.Call
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.Test

class FileDownloaderIntegrationTest : BaseInstrumentationTest() {

    private val mockWebServer = MockWebServer()

    private val fileDownloader = FileDownloader(context())

    override fun context(): Context {
        return InstrumentationRegistry.getInstrumentation().targetContext
    }

    override fun setUp() {
        super.setUp()

        clearFilesFolder()
    }

    override fun tearDown() {
        super.tearDown()

        clearFilesFolder()
    }

    /*
    download
     */
    @Test
    fun download_returnsFile_ifItExistsAndSizeIsNotEmpty() {
        val expectedFilename = "random.txt"
        val existingFile = File(folder(), expectedFilename)

        assertTrue(folder().mkdirs())
        assertTrue(existingFile.createNewFile())

        existingFile.writeText("Hola")

        assertTrue(existingFile.length() > 0)

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            assertEquals(
                existingFile,
                fileDownloader.download(baseUrl.toString(), expectedFilename)
            )

            assertEquals(0, webServer.requestCount)
        }
    }

    @Test
    fun download_downloadsUrlAndOverwritesFile_ifFileExistsButSizeIsEmpty() {
        val expectedFilename = "random.txt"
        val existingFile = File(folder(), expectedFilename)

        assertTrue(folder().mkdirs())
        assertTrue(existingFile.createNewFile())

        assertEquals(0, existingFile.length())

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            val bodyText = "Hola"
            webServer.enqueue(MockResponse().setResponseCode(200).setBody(bodyText))

            assertEquals(
                existingFile,
                fileDownloader.download(baseUrl.toString(), expectedFilename)
            )

            assertEquals(1, webServer.requestCount)

            assertEquals(bodyText, existingFile.readText())
        }
    }

    @Test
    fun download_downloadsUrlToFile_ifFileDoesNotExist() {
        val expectedFilename = "random.txt"
        val existingFile = File(folder(), expectedFilename)

        assertTrue(folder().mkdirs())

        assertFalse(existingFile.exists())

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            val bodyText = "Hola"
            webServer.enqueue(MockResponse().setResponseCode(200).setBody(bodyText))

            assertEquals(
                existingFile,
                fileDownloader.download(baseUrl.toString(), expectedFilename)
            )

            assertEquals(1, webServer.requestCount)

            assertEquals(bodyText, existingFile.readText())
        }
    }

    @Test(expected = IOException::class)
    fun download_throwsIOException_ifResponseIsNotSuccessful() {
        val expectedFilename = "random.txt"
        val existingFile = File(folder(), expectedFilename)

        assertTrue(folder().mkdirs())
        assertTrue(existingFile.createNewFile())

        assertEquals(0, existingFile.length())

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            webServer.enqueue(MockResponse().setResponseCode(404))

            fileDownloader.download(baseUrl.toString(), expectedFilename)
        }
    }

    @Test(expected = IOException::class)
    fun download_throwsIOException_ifBodyIsEmpty() {
        val expectedFilename = "random.txt"
        val existingFile = File(folder(), expectedFilename)

        assertTrue(folder().mkdirs())
        assertTrue(existingFile.createNewFile())

        assertEquals(0, existingFile.length())

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            webServer.enqueue(MockResponse().setResponseCode(200).setBody(Buffer()))

            fileDownloader.download(baseUrl.toString(), expectedFilename)
        }
    }

    @Test
    fun download_fileAlreadyExistsButIsEmpty_deletesFileIfResponseIsNotSuccessful() {
        val expectedFilename = "random.txt"
        val existingFile = File(folder(), expectedFilename)

        assertTrue(folder().mkdirs())
        assertTrue(existingFile.createNewFile())

        assertEquals(0, existingFile.length())

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            webServer.enqueue(MockResponse().setResponseCode(404))

            try {
                fileDownloader.download(baseUrl.toString(), expectedFilename)
            } catch (ioe: IOException) {
                // do nothing
            }

            assertFalse(existingFile.exists())
        }
    }

    @Test
    fun download_fileDoesNotExist_doesNotCreateFileIfResponseIsNotSuccessful() {
        val expectedFilename = "random.txt"
        val expectedDestinationFile = File(folder(), expectedFilename)

        assertTrue(folder().mkdirs())

        assertFalse(expectedDestinationFile.exists())

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            webServer.enqueue(MockResponse().setResponseCode(404))

            try {
                fileDownloader.download(baseUrl.toString(), expectedFilename)
            } catch (ioe: IOException) {
                // do nothing
            }

            assertFalse(expectedDestinationFile.exists())
        }
    }

    /*
    cancelRequests
     */
    @Test
    fun cancelRequests_doesNothingIsNoOngoingRequest() {
        fileDownloader.cancelRequests()
    }

    @Test
    fun cancelRequests_invokesCancelIfRequestIsNotCanceled() {
        val request = mock<Call>()
        fileDownloader.ongoingCalls.add(request)

        whenever(request.isCanceled).thenReturn(false)

        fileDownloader.cancelRequests()

        verify(request).cancel()
    }

    @Test
    fun cancelRequests_doesNotInvokeCancelIfRequestIsAlreadyCanceled() {
        val request = mock<Call>()
        fileDownloader.ongoingCalls.add(request)

        whenever(request.isCanceled).thenReturn(true)

        fileDownloader.cancelRequests()

        verify(request, never()).cancel()
    }

    @Test
    fun cancelRequests_clearsOngoingRequests() {
        fileDownloader.ongoingCalls.add(mock())

        fileDownloader.cancelRequests()

        assertTrue(fileDownloader.ongoingCalls.isEmpty())
    }

    @Test
    fun cancelRequests_cancelsOngoingCall() {
        val expectedFilename = "random.txt"
        val expectedDestinationFile = File(folder(), expectedFilename)

        assertTrue(folder().mkdirs())

        assertFalse(expectedDestinationFile.exists())

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            val bodyText = "Hola"
            webServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBodyDelay(300, TimeUnit.MILLISECONDS)
                    .setBody(bodyText)
            )

            val thread = Thread {
                Thread.sleep(50)

                assertFalse(fileDownloader.ongoingCalls.isEmpty())

                if (!Thread.interrupted()) {
                    fileDownloader.cancelRequests()
                }
            }

            try {
                thread.start()

                var socketExceptionCaptured = false
                try {
                    fileDownloader.download(baseUrl.toString(), expectedFilename)
                } catch (se: SocketException) {
                    socketExceptionCaptured = true
                }

                assertTrue(socketExceptionCaptured)

                assertFalse(expectedDestinationFile.exists())
            } finally {
                thread.interrupt()
            }
        }
    }

    /*
    Utils
     */
    private fun folder(): File =
        File(context().applicationContext.cacheDir, FileDownloader.KOLIBREE_FILES_CACHE)

    private fun clearFilesFolder() {
        folder().deleteRecursively()
    }
}
