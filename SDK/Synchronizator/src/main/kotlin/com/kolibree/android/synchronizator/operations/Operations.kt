/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import com.kolibree.android.synchronizator.QueueOperationExecutor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Represents an Operation that can run in the context of Synchronization
 */
internal interface SyncOperation

/**
 * Operations to be executed sequentially in [QueueOperationExecutor]
 *
 * Descendants won't be run if there's no connectivity. Still, it's expected that they deal with
 * network errors internally and provide graceful recover if the error is recoverable, or take
 * action if it's an unrecoverable error so that the operation is not run again in the future
 */
internal abstract class QueueOperation : Runnable {
    private var isCanceled = AtomicBoolean(false)

    protected fun isCanceled() = isCanceled.get()

    /**
     * Invoked when the operation couldn't run at the time it was executed
     */
    abstract fun onOperationNotRun()

    /**
     * Invoked when the operation should immediately stop running
     */
    fun onOperationCanceled() {
        isCanceled.set(true)
    }
}
