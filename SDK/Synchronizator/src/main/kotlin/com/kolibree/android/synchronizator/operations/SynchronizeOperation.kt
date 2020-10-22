/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import androidx.annotation.VisibleForTesting
import com.kolibree.android.synchronizator.BundleConsumable
import com.kolibree.android.synchronizator.BundleConsumableBuilder
import com.kolibree.android.synchronizator.CatalogBundleConsumable
import com.kolibree.android.synchronizator.ItemBundleConsumable
import com.kolibree.android.synchronizator.QueueOperationExecutor
import com.kolibree.android.synchronizator.ReadOnlyBundleConsumable
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.data.usecases.PendingWrappersUseCase
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.exceptions.SynchronizatorException
import com.kolibree.android.synchronizator.operations.usecases.DeleteByKolibreeIdUseCase
import com.kolibree.android.synchronizator.operations.usecases.ProcessUpdatedIdUseCase
import com.kolibree.android.synchronizator.usecases.BackendSynchronizationStateUseCase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.SortedMap
import javax.inject.Inject
import javax.inject.Provider
import org.threeten.bp.Duration
import retrofit2.HttpException
import timber.log.Timber

internal class SynchronizeOperation
@Inject constructor(
    private val synchronizeQueueOperationProvider: Provider<SynchronizeQueueOperation>,
    private val queueOperationExecutor: QueueOperationExecutor
) : SyncOperation {
    fun run(initialDelay: Duration = Duration.ZERO): Completable {
        return Completable.fromAction {
            queueOperationExecutor.enqueue(
                synchronizeQueueOperationProvider.get(),
                initialDelay
            )
        }
    }
}

/**
 * Operation that processes all pending operations (Create/Edit + Delete) before syncing with the
 * backend
 *
 * Uses Synchronization Support (https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755460/Synchronization+support)
 * , a lightweight synchronization mechanism that only requests updates for the items that changed
 * since we last synchronized
 */
