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
import com.kolibree.android.synchronizator.SynchronizationBundles.bundleForTrackingEntity
import com.kolibree.android.synchronizator.createOrEdit
import com.kolibree.android.synchronizator.data.usecases.UpdateUploadStatusUseCase
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import com.kolibree.android.synchronizator.network.CreateOrEditHttpErrorInterceptor
import com.kolibree.android.synchronizator.operations.usecases.ConflictResolutionUseCase
import com.kolibree.android.synchronizator.resolve
import javax.inject.Inject

internal class RemoteCreateOrEditQueueOperationFactory
@Inject constructor(
    private val errorInterceptor: CreateOrEditHttpErrorInterceptor,
    private val conflictResolutionUseCase: ConflictResolutionUseCase,
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase
) {
    fun create(synchronizableItemWrapper: SynchronizableItemWrapper): RemoteCreateOrEditQueueOperation {
        return RemoteCreateOrEditQueueOperation(
            wrapper = synchronizableItemWrapper,
            errorInterceptor = errorInterceptor,
            conflictResolutionUseCase = conflictResolutionUseCase,
            updateUploadStatusUseCase = updateUploadStatusUseCase
        )
    }
}

/**
 * Creates or edits [SynchronizableItemWrapper] on the backend
 *
 * This operation does not react to operation canceled
 */
internal class RemoteCreateOrEditQueueOperation
constructor(
    @VisibleForTesting val wrapper: SynchronizableItemWrapper,
    private val errorInterceptor: CreateOrEditHttpErrorInterceptor,
    private val conflictResolutionUseCase: ConflictResolutionUseCase,
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase
) : QueueOperation() {
    override fun run() {
        bundleForTrackingEntity(wrapper.trackingEntity)?.run {
            try {
                val remoteSynchronizable = api.createOrEdit(wrapper)

                val conflictResolution = conflictStrategy.resolve(wrapper, remoteSynchronizable)

                conflictResolutionUseCase.resolve(conflictResolution, this)
            } catch (e: Throwable) {
                errorInterceptor.intercept(
                    throwable = e,
                    wrapper = wrapper,
                    bundle = this
                )
            }
        }
    }

    override fun onOperationNotRun() {
        bundleForTrackingEntity(wrapper.trackingEntity)?.run {
            updateUploadStatusUseCase.update(wrapper, this, UploadStatus.PENDING)
        }
    }
}
