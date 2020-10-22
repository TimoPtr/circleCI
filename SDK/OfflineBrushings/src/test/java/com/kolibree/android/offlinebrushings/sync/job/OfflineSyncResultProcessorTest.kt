/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.content.Context
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.offlinebrushings.BrushingSyncedResult
import com.kolibree.android.offlinebrushings.OfflineBrushingsResourceProvider
import com.kolibree.android.offlinebrushings.createOfflineBrushingSyncedResult
import com.kolibree.android.offlinebrushings.createOrphanBrushingSyncedResult
import com.kolibree.android.offlinebrushings.sync.job.OfflineBrushingNotificationContent.Companion.EMPTY
import com.kolibree.android.rewards.feedback.ChallengeCompletedFeedback
import com.kolibree.android.rewards.feedback.FeedbackAction
import com.kolibree.android.rewards.feedback.NoFeedback
import com.kolibree.android.rewards.feedback.NoSmilesEarnedFeedback
import com.kolibree.android.rewards.feedback.OfflineBrushingsSyncedFeedback
import com.kolibree.android.rewards.feedback.RewardsFeedback
import com.kolibree.android.rewards.feedback.SmilesEarnedFeedback
import com.kolibree.android.rewards.feedback.StreakCompletedFeedback
import com.kolibree.android.rewards.feedback.TierReachedFeedback
import com.kolibree.android.rewards.synchronization.RewardsSynchronizationRegistrar
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createTier
import com.kolibree.sdkws.profile.ProfileManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.OffsetDateTime

class OfflineSyncResultProcessorTest : BaseUnitTest() {
    private val scheduler = TestScheduler()

    private val networkChecker: NetworkChecker = mock()
    private val profileManager: ProfileManager = mock()
    private val rewardsSynchronizationRegistrar: RewardsSynchronizationRegistrar = mock()
    private val rewardsFeedback: RewardsFeedback = mock()
    private val synchronizator: Synchronizator = mock()
    private val context: Context = mock<Context>().apply {
        whenever(this.applicationContext).thenReturn(this)
    }
    private val resourceProvider: OfflineBrushingsResourceProvider = mock()

    private var processor = OfflineSyncResultProcessor(
        networkChecker,
        profileManager,
        rewardsSynchronizationRegistrar,
        rewardsFeedback,
        synchronizator,
        scheduler,
        context,
        resourceProvider
    )

    @Test
    fun `createNotificationContent emits EMPTY if shouldCreateNotification returns false`() {
        spy()

        doReturn(Single.just(false)).whenever(processor).shouldCreateNotification(any())

        processor.createNotificationContent(listOf()).test().assertValue(EMPTY)
    }

    @Test
    fun `createNotificationContent never invokes notificationContentSingle if shouldCreateNotification returns false`() {
        spy()

        doReturn(Single.just(false)).whenever(processor).shouldCreateNotification(any())

        processor.createNotificationContent(listOf()).test()

        verify(processor, never()).notificationContentSingle(any())
    }

    @Test
    fun `createNotificationContent invokes notificationContentSingle if shouldCreateNotification returns true`() {
        spy()

        doReturn(Single.just(true)).whenever(processor).shouldCreateNotification(any())

        val expectedList: List<BrushingSyncedResult> = listOf()
        val subject = SingleSubject.create<OfflineBrushingNotificationContent>()
        doReturn(subject).whenever(processor).notificationContentSingle(expectedList)

        processor.createNotificationContent(expectedList).test()

        verify(processor).notificationContentSingle(expectedList)
        assertTrue(subject.hasObservers())
    }

    /*
    createOfflineBrushingNotification
     */

    @Test
    fun `createOfflineBrushingNotification invokes forceRewardsSynchronization if shouldCreateNotification returns true`() {
        spy()

        doReturn(Flowable.never<OfflineBrushingsSyncedFeedback>())
            .whenever(processor)
            .offlineBrushingSessionFeedbackStream()

        processor.createOfflineBrushingNotification(listOf()).test()

        verify(processor).forceRewardsSynchronization()
    }

