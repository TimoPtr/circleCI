/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations.utils

import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.operations.CreateOrEditOperation
import com.kolibree.android.synchronizator.operations.DeleteOperation
import com.kolibree.android.synchronizator.operations.SynchronizeOperation
import com.kolibree.android.synchronizator.operations.UpdateOperation
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Provider

internal abstract class OperationProvider<T : Any> {
    val operations = mutableListOf<T>()

    abstract var newOperation: () -> T

    /**
     * Singleton Provider returned by this OperationProvider. Always returns the same instance
     */
    abstract fun provider(): Provider<T>
}

internal inline fun <reified T : Any> provider(noinline newOperationParameter: () -> T = { mock() }): OperationProvider<T> {
    return object : OperationProvider<T>() {
        private val operationProvider = mock<Provider<T>>()
            .apply {
                whenever(get())
                    .thenAnswer {
                        val operation: T = newOperation.invoke()

                        operations.add(operation)

                        operation
                    }
            }

        override fun provider(): Provider<T> = operationProvider

        override var newOperation: () -> T = newOperationParameter
    }
}

internal inline fun OperationProvider<SynchronizeOperation>.mockSync(crossinline responseProvider: () -> Completable = { Completable.complete() }) {
    newOperation = {
        mock<SynchronizeOperation>().apply {
            whenever(run()).thenReturn(responseProvider.invoke())
        }
    }
}

internal fun OperationProvider<CreateOrEditOperation>.mockCreateOrEdit(item: SynchronizableItem) {
    mockCreateOrEdit(Single.just(item))
}

internal fun OperationProvider<CreateOrEditOperation>.mockCreateOrEdit(response: Single<SynchronizableItem>) {
    newOperation = {
        mock<CreateOrEditOperation>().apply {
            whenever(run(any<SynchronizableItem>())).thenReturn(response)
        }
    }
}

internal fun OperationProvider<CreateOrEditOperation>.mockCreateOrEdit(
    items: List<SynchronizableItem>,
    responses: Array<Single<SynchronizableItem>>
) {
    newOperation = {
        mock<CreateOrEditOperation>().apply {
            whenever(run(any<SynchronizableItem>())).thenAnswer {
                val index = items.indexOf(it.getArgument(0))

                responses[index]
            }
        }
    }
}

internal fun OperationProvider<CreateOrEditOperation>.mockWrapperssAddEdit(
    wrappers: List<SynchronizableItemWrapper>,
    responses: Array<Single<SynchronizableItem>>
) {
    newOperation = {
        mock<CreateOrEditOperation>().apply {
            whenever(run(any<SynchronizableItemWrapper>())).thenAnswer {
                val index = wrappers.indexOf(it.getArgument(0))

                responses[index]
            }
        }
    }
}

internal fun OperationProvider<CreateOrEditOperation>.mockCreateOrEdit() {
    newOperation = {
        mock<CreateOrEditOperation>().apply {
            whenever(run(any<SynchronizableItem>()))
                .thenAnswer {
                    Single.just<SynchronizableItem>(it.getArgument(0))
                }
        }
    }
}

internal fun OperationProvider<UpdateOperation>.mockUpdate() {
    newOperation = {
        mock<UpdateOperation>().apply {
            whenever(run(any<SynchronizableItem>()))
                .thenAnswer {
                    Single.just<SynchronizableItem>(it.getArgument(0))
                }
        }
    }
}

internal inline fun OperationProvider<DeleteOperation>.mockDelete(crossinline responseProvider: () -> Completable = { Completable.complete() }) {
    newOperation = {
        mock<DeleteOperation>().apply {
            whenever(run(any<SynchronizableItem>())).thenReturn(responseProvider.invoke())
        }
    }
}

internal fun OperationProvider<DeleteOperation>.mockDelete(
    items: List<SynchronizableItem>,
    responses: Array<Completable>
) {
    newOperation = {
        mock<DeleteOperation>().apply {
            whenever(run(any<SynchronizableItem>())).thenAnswer {
                val index = items.indexOf(it.getArgument(0))

                responses[index]
            }
        }
    }
}

internal fun OperationProvider<DeleteOperation>.mockWrappersDelete(
    wrappers: List<SynchronizableItemWrapper>,
    responses: Array<Completable>
) {
    newOperation = {
        mock<DeleteOperation>().apply {
            whenever(run(any<SynchronizableItemWrapper>())).thenAnswer {
                val index = wrappers.indexOf(it.getArgument(0))

                responses[index]
            }
        }
    }
}
