/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.usecases

import androidx.annotation.VisibleForTesting
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.synchronizator.data.SynchronizableTrackingEntityDataStore
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.data.database.updateWrapper
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus.IN_PROGRESS
import com.kolibree.android.synchronizator.models.UploadStatus.PENDING
import com.kolibree.android.synchronizator.operations.RemoteCreateOrEditQueueOperation
import javax.inject.Inject
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

/**
 * Helper to identify [SynchronizableItemWrapper] that should be considered as UploadStatus.PENDING
 *
 * This is true in two circumstances
 * 1) `uploadStatus` == PENDING
 * 2) `uploadStatus` == IN_PROGRESS, and `updatedAt` is too old
 *
 * The latter must be implemented to support the following scenario
 * 1. Run CreateOrEdit operation, which flags as [IN_PROGRESS] and enqueues a
 * [RemoteCreateOrEditQueueOperation]
 * 2. There's a crash and the [RemoteCreateOrEditQueueOperation] never runs
 *
 * In this scenario, the [SynchronizableTrackingEntity] would never be detected as pending
 * because it'll remain in [IN_PROGRESS] forever
 */
internal class FilterPendingWrapperUseCase @Inject constructor(
    private val synchronizableTrackingEntityDataStore: SynchronizableTrackingEntityDataStore
) {
    /**
     * @return [SynchronizableItemWrapper?] whose [SynchronizableTrackingEntity.uploadStatus] is
     * equal to [PENDING] or [IN_PROGRESS] and [SynchronizableItemWrapper.updatedAt] is before
     * (now - [IN_PROGRESS_MINUTES_THRESHOLD]) minutes
     */
    fun nullUnlessPending(wrapper: SynchronizableItemWrapper): SynchronizableItemWrapper? {
        return wrapper.run {
            when (uploadStatus) {
                PENDING -> this
                IN_PROGRESS -> maybeReturnWrapper()
                else -> null
            }
        }
    }

    private fun SynchronizableItemWrapper.maybeReturnWrapper(): SynchronizableItemWrapper? {
        return if (shouldConsiderInProgressAsPending(this))
            persistAsPending()
        else
            null
    }

    private fun SynchronizableItemWrapper.persistAsPending(): SynchronizableItemWrapper {
        val withUploadStatusPending = withUploadStatus(PENDING)

        synchronizableTrackingEntityDataStore.updateWrapper(withUploadStatusPending)

        return withUploadStatusPending
    }

    /**
     * @return true if [wrapper]'s updatedAt is before [Time.now() - [IN_PROGRESS_MINUTES_THRESHOLD]],
     * false otherwise
     */
    private fun shouldConsiderInProgressAsPending(wrapper: SynchronizableItemWrapper): Boolean {
        val updatedAtDelta =
            Duration.between(wrapper.updatedAt, TrustedClock.getNowZonedDateTimeUTC())

        return updatedAtDelta > maxDurationInProgress
    }
}

/**
 * Number of minutes after which we consider that a [SynchronizableTrackingEntity] with uploadStatus
 * [IN_PROGRESS] can be considered as if it had uploadStatus equal to [PENDING]
 */
@VisibleForTesting
internal const val IN_PROGRESS_MINUTES_THRESHOLD = 5L

private val maxDurationInProgress =
    Duration.of(IN_PROGRESS_MINUTES_THRESHOLD, ChronoUnit.MINUTES)
