/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.usecases

import com.android.synchronizator.synchronizableItem
import com.android.synchronizator.synchronizableItemWrapper
import com.android.synchronizator.synchronizableTrackingEntity
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.synchronizator.data.SynchronizableTrackingEntityDataStore
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import com.kolibree.android.synchronizator.models.UploadStatus.IN_PROGRESS
import com.kolibree.android.synchronizator.models.UploadStatus.PENDING
import com.kolibree.android.synchronizator.models.UploadStatus.values
import com.kolibree.android.test.extensions.setFixedDate
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

internal class FilterPendingWrapperUseCaseTest : BaseUnitTest() {
    private val dataStore: SynchronizableTrackingEntityDataStore = mock()
    private val useCase = FilterPendingWrapperUseCase(dataStore)

    override fun setup() {
        super.setup()

        TrustedClock.setFixedDate(now)
    }

    @Test
    fun `when uploadStatus is not PENDING or IN_PROGRESS, then always return null`() {
        values()
            .filterNot { it == PENDING || it == IN_PROGRESS }
            .forEach { uploadStatus ->
                assertNull(useCase.nullUnlessPending(wrapperWithUploadStatus(uploadStatus)))
                assertNull(
                    useCase.nullUnlessPending(wrapperWithUploadStatus(uploadStatus, oldTime))
                )
            }
    }

    @Test
    fun `when uploadStatus is PENDING, then always return the same instance`() {
        val wrapper = wrapperWithUploadStatus(PENDING)
        val oldWrapper = wrapperWithUploadStatus(PENDING, oldTime)

        assertEquals(wrapper, useCase.nullUnlessPending(wrapper))
        assertEquals(oldWrapper, useCase.nullUnlessPending(oldWrapper))
    }

    @Test
    fun `when uploadStatus is IN_PROGRESS and updatedAt is newer than IN_PROGRESS_MINUTES_THRESHOLD, then return null`() {
        (0 until IN_PROGRESS_MINUTES_THRESHOLD)
            .map { now.minusMinutes(it) }
            .forEach { updatedAt ->
                assertNull(
                    useCase.nullUnlessPending(wrapperWithUploadStatus(IN_PROGRESS, updatedAt))
                )
            }

        verifyNoMoreInteractions(dataStore)
    }

    @Test
    fun `when uploadStatus is IN_PROGRESS and updatedAt is equal or older than IN_PROGRESS_MINUTES_THRESHOLD, then return the instance and update the entity with uploadStatus = PENDING`() {
        (IN_PROGRESS_MINUTES_THRESHOLD until 10)
            .map { now.minusMinutes(it).minusSeconds(1) }
            .forEach { updatedAt ->
                val wrapper = wrapperWithUploadStatus(IN_PROGRESS, updatedAt)

                val expectedWrapper = wrapper.withUploadStatus(PENDING)
                assertEquals(
                    expectedWrapper,
                    useCase.nullUnlessPending(wrapper)
                )

                verify(dataStore).update(expectedWrapper.trackingEntity)
            }
    }

    /**
     * shouldConsiderInProgressAsPending
     */

    @Test
    fun shouldConsiderInProgressAsPending_isImmuneToTimeZoneDifferences() {
        TrustedClock.setFixedDate()

        val nowInUtc = TrustedClock.getNowZonedDateTimeUTC()
        val nowInNewYork = TrustedClock.getNowZonedDateTime()
            .withZoneSameInstant(ZoneOffset.ofHours(-5))
        val nowInTokyo = TrustedClock.getNowZonedDateTime()
            .withZoneSameInstant(ZoneOffset.ofHours(9))

        val unexpectedItem1 = wrapper(
            uploadStatus = IN_PROGRESS,
            isDeletedLocally = true,
            updatedAt = nowInUtc.minusMinutes(IN_PROGRESS_MINUTES_THRESHOLD)
        )
        val unexpectedItem2 = wrapper(
            uploadStatus = IN_PROGRESS,
            isDeletedLocally = true,
            updatedAt = nowInNewYork.minusMinutes(IN_PROGRESS_MINUTES_THRESHOLD)
        )
        val unexpectedItem3 = wrapper(
            uploadStatus = IN_PROGRESS,
            isDeletedLocally = true,
            updatedAt = nowInTokyo.minusMinutes(IN_PROGRESS_MINUTES_THRESHOLD)
        )

        val expectedItem1 = wrapper(
            uploadStatus = IN_PROGRESS,
            isDeletedLocally = true,
            updatedAt = nowInUtc
                .minusMinutes(IN_PROGRESS_MINUTES_THRESHOLD)
                .minusSeconds(1)
        )
        val expectedItem2 = wrapper(
            uploadStatus = IN_PROGRESS,
            isDeletedLocally = true,
            updatedAt = nowInNewYork
                .minusMinutes(IN_PROGRESS_MINUTES_THRESHOLD)
                .minusSeconds(1)
        )
        val expectedItem3 = wrapper(
            uploadStatus = IN_PROGRESS,
            isDeletedLocally = true,
            updatedAt = nowInTokyo
                .minusMinutes(IN_PROGRESS_MINUTES_THRESHOLD)
                .minusSeconds(1)
        )

        listOf(expectedItem1, expectedItem2, expectedItem3)
            .map { it.withUploadStatus(PENDING) }
            .forEach { wrapper -> assertEquals(wrapper, useCase.nullUnlessPending(wrapper)) }

        assertNull(useCase.nullUnlessPending(unexpectedItem1))
        assertNull(useCase.nullUnlessPending(unexpectedItem2))
        assertNull(useCase.nullUnlessPending(unexpectedItem3))
    }

    /*
    Utils
     */
    private fun wrapper(
        uploadStatus: UploadStatus,
        updatedAt: ZonedDateTime,
        isDeletedLocally: Boolean = false
    ): SynchronizableItemWrapper {
        val entity = synchronizableTrackingEntity(
            isDeletedLocally = isDeletedLocally,
            uploadStatus = uploadStatus
        )
        return synchronizableItemWrapper(
            item = synchronizableItem(updatedAt = updatedAt, uuid = entity.uuid),
            entity = entity
        )
    }

    private fun wrapperWithUploadStatus(
        uploadStatus: UploadStatus,
        updatedAt: ZonedDateTime = TrustedClock.getNowZonedDateTime()
    ): SynchronizableItemWrapper {
        val entity = synchronizableTrackingEntity(uploadStatus = uploadStatus)
        return synchronizableItemWrapper(
            entity = entity,
            item = synchronizableItem(uuid = entity.uuid, updatedAt = updatedAt)
        )
    }

    val now = TrustedClock.getNowZonedDateTime()
    val oldTime = now.minusMinutes(IN_PROGRESS_MINUTES_THRESHOLD + 1)
}

private fun List<SynchronizableItemWrapper>.withUploadStatus(uploadStatus: UploadStatus): List<SynchronizableItemWrapper> {
    return map {
        it.copy(
            trackingEntity = it.trackingEntity.withUploadStatus(
                uploadStatus
            )
        )
    }
}
