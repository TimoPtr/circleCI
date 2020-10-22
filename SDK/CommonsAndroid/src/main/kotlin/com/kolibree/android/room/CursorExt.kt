/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.room

import android.database.Cursor
import androidx.annotation.Keep

@Keep
fun Cursor.booleanValueForColumn(columnName: String): Boolean? =
    getColumnIndex(columnName).let {
        if (isNull(it)) null else getInt(it) > 0
    }

@Keep
fun Cursor.intValueForColumn(columnName: String): Int? =
    getColumnIndex(columnName).let {
        if (isNull(it)) null else getInt(it)
    }

@Keep
fun Cursor.longValueForColumn(columnName: String): Long? =
    getColumnIndex(columnName).let {
        if (isNull(it)) null else getLong(it)
    }

@Keep
fun Cursor.doubleValueForColumn(columnName: String): Double? =
    getColumnIndex(columnName).let {
        if (isNull(it)) null else getDouble(it)
    }

@Keep
fun Cursor.stringValueForColumn(columnName: String): String? =
    getColumnIndex(columnName).let {
        if (isNull(it)) null else getString(it)
    }
