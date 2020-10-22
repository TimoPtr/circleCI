/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.models.exceptions

import io.reactivex.exceptions.CompositeException

/**
 * Groups exceptions occurred during Synchronization and reports them after all Bundles have been processed
 */
internal class SynchronizatorException(cause: Throwable? = null) : Exception(cause) {

    init {
        if (cause is CompositeException) {
            cause.exceptions.forEach(::addSuppressed)
        }
    }
}