    @Test
    fun `createOfflineBrushingNotification emits EMPTY if nothing is emitted after FEEDBACK_TIMEOUT_SECONDS`() {
        spy()

        val feedbackSubject = PublishProcessor.create<OfflineBrushingsSyncedFeedback>()
        doReturn(feedbackSubject).whenever(processor).offlineBrushingSessionFeedbackStream()

        val observer = processor.createOfflineBrushingNotification(listOf()).test()
            .assertEmpty()

        scheduler.advanceTimeBy(FEEDBACK_TIMEOUT_SECONDS + 1, TimeUnit.SECONDS)

        observer.assertValue(EMPTY).assertComplete()
    }

    @Test
    fun `createOfflineBrushingNotification returns value from createContentFromFeedback emitted before FEEDBACK_TIMEOUT_SECONDS`() {
        spy()

        val expectedTitle = "title"
        val expectedMessage = "dasdasdas"
        doReturn(Pair(expectedTitle, expectedMessage)).whenever(processor)
            .messageFromBrushingsAndSmiles(any(), any())
        val expectedContent =
            createOfflineBrushingNotificationContent(expectedTitle, expectedMessage)

        val feedbackSubject = PublishProcessor.create<OfflineBrushingsSyncedFeedback>()
        doReturn(feedbackSubject).whenever(processor).offlineBrushingSessionFeedbackStream()

        val expectedSyncResultList = emptyList<BrushingSyncedResult>()
        val observer = processor.createOfflineBrushingNotification(expectedSyncResultList).test()
            .assertEmpty()

        val brushingsFirstFeedback = 5
        val firstFeedback = createOfflineBrushingsFeedback(nbOFBrushings = brushingsFirstFeedback)
        feedbackSubject.onNext(firstFeedback)

        scheduler.advanceTimeBy(FEEDBACK_TIMEOUT_SECONDS - 5, TimeUnit.SECONDS)

        observer.assertEmpty()

        val brushingsSecondFeedback = 2
        val secondFeedback = createOfflineBrushingsFeedback(nbOFBrushings = brushingsSecondFeedback)
        feedbackSubject.onNext(secondFeedback)

        scheduler.advanceTimeBy(FEEDBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        observer.assertValue(expectedContent).assertComplete()

        verify(processor).createContentFromFeedback(
            listOf(firstFeedback, secondFeedback),
            expectedSyncResultList
        )
    }

    /*
    This is more an unfortunate consequence than something we want, but we need to complete the
    Flowable to be able to invoke singleOrError
     */
    @Test
    fun `createOfflineBrushingNotification returns ignores values emitted after FEEDBACK_TIMEOUT_SECONDS`() {
        spy()

        val feedbackSubject = PublishProcessor.create<OfflineBrushingsSyncedFeedback>()
        doReturn(feedbackSubject).whenever(processor).offlineBrushingSessionFeedbackStream()

        val observer = processor.createOfflineBrushingNotification(listOf()).test()
            .assertEmpty()

        scheduler.advanceTimeBy(FEEDBACK_TIMEOUT_SECONDS + 1, TimeUnit.SECONDS)

        feedbackSubject.onNext(createOfflineBrushingsFeedback())

        observer.assertValue(EMPTY).assertComplete()
    }

    /*
    notificationContentSingle
     */
    @Test
    fun `notificationContentSingle returns EMPTY if we didn't synchronize any offline or orphan brushing`() {
        processor.notificationContentSingle(emptyList()).test().assertValue(EMPTY)
    }

    @Test
    fun `notificationContentSingle returns OfflineBrushingNotificationContent from createOrphanNotificationContent if only synchronized orphan brushings`() {
        spy()

        val syncedList = listOf(
            createOrphanBrushingSyncedResult(),
            createOrphanBrushingSyncedResult(),
            createOrphanBrushingSyncedResult()
        )

        val expectedNotificationContent = createOfflineBrushingNotificationContent()
        doReturn(Single.just(expectedNotificationContent)).whenever(processor)
            .createOrphanNotificationContent(syncedList)

        processor.notificationContentSingle(syncedList).test()
            .assertValue(expectedNotificationContent)
    }

    @Test
    fun `notificationContentSingle returns OfflineBrushingNotificationContent from createOfflineBrushingNotification if only synchronized orphan brushings`() {
        spy()

        val syncedList = listOf(
            createOfflineBrushingSyncedResult(),
            createOfflineBrushingSyncedResult(),
            createOfflineBrushingSyncedResult()
        )

        val expectedNotificationContent = createOfflineBrushingNotificationContent()
        doReturn(Single.just(expectedNotificationContent)).whenever(processor)
            .createOfflineBrushingNotification(syncedList)

        processor.notificationContentSingle(syncedList).test()
            .assertValue(expectedNotificationContent)
    }

    /*
    createOrphanNotificationContent
     */
    @Test
    fun `createOrphanNotificationContent OfflineBrushingNotificationContent only with multi synced title if there's more than 1 orphan brushing synced`() {
        val syncedList = listOf(
            createOrphanBrushingSyncedResult(),
            createOrphanBrushingSyncedResult(),
            createOrphanBrushingSyncedResult()
        )

        val title = "tittle"
        val titleId = 1024
        whenever(resourceProvider.multipleBrushingWithSmilesNotificationTitle).thenReturn(titleId)
        whenever(
            context.getString(
                titleId,
                3.toString()
            )
        ).thenReturn(title)

        val expectedNotificationContent = createOfflineBrushingNotificationContent(
            title = title,
            orphanBrushingsDateTimes = syncedList.orphanBrushingsDateTimes()
        )

        processor.createOrphanNotificationContent(syncedList).test()
            .assertValue(expectedNotificationContent)
    }

    @Test
    fun `createOrphanNotificationContent OfflineBrushingNotificationContent only with single synced title if there's only 1 orphan brushing synced`() {
        val syncedList = listOf(
            createOrphanBrushingSyncedResult()
        )

        val title = "tittle"
        val titleId = 1024
        whenever(resourceProvider.singleBrushingWithSmilesNotificationTitle).thenReturn(titleId)
        whenever(context.getString(titleId))
            .thenReturn(title)

        val expectedNotificationContent = createOfflineBrushingNotificationContent(
            title = title,
            orphanBrushingsDateTimes = syncedList.orphanBrushingsDateTimes()
        )

        processor.createOrphanNotificationContent(syncedList).test()
            .assertValue(expectedNotificationContent)
    }

    /*
    createContentFromFeedback
     */
    @Test
    fun `createContentFromFeedback returns EMPTY if feedbackFromBackend list is empty`() {
        assertEquals(
            EMPTY,
            processor.createContentFromFeedback(
                listOf(),
                listOf()
            )
        )
    }

    @Test
    fun `createContentFromFeedback invokes messageFromBrushingsAndSmiles with expected parameters`() {
        spy()

        val brushingsFirstFeedback = 5
        val smilesFirstFeedback = 543534
        val firstFeedback = createOfflineBrushingsFeedback(
            nbOFBrushings = brushingsFirstFeedback,
            earnedSmiles = smilesFirstFeedback
        )

        val brushingsSecondFeedback = 2
        val smilesSecondFeedback = 13
        val secondFeedback = createOfflineBrushingsFeedback(
            nbOFBrushings = brushingsSecondFeedback,
            earnedSmiles = smilesSecondFeedback
        )

        doReturn(Pair("", "")).whenever(processor).messageFromBrushingsAndSmiles(any(), any())

        processor.createContentFromFeedback(listOf(firstFeedback, secondFeedback), listOf())

        verify(processor).messageFromBrushingsAndSmiles(
            brushingsFirstFeedback + brushingsSecondFeedback,
            smilesFirstFeedback + smilesSecondFeedback
        )
    }

    @Test
    fun `createContentFromFeedback returns OfflineBrushingNotificationContent with expected message and LocalDateTimes`() {
        spy()

        val brushingsFirstFeedback = 5
        val smilesFirstFeedback = 543534
        val feedback = createOfflineBrushingsFeedback(
            nbOFBrushings = brushingsFirstFeedback,
            earnedSmiles = smilesFirstFeedback
        )

        val expectedTitle = "my tittle"
        val expectedMessage = "dada"
        doReturn(Pair(expectedTitle, expectedMessage)).whenever(processor)
            .messageFromBrushingsAndSmiles(any(), any())

        val expectedOfflineDateTimes = listOf(
            TrustedClock.getNowOffsetDateTime(),
            TrustedClock.getNowOffsetDateTime().minusDays(3)
        )

        val expectedOrphanDateTimes = listOf(
            TrustedClock.getNowOffsetDateTime(),
            TrustedClock.getNowOffsetDateTime().minusHours(14)
        )

        val offlineBrushingSync = expectedOfflineDateTimes.map { createOfflineBrushingSyncedResult(time = it) }
        val orphanBrushingSync = expectedOrphanDateTimes.map { createOrphanBrushingSyncedResult(time = it) }

        assertEquals(
            createOfflineBrushingNotificationContent(
                title = expectedTitle,
                content = expectedMessage,
                offlineBrushingsDateTimes = expectedOfflineDateTimes,
                orphanBrushingsDateTimes = expectedOrphanDateTimes
            ),
            processor.createContentFromFeedback(
                listOf(feedback),
                offlineBrushingSync + orphanBrushingSync
            )
        )
    }

    /*
    shouldCreateNotification
     */
    @Test
    fun `shouldCreateNotification returns false if there's no connectivity`() {
        whenever(networkChecker.hasConnectivity()).thenReturn(false)

        processor.shouldCreateNotification(listOf()).test().assertValue(false)
    }

    @Test
    fun `shouldCreateNotification returns false if there's connectivity but we extracted 0 offline brushings`() {
        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        processor.shouldCreateNotification(emptyList())
            .test().assertValue(false)
    }

    @Test
    fun `shouldCreateNotification returns true if there's connectivity but we extracted at least 1 offline brushings`() {
        whenever(networkChecker.hasConnectivity()).thenReturn(true)

        processor.shouldCreateNotification(
            listOf(
                createOfflineBrushingSyncedResult()
            )
        )
            .test().assertValue(true)
    }

    /*
    offlineBrushingSessionFeedbackStream
     */

    @Test
    fun `offlineBrushingSessionFeedbackStream only flags as read OfflineBrushingsSyncedFeedback`() {
        val peterId = 3L
        val peter = ProfileBuilder.create().withId(peterId).build()
        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(peter)))

