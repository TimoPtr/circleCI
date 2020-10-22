/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator

import com.kolibree.android.synchronizator.operations.QueueOperation
import org.threeten.bp.Duration

internal open class FakeOperation(
    private val duration: Duration = Duration.ofSeconds(0)
) : QueueOperation() {
    var runInvoked: Boolean = false
        private set

    var onOperationNotRunInvoked: Boolean = false
        private set

    override fun onOperationNotRun() {
        onOperationNotRunInvoked = true
    }

    override fun run() {
        runInvoked = true
        Thread.sleep(duration.toMillis())
    }

    fun testIsCanceled() = isCanceled()
}
