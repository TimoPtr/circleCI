/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator

import android.os.Handler
import android.os.HandlerThread
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.synchronizator.operations.QueueOperation
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import org.threeten.bp.Duration
import timber.log.Timber

/**
 * Executor of [QueueOperation]
 */
internal interface QueueOperationExecutor {
    /**
     * Adds [queueOperation] to the [QueueOperation] execution queue
     *
     * @param initialDelay minimum delay after which [queueOperation] can be started
     */
    fun enqueue(queueOperation: QueueOperation, initialDelay: Duration = Duration.ZERO)

    /**
     * Cancels every [QueueOperation] that hasn't started yet
     *
     * This method should be used when we want to discard all pending operations because they will
     * never succeed. For example, after a user logs out, any interaction with the backend will
     * result in an unrecoverable 401
     */
    fun cancelOperations()
}

/**
 * Executes enqueued [QueueOperation]s using a FIFO strategy
 */
@AppScope
internal class FifoQueueOperationExecutor
constructor(
    private val networkChecker: NetworkChecker,
    private val synchronizeOnNetworkAvailableUseCase: SynchronizeOnNetworkAvailableUseCase,
    private val handler: Handler
) : QueueOperationExecutor {
    private val ongoingOperations = ConcurrentLinkedQueue<WeakReference<QueueOperation>>()

    @Inject
    constructor(
        networkChecker: NetworkChecker,
        synchronizeOnNetworkAvailableUseCase: SynchronizeOnNetworkAvailableUseCase
    ) : this(
        networkChecker,
        synchronizeOnNetworkAvailableUseCase,
        HandlerThread("Synchronization-Queue-Thread").run {
            start()
            Handler(looper)
        }
    )

    /**
     * If there's no network at the time [queueOperation] is executed, the operation won't be run
     * and we'll schedule a Synchronization when connectivity is back
     */
    override fun enqueue(queueOperation: QueueOperation, initialDelay: Duration) {
        handler.postDelayed({
            try {
                if (networkChecker.hasConnectivity()) {
                    runOperation(queueOperation)
                } else {
                    synchronizeOnNetworkAvailableUseCase.schedule()

                    queueOperation.onOperationNotRun()
                }
            } catch (e: Exception) {
                // we don't want the queue to crash because of an operation error
                Timber.e(e)
            }
        }, initialDelay.toMillis())
    }

    private fun runOperation(queueOperation: QueueOperation) {
        val weakReference = WeakReference(queueOperation)
        ongoingOperations.add(weakReference)

        try {
            queueOperation.run()
        } finally {
            ongoingOperations.remove(weakReference)
        }
    }

    override fun cancelOperations() {
        handler.removeCallbacksAndMessages(null)

        ongoingOperations
            .asSequence()
            .mapNotNull(WeakReference<QueueOperation>::get)
            .forEach(QueueOperation::onOperationCanceled)
    }
}
