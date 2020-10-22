/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils.assets

import android.content.Context
import androidx.annotation.Keep
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader

@Keep
class AssetLoader(private val context: Context) {

    fun listAssets(dir: String): List<String> {
        return (context.assets.list(dir) ?: emptyArray<String>()).toList()
    }

    fun loadFile(assetName: String): File {
        val inputStream = context.assets.open(assetName)
        val outputFile = File(context.cacheDir.absolutePath + File.separator + assetName)
        outputFile.parentFile.mkdirs()
        val outputStream = FileOutputStream(outputFile)

        try {
            inputStream.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
        } finally {
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        }
        return outputFile
    }

    fun loadString(assetName: String): String {
        val buf = StringBuilder()
        val inputStream = context.assets.open(assetName)
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

        try {
            var line: String? = reader.readLine()
            do {
                line?.let { buf.append(it) }
                line = reader.readLine()
            } while (line != null)
        } catch (ex: IOException) {
            throw IOException("Error in reading asset file: $ex")
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                throw IOException("Error while closing input stream: $e")
            }
        }

        return buf.toString()
    }
}