@VisibleForTesting
internal class SynchronizeQueueOperation
@Inject constructor(
    private val bundleConsumableBuilder: BundleConsumableBuilder,
    private val createOrEditOperationProvider: Provider<CreateOrEditOperation>,
    private val deleteOperationProvider: Provider<DeleteOperation>,
    private val pendingWrappersUseCase: PendingWrappersUseCase,
    private val deleteByKolibreeIdUseCase: DeleteByKolibreeIdUseCase,
    private val processUpdatedIdUseCase: ProcessUpdatedIdUseCase,
    private val ongoingSynchronizationUseCase: BackendSynchronizationStateUseCase
) : QueueOperation() {
    override fun onOperationNotRun() {
        // no-op
    }

    @Throws(SynchronizatorException::class)
    override fun run() {
        if (isCanceled()) return

        ongoingSynchronizationUseCase.onSyncStarted()

        try {
            uploadPending()

            fetchRemote()

            ongoingSynchronizationUseCase.onSyncSuccess()
        } catch (exception: Exception) {
            ongoingSynchronizationUseCase.onSyncFailed()

            throw exception
        }
    }

    @VisibleForTesting
    fun uploadPending() {
        SynchronizationBundles.bundles
            .filterIsInstance(SynchronizableItemBundle::class.java)
            .sortedGroup(SynchronizableItemBundle::key)
            .parallel(
                onEachPriority = { priority ->
                    Timber.d("Upload: Priority: $priority - ${SynchronizableKey.findBy(priority)}")
                },
                onEachItem = { bundle ->
                    Timber.d("Upload: ${bundle.key()} on ${Thread.currentThread()}")
                    bundle.uploadPending()
                    bundle.deletePending()
                }
            )
    }

    private fun SynchronizableItemBundle.uploadPending() {
        pendingWrappersUseCase.getPendingCreate(this)
            .filterNot { isCanceled() }
            .forEach { item -> createOrEditOperationProvider.get().run(item).blockingGet() }
    }

    private fun SynchronizableItemBundle.deletePending() {
        pendingWrappersUseCase.getPendingDelete(this)
            .filterNot { isCanceled() }
            .forEach { item -> deleteOperationProvider.get().run(item).blockingAwait() }
    }

    /**
     * Constructs a List<[BundleConsumable]> from backend's response and processes them in order
     *
     * @throws HttpException if the call to the backend fails
     * @throws SynchronizatorException containing all exceptions thrown from interactions with [BundleConsumable]. It
     * guarantees that the exception is only thrown after processing all items
     */
    @VisibleForTesting
    fun fetchRemote() {
        if (isCanceled()) return

        bundleConsumableBuilder
            .buildBundleConsumables()
            .filterNot { isCanceled() }
            .sortedGroup(BundleConsumable::key)
            .parallel(
                onEachPriority = { priority ->
                    Timber.d("Fetch: Priority: $priority - ${SynchronizableKey.findBy(priority)}")
                },
                onEachItem = { bundle ->
                    Timber.d("Fetch: ${bundle.key()} on ${Thread.currentThread()}")
                    bundle.process()
                })
    }

    private fun BundleConsumable.process() {
        when (this) {
            is ItemBundleConsumable -> internalProcessItemBundleConsumable(this)
            is CatalogBundleConsumable -> internalProcessCatalogBundleConsumable(this)
            is ReadOnlyBundleConsumable -> internalProcessReadOnlyBundleConsumable(this)
        }
    }

    /**
     * Processes data contained in [bundleConsumable]
     *
     * @throws SynchronizatorException containing all exceptions thrown from interactions with [bundleConsumable]. It
     * guarantees that the exception is only thrown after processing all items
     */
    @VisibleForTesting
    fun internalProcessItemBundleConsumable(bundleConsumable: ItemBundleConsumable) {
        bundleConsumable.apply {
            processDeletedIds()

            processUpdatedIds()
        }
    }

    /**
     * Commands bundle's DataStore to delete all deletedIds
     *
     * @throws SynchronizatorException containing all exceptions thrown from interactions with
     * [SynchronizableItemDataStore]. It guarantees that the exception is only thrown after processing all items
     */
    private fun ItemBundleConsumable.processDeletedIds() {
        itemConsumable.deletedIds.invokeDelayError { deletedId ->
            deleteByKolibreeIdUseCase.delete(deletedId, itemBundle)
        }
    }

    /**
     * Processes all updatedIds from [ItemBundleConsumable]
     *
     * @throws SynchronizatorException containing all exceptions thrown from processing updatedIds. It guarantees that
     * the exception is only thrown after processing all items
     */
    private fun ItemBundleConsumable.processUpdatedIds() {
        itemConsumable.updatedIds.invokeDelayError { updatedId ->
            processUpdatedIdUseCase.process(updatedId, itemBundle)
        }
    }

    /**
     * Processes all updatedIds in [bundleConsumable]
     *
     * @throws SynchronizatorException containing all exceptions thrown from processing updatedIds. It guarantees that
     * the exception is only thrown after processing all items
     */
    @VisibleForTesting
    fun internalProcessReadOnlyBundleConsumable(bundleConsumable: ReadOnlyBundleConsumable) {
        bundleConsumable.apply {
            readOnlyConsumable.updatedIds.invokeDelayError { id ->
                val remoteList = readOnlyBundle.api.get(id)

                readOnlyBundle.dataStore.replace(remoteList)
            }
        }
    }

    /**
     * Processes data contained in [bundleConsumable]
     *
     * @throws HttpException if the call to the backend fails
     */
    @VisibleForTesting
    fun internalProcessCatalogBundleConsumable(bundleConsumable: CatalogBundleConsumable) {
        bundleConsumable.apply {
            val remoteSynchronizables = catalogBundle.api.get()

            catalogBundle.dataStore.replace(remoteSynchronizables)
        }
    }

    /**
     * Groups any items that contains [SynchronizableKey]
     * into [SortedMap]. Higher priority goes first.
     */
    private fun <T> List<T>.sortedGroup(key: T.() -> SynchronizableKey): SortedMap<Int, List<T>> {
        return groupBy { item -> item.key().priority }
            .toSortedMap(reverseOrder())
    }

    /**
     * Executes given [onEachItem] actions in parallel.
     *
     * Parallel execution is only performed for the same priority.
     * Priorities are executed concurrently - next priority is started
     * when previous one finishes.
     *
     * If operation is canceled, neither [onEachItem] nor [onEachPriority] are invoked
     *
     * @suppress SpreadOperator because we need it for mergeArrayDelayError.
     */
    @Suppress("SpreadOperator")
    private fun <T> SortedMap<Int, List<T>>.parallel(
        onEachPriority: (Int) -> Unit,
        onEachItem: (T) -> Unit
    ) {
        if (isEmpty()) return

        Observable.fromIterable(entries)
            .concatMapCompletableDelayError { (priority, items) ->
                val completables = items.map { item ->
                    Completable
                        .fromAction { if (!isCanceled()) onEachItem(item) }
                        .subscribeOn(Schedulers.io())
                }.toTypedArray()

                Completable
                    .mergeArrayDelayError(*completables)
                    .doOnSubscribe { if (!isCanceled()) onEachPriority(priority) }
            }
            .synchronizatorBlockingAwait()
    }

    /**
     * To persist the same behavior and avoid additional
     * exception wrapping we need to catch exception on our own.
     *
     * By default RxJava will wrap any exception different
     * than [Error] into [RuntimeException].
     *
     * @suppress TooGenericExceptionCaught because we want to catch everything.
     */
    @SuppressWarnings("TooGenericExceptionCaught")
    private fun Completable.synchronizatorBlockingAwait() {
        try {
            blockingAwait()
        } catch (throwable: Throwable) {
            throw SynchronizatorException(throwable)
        }
    }
}

/**
 * Invokes [block] on each T and throws a [SynchronizatorException] that groups thrown exceptions, if any, as
 * suppressed exceptions
 *
 * @throws SynchronizatorException containing any exception thrown by [block]
 */
private inline fun <T> List<T>.invokeDelayError(block: (T) -> Unit) {
    val synchronizatorException = SynchronizatorException()

    forEach {
        try {
            block(it)
        } catch (e: Exception) {
            Timber.e(e)

            synchronizatorException.addSuppressed(e)
        }
    }

    if (synchronizatorException.suppressed.isNotEmpty())
        throw synchronizatorException
}

private fun BundleConsumable.key(): SynchronizableKey {
    return when (this) {
        is ItemBundleConsumable -> itemBundle.key()
        is ReadOnlyBundleConsumable -> readOnlyBundle.key()
        is CatalogBundleConsumable -> catalogBundle.key()
    }
}