        val subject = PublishProcessor.create<FeedbackAction>()
        whenever(rewardsFeedback.feedbackAction(peterId)).thenReturn(subject)

        processor.offlineBrushingSessionFeedbackStream().test()

        val noSmilesEarned = NoSmilesEarnedFeedback(peterId)
        subject.onNext(noSmilesEarned)

        val smilesEarned = SmilesEarnedFeedback(peterId, 43)
        subject.onNext(smilesEarned)

        val challengeCompleted = ChallengeCompletedFeedback(peterId, listOf())
        subject.onNext(challengeCompleted)

        val tierReachedFeedback = TierReachedFeedback(peterId, createTier(), listOf())
        subject.onNext(tierReachedFeedback)

        val streakCompletedFeedback = StreakCompletedFeedback(peterId, 0)
        subject.onNext(streakCompletedFeedback)

        val noFeedback = NoFeedback
        subject.onNext(noFeedback)

        val offlineBrushingsSyncedFeedback1 = OfflineBrushingsSyncedFeedback(peterId, 0, 0)
        val offlineBrushingsSyncedFeedback2 = OfflineBrushingsSyncedFeedback(peterId, 1, 1)

        val markAsReadOfflineSubject = CompletableSubject.create()
        whenever(rewardsFeedback.markAsRead(any<OfflineBrushingsSyncedFeedback>()))
            .thenReturn(markAsReadOfflineSubject)

