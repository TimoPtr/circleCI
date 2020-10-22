/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.extensions

import android.net.Uri
import android.webkit.URLUtil
import androidx.annotation.Keep

@Keep
fun String?.isNullOrNullValue(): Boolean = this == null ||
    isEmpty() ||
    trim().equals(NULL, ignoreCase = true)

@Keep
fun String?.takeIfNotBlank(): String? = takeIf { !it.isNullOrBlank() }

/**
 * @return null if String is blank, not a valid URL or not a File path. Otherwise, return
 * sanitized path to be consumed by Picasso
 */
@Keep
fun String?.sanitizedUrl(): String? {
    return when {
        URLUtil.isNetworkUrl(this) -> this
        URLUtil.isFileUrl(this) -> this
        else -> fileOrNull()?.let { file -> Uri.fromFile(file).toString() }
    }
}

private const val NULL = "null"
