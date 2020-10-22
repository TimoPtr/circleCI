@file:JvmName("ResourcesUtils")

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.test

import java.io.File
import java.io.IOException

@Suppress("SdkPublicExtensionMethodWithoutKeep")
fun String.writeToTempFile(): File {
    val tmpFile = File.createTempFile("tmp_", "_fromResource")
    // It's important to use the classloader of a file in androidTest
    SharedTestUtils::class.java.classLoader?.getResourceAsStream(this).use { inputStream ->
        tmpFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
    }

    if (!tmpFile.exists() || tmpFile.length() == 0L) throw IOException("Couldn't write to tmpFile")

    return tmpFile
}