        verify(rewardsFeedback, never()).markAsRead(noSmilesEarned)
        verify(rewardsFeedback, never()).markAsRead(smilesEarned)
        verify(rewardsFeedback, never()).markAsRead(challengeCompleted)
        verify(rewardsFeedback, never()).markAsRead(tierReachedFeedback)
        verify(rewardsFeedback, never()).markAsRead(streakCompletedFeedback)
        verify(rewardsFeedback, never()).markAsRead(noFeedback)

        subject.onNext(offlineBrushingsSyncedFeedback1)
        verify(rewardsFeedback).markAsRead(offlineBrushingsSyncedFeedback1)

        subject.onNext(offlineBrushingsSyncedFeedback2)
        verify(rewardsFeedback).markAsRead(offlineBrushingsSyncedFeedback2)

        assertTrue(markAsReadOfflineSubject.hasObservers())
    }

    @Test
    fun `offlineBrushingSessionFeedbackStream receives feedback from all profiles`() {
        val peterId = 3L
        val peter = ProfileBuilder.create().withId(peterId).build()
        val johnId = 2L
        val john = ProfileBuilder.create().withId(johnId).build()
        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(peter, john)))

        val peterSubject = PublishProcessor.create<FeedbackAction>()
        whenever(rewardsFeedback.feedbackAction(peterId)).thenReturn(peterSubject)

        val johnSubject = PublishProcessor.create<FeedbackAction>()
        whenever(rewardsFeedback.feedbackAction(johnId)).thenReturn(johnSubject)

        val observer = processor.offlineBrushingSessionFeedbackStream().test().assertEmpty()

        val offlineBrushingsSyncedPeter = OfflineBrushingsSyncedFeedback(peterId, 0, 0)
        val offlineBrushingsSyncedJohn = OfflineBrushingsSyncedFeedback(johnId, 1, 1)

        whenever(rewardsFeedback.markAsRead(any<OfflineBrushingsSyncedFeedback>()))
            .thenReturn(Completable.complete())

        johnSubject.onNext(offlineBrushingsSyncedJohn)

        observer.assertValue(offlineBrushingsSyncedJohn)

        peterSubject.onNext(offlineBrushingsSyncedPeter)

        observer.assertValues(offlineBrushingsSyncedJohn, offlineBrushingsSyncedPeter)
    }

    /*
    forceRewardsSynchronization
     */
    @Test
    fun `forceRewardsSynchronization registers rewards registrar and then invokes synchronize`() {
        processor.forceRewardsSynchronization()

        inOrder(rewardsSynchronizationRegistrar, synchronizator) {
            verify(rewardsSynchronizationRegistrar).register()
            verify(synchronizator).synchronize()
        }
    }

    /*
    messageFromBrushingsAndSmiles
     */
    @Test
    fun `messageFromBrushingsAndSmiles returns offline_brushing_notification_multiple_with_smiles when smiles greater than 0 and brushings greater than 1`() {
        val expectedTitle = "tittitl"
        val expectedMessage = "dada"
        val totalBrushings = 2
        val earnedSmiles = 5

        val titleId = 1024
        val bodyId = 1025
        whenever(resourceProvider.multipleBrushingWithSmilesNotificationBody).thenReturn(bodyId)
        whenever(
            context.getString(
                bodyId,
                earnedSmiles.toString()
            )
        ).thenReturn(expectedMessage)

        whenever(resourceProvider.multipleBrushingWithSmilesNotificationTitle).thenReturn(titleId)
        whenever(
            context.getString(
                titleId,
                totalBrushings.toString()
            )
        ).thenReturn(expectedTitle)

        assertEquals(
            Pair(expectedTitle, expectedMessage),
            processor.messageFromBrushingsAndSmiles(totalBrushings, earnedSmiles)
        )
    }

    @Test
    fun `messageFromBrushingsAndSmiles returns offline_brushing_notification_multiple_no_smiles when smiles = 0 and brushings greater than 1`() {
        val expectedTitle = "tittitl"
        val expectedMessage = "dada"
        val totalBrushings = 2
        val earnedSmiles = 0

        val titleId = 1024
        val bodyId = 1025
        whenever(resourceProvider.multipleBrushingNoSmilesNotificationTitle).thenReturn(titleId)
        whenever(
            context.getString(
                titleId,
                totalBrushings.toString()
            )
        ).thenReturn(expectedTitle)

        whenever(resourceProvider.multipleBrushingNoSmilesNotificationBody).thenReturn(bodyId)
        whenever(context.getString(bodyId))
            .thenReturn(expectedMessage)

        assertEquals(
            Pair(expectedTitle, expectedMessage),
            processor.messageFromBrushingsAndSmiles(totalBrushings, earnedSmiles)
        )
    }

    @Test
    fun `messageFromBrushingsAndSmiles returns offline_brushing_notification_single_with_smiles when smiles is greater than 0 and brushings = 1`() {
        val expectedTitle = "tittitl"
        val expectedMessage = "dada"
        val totalBrushings = 1
        val earnedSmiles = 5

        val titleId = 1024
        val bodyId = 1025
        whenever(resourceProvider.singleBrushingWithSmilesNotificationTitle).thenReturn(titleId)
        whenever(context.getString(titleId))
            .thenReturn(expectedTitle)

        whenever(resourceProvider.singleBrushingWithSmilesNotificationBody).thenReturn(bodyId)
        whenever(
            context.getString(
                bodyId,
                earnedSmiles.toString()
            )
        ).thenReturn(expectedMessage)

        assertEquals(
            Pair(expectedTitle, expectedMessage),
            processor.messageFromBrushingsAndSmiles(totalBrushings, earnedSmiles)
        )
    }

    @Test
    fun `messageFromBrushingsAndSmiles returns offline_brushing_notification_single_no_smiles when smiles is 0 and brushings = 0`() {
        val expectedTitle = "tittitl"
        val expectedMessage = "dada"
        val totalBrushings = 0
        val earnedSmiles = 0

        val titleId = 1024
        val bodyId = 1025
        whenever(resourceProvider.singleBrushingNoSmilesNotificationTitle).thenReturn(titleId)
        whenever(context.getString(titleId))
            .thenReturn(expectedTitle)

        whenever(resourceProvider.singleBrushingNoSmilesNotificationBody).thenReturn(bodyId)
        whenever(context.getString(bodyId))
            .thenReturn(expectedMessage)

        assertEquals(
            Pair(expectedTitle, expectedMessage),
            processor.messageFromBrushingsAndSmiles(totalBrushings, earnedSmiles)
        )
    }

    /*
    hasZeroBrushings
     */
    @Test
    fun `hasZeroBrushings returns true if all items in the list have 0 brushings`() {
        assertTrue(
            emptyList<BrushingSyncedResult>().hasZeroBrushings()
        )
    }

    @Test
    fun `hasZeroBrushings returns false if any item has 1 offline brushing`() {
        assertFalse(
            listOf(
                createOfflineBrushingSyncedResult()
            ).hasZeroBrushings()
        )
    }

    @Test
    fun `hasZeroBrushings returns false if any item has 1 orphan brushing`() {
        assertFalse(
            listOf(
                createOrphanBrushingSyncedResult()
            ).hasZeroBrushings()
        )
    }

    /*
    onlyHasOrphanBrushings
     */
    @Test
    fun `onlyHasOrphanBrushings returns false if all items in the list have 0 brushings`() {
        assertFalse(
            emptyList<BrushingSyncedResult>().onlyHasOrphanBrushings()
        )
    }

    @Test
    fun `onlyHasOrphanBrushings returns false if any item has 1 offline brushing`() {
        assertFalse(
            listOf(
                createOrphanBrushingSyncedResult(),
                createOfflineBrushingSyncedResult()
            ).onlyHasOrphanBrushings()
        )
    }

    @Test
    fun `onlyHasOrphanBrushings returns true if items only contain orphan brushings`() {
        assertTrue(
            listOf(
                createOrphanBrushingSyncedResult()
            ).onlyHasOrphanBrushings()
        )
    }

    /*
    Utils
     */

    private var feedbackId = 1L

    private fun spy() {
        processor = spy(processor)
    }

    private fun createOfflineBrushingsFeedback(
        nbOFBrushings: Int = 1,
        earnedSmiles: Int = 0
    ) = OfflineBrushingsSyncedFeedback(feedbackId++, nbOFBrushings, earnedSmiles)
}

internal fun createOfflineBrushingNotificationContent(
    title: String = "",
    content: String? = null,
    offlineBrushingsDateTimes: List<OffsetDateTime> = listOf(),
    orphanBrushingsDateTimes: List<OffsetDateTime> = listOf()
) = OfflineBrushingNotificationContent(
    title,
    content,
    offlineBrushingsDateTimes,
    orphanBrushingsDateTimes
)

internal fun createOfflineBrushingNotificationContentFromNumber(
    title: String = "",
    content: String? = null,
    totalOfflineBrushings: Int = 0,
    totalOrphanBrushings: Int = 0
) = createOfflineBrushingNotificationContent(
    title = title,
    content = content,
    offlineBrushingsDateTimes = createNSizeZonedDatetimeList(totalOfflineBrushings),
    orphanBrushingsDateTimes = createNSizeZonedDatetimeList(totalOrphanBrushings)
)

internal fun createNSizeZonedDatetimeList(size: Int): List<OffsetDateTime> =
    0.until(size).map { TrustedClock.getNowOffsetDateTime() }
