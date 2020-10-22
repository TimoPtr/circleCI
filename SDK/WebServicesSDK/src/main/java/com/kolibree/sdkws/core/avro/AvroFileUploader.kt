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
import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import timber.log.Timber

@VisibleForApp
interface AvroFileUploader {
    /**
     * Attempts to upload avroFile to the backend. If the file is uploaded successfully, it's deleted
     * from the filesystem.
     * If the user forbid the usage of his data the file will be delete without being upload onto the
     * backend
     *
     * @param avroFile the File to upload
     * @return a Single that will emit the url the file was uploaded to, or an Exception
     */
    fun uploadFileAndDeleteOnSuccess(avroFile: File): Completable

    /** Uploads all files returned by AvroFileUploader.listAvroFiles  */
    fun uploadPendingFiles()

    /** Clears all pending files  */
    fun deletePendingFiles()
}

/**
 * Created by aurelien on 19/07/17.
 *
 *
 * AVRO raw data files uploader
 */
internal class AvroFileUploaderImpl @Inject internal constructor(
    private val context: Context,
    private val kolibreeConnector: IKolibreeConnector,
    private val networkChecker: NetworkChecker
) : AvroFileUploader {

    override fun uploadFileAndDeleteOnSuccess(avroFile: File): Completable =
        isUploadAllowed().flatMapCompletable { canUpload ->
            if (canUpload) {
                uploadFile(avroFile)
            } else {
                Timber.d("User disable data collection removing avro file ${avroFile.name}")
                Completable.complete()
            }
        }.andThen(deleteFile(avroFile))

    override fun uploadPendingFiles() {
        val files = listAvroFiles(context)
        if (files.isNotEmpty()) {
            getJobScheduler(context)?.run {
                files.forEach { avroFile ->
                    Timber.d("Schedule upload for $avroFile")
                    schedule(uploaderJobInfo(context, avroFile))
                }
            } ?: Timber.w("Impossible to get JobScheduler")
        } else {
            Timber.d("No file to upload, stopping...")
        }
    }

    override fun deletePendingFiles() {
        listAvroFiles(context).takeIf { it.isNotEmpty() }?.map(File::delete)
            ?: Timber.d("No file to delete, stopping...")
    }

    @VisibleForTesting
    fun uploaderJobInfo(context: Context, file: File): JobInfo =
        AvroUploaderJobService.uploaderJobInfo(context, file)

    /**
     * Get a list of the non uploaded AVRO data files
     *
     * @param context non null [Context]
     * @return file array, or null if no AVRO file found
     */
    @VisibleForTesting
    fun listAvroFiles(context: Context): Array<File> {
        val avroDir = File(context.cacheDir, AVRO_CACHE_DIRECTORY)

        if (!avroDir.exists()) {
            avroDir.mkdirs()
        }

        return avroDir.listFiles { _: File?, filename: String ->
            filename.startsWith(AVRO_PREFIX) &&
                filename.endsWith(AVRO_EXTENSION)
        } ?: emptyArray()
    }

    private fun isUploadAllowed(): Single<Boolean> =
        Single.fromCallable { kolibreeConnector.isDataCollectingAllowed }

    @Throws
    @VisibleForTesting
    fun uploadFileToS3(url: URL, avroFile: File) {
        var connection: HttpURLConnection? = null

        try {
            connection = url.openConnection() as HttpURLConnection
            connection.prepareConnectionForUpload(avroFile)
            sendData(avroFile, connection)

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                val errorStream = IOUtils.toString(connection.errorStream, "UTF_8")
                val errorMessage =
                    "Could not upload ${avroFile.name}: ${connection.responseCode} - $errorStream"

                throw IllegalStateException(errorMessage)
            }

            Timber.d("Avro file ${avroFile.name} correctly uploaded to $url")
        } finally {
            connection?.disconnect()
        }
    }

    @VisibleForTesting
    fun uploadFile(avroFile: File): Completable {
        return if (!networkChecker.hasConnectivity()) {
            Completable.error(IOException("No connectivity"))
        } else {
            Completable.create { emitter ->
                runCatching {
                    // Get Amazon S3 upload url
                    val amazonUrl = URL(kolibreeConnector.getAvroFileUploadUrl())

                    uploadFileToS3(amazonUrl, avroFile)

                    if (!emitter.isDisposed) {
                        emitter.onComplete()
                    }
                }.getOrElse {
                    emitter.tryOnError(it)
                }
            }
        }
    }

    private fun deleteFile(avroFile: File): Completable = Completable.create { emitter ->
        try {
            avroFile.delete()
            emitter.onComplete()
        } catch (se: SecurityException) {
            emitter.tryOnError(se)
        }
    }

    private fun getJobScheduler(context: Context): JobScheduler? =
        context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

    private fun HttpURLConnection.prepareConnectionForUpload(avroFile: File) {
        doOutput = true
        requestMethod = "PUT"
        setFixedLengthStreamingMode(avroFile.length())
        setRequestProperty("Content-Type", "binary/octet-stream")
    }

    @VisibleForTesting
    fun sendData(avroFile: File, connection: HttpURLConnection) {
        connection.outputStream.use { out ->
            out.write(avroFile.readBytes())
            out.flush()
        }
    }
}

private const val AVRO_PREFIX = "raw_data_avro_"
private const val AVRO_EXTENSION = ".avro"

/**
 * The directory within the uploader will look for AVRO data files
 *
 *
 * This directory is a subdirectory of the root of the app cache directory
 */
const val AVRO_CACHE_DIRECTORY = "avro"

/**
 * The uploader will search for files with names matching this pattern
 *
 *
 * The %s is an user's choice unique string
 *
 *
 * Make sure to fill it properly to prevent name collision
 */
const val AVRO_FILE_FORMAT = "$AVRO_PREFIX%s$AVRO_EXTENSION"
