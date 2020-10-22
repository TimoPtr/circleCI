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
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.offlinebrushings.BrushingSyncedResult
import com.kolibree.android.offlinebrushings.OfflineBrushingSyncedResult
import com.kolibree.android.offlinebrushings.OfflineBrushingsResourceProvider
import com.kolibree.android.offlinebrushings.OrphanBrushingSyncedResult
import com.kolibree.android.offlinebrushings.sync.job.OfflineBrushingNotificationContent.Companion.EMPTY
import com.kolibree.android.rewards.feedback.FeedbackAction
import com.kolibree.android.rewards.feedback.OfflineBrushingsSyncedFeedback
import com.kolibree.android.rewards.feedback.RewardsFeedback
import com.kolibree.android.rewards.synchronization.RewardsSynchronizationRegistrar
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.driver.ble.offlinebrushings.OfflineBrushing
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.sdkws.profile.ProfileManager
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime

/**
 * Maps the result of extracting brushings from all active [KLTBConnection] to [OfflineBrushingNotificationContent]
 *
 * Since smiles earned can only be obtained from the backend, we need to force a rewards synchronization
 * after subscribing to FeedbackAction for every profile
 */
internal class OfflineSyncResultProcessor @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val profileManager: ProfileManager,
    private val rewardsSynchronizationRegistrar: RewardsSynchronizationRegistrar,
    private val rewardsFeedback: RewardsFeedback,
    private val synchronizator: Synchronizator,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler,
    context: Context,
    private val resourceProvider: OfflineBrushingsResourceProvider
) {
    private val appContext = context.applicationContext

    /**
     * Asks the backend for the [FeedbackAction] to be displayed after creating brushing sessions
     * from [OfflineBrushing]
     *
     * It will emit [EMPTY] if, at the time the Single is run
     * - There's no connectivity
     * - We didn't extract any brushing
     * - We don't receive any [FeedbackAction] after [FEEDBACK_TIMEOUT_SECONDS] seconds
     *
     * @return [Single]<[OfflineBrushingNotificationContent]>. If it emits [EMPTY], clients
     * shouldn't create a notification from this synchronization
     */
    fun createNotificationContent(
        syncResult: List<BrushingSyncedResult>
    ): Single<OfflineBrushingNotificationContent> {
        return shouldCreateNotification(syncResult)
            .flatMap { shouldCreateNotification ->
                if (shouldCreateNotification) {
                    notificationContentSingle(syncResult)
                } else {
                    Single.just(EMPTY)
                }
            }
    }

    @VisibleForTesting
    fun notificationContentSingle(
        syncResult: List<BrushingSyncedResult>
    ): Single<OfflineBrushingNotificationContent> {
        if (syncResult.hasZeroBrushings()) {
            return Single.just(EMPTY)
        } else if (syncResult.onlyHasOrphanBrushings()) {
            return createOrphanNotificationContent(syncResult)
        }

        return createOfflineBrushingNotification(syncResult)
    }

    @VisibleForTesting
    fun createOfflineBrushingNotification(
        syncResult: List<BrushingSyncedResult>
    ): Single<OfflineBrushingNotificationContent> {
        return offlineBrushingSessionFeedbackStream()
            .doOnSubscribe { forceRewardsSynchronization() }
            .buffer(FEEDBACK_TIMEOUT_SECONDS, TimeUnit.SECONDS, timeoutScheduler)
            .take(1)
            .singleOrError()
            .map { createContentFromFeedback(it, syncResult) }
    }

    @VisibleForTesting
    fun createContentFromFeedback(
        feedbackFromBackend: List<OfflineBrushingsSyncedFeedback>,
        syncResult: List<BrushingSyncedResult>
    ): OfflineBrushingNotificationContent {
        if (feedbackFromBackend.isEmpty()) return EMPTY

        val totalBrushings = feedbackFromBackend.totalBrushings()
        val earnedSmiles = feedbackFromBackend.earnedSmiles()

        val (title, message) = messageFromBrushingsAndSmiles(totalBrushings, earnedSmiles)

        return OfflineBrushingNotificationContent(
            title = title,
            message = message,
            offlineBrushingsDateTimes = syncResult.offlineBrushingsDateTimes(),
            orphanBrushingsDateTimes = syncResult.orphanBrushingsDateTimes()
        )
    }

    /**
     * See https://kolibree.atlassian.net/browse/KLTB002-3061 for logic implemented
     */
    @VisibleForTesting
    fun messageFromBrushingsAndSmiles(
        totalBrushingsExtracted: Int,
        earnedSmiles: Int
    ): Pair<String, String> {
        val title: String
        val message: String

        if (totalBrushingsExtracted > 1) {
            if (earnedSmiles > 0) {
                title = appContext.getString(
                    resourceProvider.multipleBrushingWithSmilesNotificationTitle,
                    totalBrushingsExtracted.toString()
                )
                message = appContext.getString(
                    resourceProvider.multipleBrushingWithSmilesNotificationBody,
                    earnedSmiles.toString()
                )
            } else {
                title = appContext.getString(
                    resourceProvider.multipleBrushingNoSmilesNotificationTitle,
                    totalBrushingsExtracted.toString()
                )
                message =
                    appContext.getString(resourceProvider.multipleBrushingNoSmilesNotificationBody)
            }
        } else {
            if (earnedSmiles > 0) {
                title =
                    appContext.getString(resourceProvider.singleBrushingWithSmilesNotificationTitle)
                message = appContext.getString(
                    resourceProvider.singleBrushingWithSmilesNotificationBody,
                    earnedSmiles.toString()
                )
            } else {
                title =
                    appContext.getString(resourceProvider.singleBrushingNoSmilesNotificationTitle)
                message =
                    appContext.getString(resourceProvider.singleBrushingNoSmilesNotificationBody)
            }
        }

        return Pair(title, message)
    }

    @VisibleForTesting
    fun shouldCreateNotification(
        syncResult: List<BrushingSyncedResult>
    ): Single<Boolean> = Single.fromCallable {
        networkChecker.hasConnectivity() && syncResult.isNotEmpty()
    }

    /**
     * @return [Flowable]<[OfflineBrushingsSyncedFeedback]> that will emit the feedback that results
     * from the creation of our just sent offline brushings
     */
    @VisibleForTesting
    fun offlineBrushingSessionFeedbackStream(): Flowable<OfflineBrushingsSyncedFeedback> {
        return profileManager.getProfilesLocally()
            .flatMapPublisher { profiles ->
                Flowable.merge(profiles.map { profile ->
                    rewardsFeedback.feedbackAction(profile.id)
                        .filter { it is OfflineBrushingsSyncedFeedback }
                        .map { it as OfflineBrushingsSyncedFeedback }
                        .flatMap {
                            rewardsFeedback.markAsRead(it)
                                .andThen(Flowable.just(it))
                        }
                })
            }
    }

    @VisibleForTesting
    fun forceRewardsSynchronization() {
        rewardsSynchronizationRegistrar.register()

        synchronizator.synchronize()
    }

    fun createOrphanNotificationContent(
        syncResult: List<BrushingSyncedResult>
    ): Single<OfflineBrushingNotificationContent> {
        return Single.fromCallable {
            val orphanBrushingDates = syncResult.orphanBrushingsDateTimes()
            val totalOrphanBrushings = orphanBrushingDates.size

            val title = if (totalOrphanBrushings == 1) {
                appContext.getString(resourceProvider.singleBrushingWithSmilesNotificationTitle)
            } else {
                appContext.getString(
                    resourceProvider.multipleBrushingWithSmilesNotificationTitle,
                    totalOrphanBrushings.toString()
                )
            }

            OfflineBrushingNotificationContent(
                title = title,
                message = null,
                orphanBrushingsDateTimes = orphanBrushingDates,
                offlineBrushingsDateTimes = listOf()
            )
        }
    }
}

@VisibleForTesting
internal const val FEEDBACK_TIMEOUT_SECONDS = 30L

@VisibleForTesting
internal fun List<BrushingSyncedResult>.hasZeroBrushings(): Boolean = isEmpty()

@VisibleForTesting
internal fun List<BrushingSyncedResult>.onlyHasOrphanBrushings(): Boolean =
    none { it is OfflineBrushingSyncedResult } && isNotEmpty()

private fun List<BrushingSyncedResult>.offlineBrushingsDateTimes(): List<OffsetDateTime> =
    filterIsInstance(OfflineBrushingSyncedResult::class.java).map {
        it.dateTime
    }

@VisibleForTesting
internal fun List<BrushingSyncedResult>.orphanBrushingsDateTimes(): List<OffsetDateTime> =
    filterIsInstance(OrphanBrushingSyncedResult::class.java).map {
        it.dateTime
    }

private fun List<OfflineBrushingsSyncedFeedback>.totalBrushings() = sumBy { it.offlineBrushings }
private fun List<OfflineBrushingsSyncedFeedback>.earnedSmiles() = sumBy { it.earnedSmiles }
