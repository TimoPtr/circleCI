/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.bttester.utils

import android.content.Context
import com.kolibree.android.game.bi.generateAvroFileName
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

internal class AvroWriter @Inject constructor(context: Context) {

    private val appContext: Context = context.applicationContext

    fun saveAvro(avroData: List<Char>): Single<String> = Single.defer {
        appContext.getExternalFilesDir(null)?.let { parent ->
            val avroFile = File(parent, generateAvroFileName())
            avroFile.writeBytes(avroData.map { it.toByte() }.toByteArray())
            Single.just(avroFile.path)
        } ?: Single.just("")
    }
}
