/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.core.avro

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.UnsupportedCharsetException
import junit.framework.TestCase.fail
import org.junit.Test

/** Created by miguelaragues on 8/3/18.  */
class AvroFileUploaderTest : BaseUnitTest() {

    private val context: Context = mock()

    private val connector: IKolibreeConnector = mock()

    private val networkChecker: NetworkChecker = mock()

    private lateinit var avroFileUploader: AvroFileUploaderImpl

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(context)

        avroFileUploader = spy(AvroFileUploaderImpl(context, connector, networkChecker))
    }

    /*
  UPLOAD PENDING FILES
   */
    @Test
    fun `uploadPendingFiles with 0 files pending does not invoke JobScheduler`() {
        doReturn(emptyArray<File>()).whenever(avroFileUploader).listAvroFiles(context)
        avroFileUploader.uploadPendingFiles()
    }

    @Test
    fun `uploadPendingFiles with 1 file pending invokes JobSchedulerOnce`() {
        val file = mock<File>()
        val jobInfo = mock<JobInfo>()

        doReturn(arrayOf(file)).whenever(avroFileUploader).listAvroFiles(context)
        doReturn(jobInfo).whenever(avroFileUploader).uploaderJobInfo(context, file)

        val scheduler = mockJobScheduler()
        avroFileUploader.uploadPendingFiles()
        verify(scheduler).schedule(jobInfo)
    }

    @Test
    fun `uploadPendingFiles with 2 files pending invokes JobSchedulerOnce`() {
        val file1 = mock<File>()
        val file2 = mock<File>()
        val jobInfo = mock<JobInfo>()
        doReturn(arrayOf(file1, file2)).whenever(avroFileUploader).listAvroFiles(context)
        doReturn(jobInfo).whenever(avroFileUploader).uploaderJobInfo(eq(context), any())

        val scheduler = mockJobScheduler()
        avroFileUploader.uploadPendingFiles()
        verify(scheduler, times(2)).schedule(jobInfo)
    }

    /*
  DELETE PENDING FILES
   */
    @Test
    fun `deletePendingFiles with 2 files pending invokes delete on each file`() {
        val file1 = mock<File>()
        val file2 = mock<File>()

        doReturn(arrayOf(file1, file2)).whenever(avroFileUploader).listAvroFiles(context)

        avroFileUploader.deletePendingFiles()
        verify(file1).delete()
        verify(file2).delete()
    }

    @Test
    fun `deletePendingFiles with 0 file pending does not crash`() {
        doReturn(emptyArray<File>()).whenever(avroFileUploader).listAvroFiles(context)

        avroFileUploader.deletePendingFiles()
    }

    /*
  uploadFile
   */
    @Test
    fun `uploadFile emits an error on when an error on backend occurred`() {
        whenever(connector.getAvroFileUploadUrl()).thenThrow(IOException())
        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        avroFileUploader.uploadFile(mock()).test()
            .assertError(IOException::class.java)
    }

    @Test
    fun `uploadFile without connectivity emits an error`() {
        whenever(networkChecker.hasConnectivity()).thenReturn(false)

        avroFileUploader.uploadFile(mock()).test()
            .assertError(IOException::class.java)

        // The Exception should be emitted, not thrown
        verify(connector, never()).getAvroFileUploadUrl()
    }

    /*
    uploadFileAndDeleteOnSuccess
     */

    @Test
    fun `uploadFileAndDeleteOnSuccess upload file if allow and delete the file if no errors`() {
        val file = mock<File>()
        whenever(connector.isDataCollectingAllowed).thenReturn(true)
        doReturn(Completable.complete()).whenever(avroFileUploader).uploadFile(file)

        avroFileUploader.uploadFileAndDeleteOnSuccess(file).test().assertComplete()

        inOrder(connector, file, avroFileUploader) {
            verify(connector).isDataCollectingAllowed
            verify(avroFileUploader).uploadFile(file)
            verify(file).delete()
        }
    }

    @Test
    fun `uploadFileAndDeleteOnSuccess does not upload file if allow and delete the file if no errors`() {
        val file = mock<File>()
        whenever(connector.isDataCollectingAllowed).thenReturn(false)
        doReturn(Completable.complete()).whenever(avroFileUploader).uploadFile(file)

        avroFileUploader.uploadFileAndDeleteOnSuccess(file).test().assertComplete()

        inOrder(connector, file, avroFileUploader) {
            verify(connector).isDataCollectingAllowed
            verify(avroFileUploader, never()).uploadFile(file)
            verify(file).delete()
        }
    }

    /*
    uploadFileToS3
     */
    @Test
    fun `uploadFileToS3 upload data correctly`() {
        val url = mock<URL>()
        val file = mock<File>()
        val connection = mock<HttpURLConnection>()

        whenever(url.openConnection()).thenReturn(connection)
        whenever(connection.responseCode).thenReturn(HttpURLConnection.HTTP_OK)
        whenever(file.length()).thenReturn(10L)
        doNothing().whenever(avroFileUploader).sendData(file, connection)

        avroFileUploader.uploadFileToS3(url, file)

        inOrder(connection, avroFileUploader) {
            verify(connection).doOutput = true
            verify(connection).requestMethod = "PUT"
            verify(connection).setFixedLengthStreamingMode(file.length())
            verify(connection).setRequestProperty("Content-Type", "binary/octet-stream")

            verify(avroFileUploader).sendData(file, connection)

            verify(connection).disconnect()
        }
    }

    @Test
    @Suppress("SwallowedException")
    fun `uploadFileToS3 disconnect on error`() {
        val url = mock<URL>()
        val file = mock<File>()
        val connection = mock<HttpURLConnection>()

        whenever(url.openConnection()).thenReturn(connection)
        whenever(connection.responseCode).thenReturn(HttpURLConnection.HTTP_OK)
        doThrow(IllegalArgumentException()).whenever(avroFileUploader).sendData(file, connection)
        try {
            avroFileUploader.uploadFileToS3(url, file)
            fail()
        } catch (e: IllegalArgumentException) {
            verify(connection).disconnect()
            verify(connection, never()).responseCode
        }
    }

    @Test(expected = UnsupportedCharsetException::class)
    /*
     It doesn't returns IllegalStateException directly because of IOUtils that crash but it
     assert that we are in the function to construct the error
     */
    fun `uploadFileToS3 throws IllegalStateException on network error`() {
        val url = mock<URL>()
        val file = mock<File>()
        val connection = mock<HttpURLConnection>()

        val data = ByteArrayInputStream("hello".toByteArray())

        whenever(url.openConnection()).thenReturn(connection)
        whenever(connection.responseCode).thenReturn(HttpURLConnection.HTTP_NOT_ACCEPTABLE)
        whenever(connection.errorStream).thenReturn(data)
        whenever(file.length()).thenReturn(10L)
        doNothing().whenever(avroFileUploader).sendData(file, connection)

        avroFileUploader.uploadFileToS3(url, file)
    }

    private fun mockJobScheduler(): JobScheduler {
        val scheduler = mock<JobScheduler>()
        whenever(context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).thenReturn(scheduler)
        return scheduler
    }
}
