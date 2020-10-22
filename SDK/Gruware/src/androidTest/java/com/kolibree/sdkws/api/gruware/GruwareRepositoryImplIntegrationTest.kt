/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.api.gruware

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.kolibree.android.network.utils.FileDownloader
import com.kolibree.android.network.utils.FileDownloader.KOLIBREE_FILES_CACHE
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.sdkws.api.response.GruwareResponse
import com.nhaarman.mockitokotlin2.mock
import java.io.File
import java.io.IOException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test

class GruwareRepositoryImplIntegrationTest : BaseInstrumentationTest() {
    private val gruWareManager: GruwareManager = mock()

    private val fileDownloader: FileDownloader = FileDownloader(context())

    private val mockWebServer = MockWebServer()

    private val gruwareRepository = GruwareRepositoryImpl(gruWareManager, fileDownloader)

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
    createGruwareData
     */
    @Test
    fun `whenUrlReturns404_fileIsDeleted`() {
        val jsonPath = "e1_2.6_fw1.19.0_response.json"

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            val json = SharedTestUtils.getJson("json/$jsonPath")
                .replace(URL_PLACEHOLDER, baseUrl.toString())
            val gruwareResponse = Gson().fromJson(json, GruwareResponse::class.java)

            webServer.enqueue(MockResponse().setResponseCode(404))

            val expectedFile = File(folder(), gruwareResponse.gru()!!.filename)

            assertFalse(expectedFile.exists())

            try {
                gruwareRepository.createGruwareData(gruwareResponse)
            } catch (ioException: IOException) {
                // do nothing
            }

            assertFalse(expectedFile.exists())
        }
    }

    @Test
    fun `whenFileIsEmpty_weAttemptToDownloadItAgain`() {
        val jsonPath = "e1_2.6_fw1.19.0_response.json"

        mockWebServer.use { webServer ->
            webServer.start()
            val baseUrl = webServer.url("/gruware/")

            val json = SharedTestUtils.getJson("json/$jsonPath")
                .replace(URL_PLACEHOLDER, baseUrl.toString())
            val gruwareResponse = Gson().fromJson(json, GruwareResponse::class.java)

            webServer.enqueue(MockResponse().setResponseCode(200).setBody("First"))
            webServer.enqueue(MockResponse().setResponseCode(200).setBody("Second"))

            val expectedFile = File(folder(), gruwareResponse.gru()!!.filename)

            assertTrue(folder().mkdir())
            assertTrue(expectedFile.createNewFile())

            gruwareRepository.createGruwareData(gruwareResponse)

            // json only contains 2 files to download
            assertEquals(2, mockWebServer.requestCount)

            assertTrue(expectedFile.exists())
        }
    }

    /*
    Utils
     */
    private fun folder(): File = File(context().applicationContext.cacheDir, KOLIBREE_FILES_CACHE)

    private fun clearFilesFolder() {
        folder().deleteRecursively()
    }
}

private const val URL_PLACEHOLDER = "<baseUrl>"
