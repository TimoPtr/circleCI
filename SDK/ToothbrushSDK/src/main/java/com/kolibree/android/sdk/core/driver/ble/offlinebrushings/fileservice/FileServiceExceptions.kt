/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.fileservice

internal sealed class FileSessionException : Exception()

internal object FileSessionNotOpenedException : FileSessionException()

internal object FileSessionNotActiveException : FileSessionException()

internal object FileSessionBusyException : FileSessionException()

internal object FileSessionNoFileSelectedException : FileSessionException()

internal object FileSessionUnknownException : FileSessionException()

internal object FileStorageError : FileSessionException()
