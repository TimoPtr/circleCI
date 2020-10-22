/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import androidx.annotation.VisibleForTesting
import com.kolibree.android.game.Game
import com.kolibree.android.rewards.models.AccountCreatedHistoryEvent
import com.kolibree.android.rewards.models.ActivityCompletedHistoryEvent
import com.kolibree.android.rewards.models.AmazonAccountLinkedEvent
import com.kolibree.android.rewards.models.BrushingSessionHistoryEvent
import com.kolibree.android.rewards.models.ChallengeCompletedHistoryEvent
import com.kolibree.android.rewards.models.NotificationTappedHistoryEvent
import com.kolibree.android.rewards.models.OfflineBrushingSessionHistoryEvent
import com.kolibree.android.rewards.models.PersonalChallengeCompletedHistoryEvent
import com.kolibree.android.rewards.models.QuestionOfTheDayAnsweredHistoryEvent
import com.kolibree.android.rewards.models.ReferralHistoryEvent
import com.kolibree.android.rewards.models.ShortTaskCompletedHistoryEvent
import com.kolibree.android.rewards.models.SmilesExpiredHistoryEvent
import com.kolibree.android.rewards.models.SmilesHistoryEvent
import com.kolibree.android.rewards.models.SmilesRedeemedHistoryEvent
import com.kolibree.android.rewards.models.SmilesTransferHistoryEvent
import com.kolibree.android.rewards.models.StreakCompletedHistoryEvent
import com.kolibree.android.rewards.models.TierReachedHistoryEvent
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.android.rewards.smileshistory.SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.Companion.fromBrushingSessionHistoryStatus
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.profile.ProfileManager
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * This extension transform a list of SmilesHistoryEvent to a list of SmilesHistoryItem.
 * It's a bit complex because for each event we need to resolve some dependencies with the
 * local database.
 *
 * To do that in an effective way here what we do :
 * 1. we group the events by kind using the class name
 * 2. we transform the list of event as an ObservableSource so it will emit each even as an Observable item
 * 3. for each kind of event we use the right method to create the list of item
 * 4. we reduce the item created into a set sorted by creationDate
 */
internal fun Flowable<List<SmilesHistoryEvent>>.mapToItem(
    profileManager: ProfileManager,
    brushingsRepository: BrushingsRepository,
    rewardsRepository: RewardsRepository,
    profileId: Long
): Flowable<List<SmilesHistoryItem>> =
    flatMapSingle { events ->
        groupEventObservable(events)
            .buildHistoryItem(profileManager, brushingsRepository, rewardsRepository, profileId)
            .sortByDate()
    }

/**
 * Do the building (resolving missing field from localDB) of the item from the event.
 * It only build the known events it just skip the other events.
 *
 * It assume that the list will contains only the same kind of elements (same java class)
 * otherwise it will skip items with differents types.
 * WARNING : This should only be called by this class or please use the groupEventObservable before
 */
@VisibleForTesting
internal fun Observable<List<SmilesHistoryEvent>>.buildHistoryItem(
    profileManager: ProfileManager,
    brushingsRepository: BrushingsRepository,
    rewardsRepository: RewardsRepository,
    profileId: Long
): Observable<List<SmilesHistoryItem>> {
    val emptyUnknownList = listOf(SmilesHistoryItem.UnknownHistoryItem)

    return flatMapSingle { eventsGroup: List<SmilesHistoryEvent> ->
        when (eventsGroup.first()) {
            is OfflineBrushingSessionHistoryEvent -> {
                buildOfflineBrushingSessionItems(eventsGroup)
            }
            is BrushingSessionHistoryEvent -> {
                buildBrushingSessionItems(
                    brushingsRepository,
                    eventsGroup,
                    profileId
                )
            }
            is ActivityCompletedHistoryEvent -> {
                buildActivityCompletedItems(eventsGroup)
            }
            is ShortTaskCompletedHistoryEvent -> {
                buildShortTaskCompletedItems(eventsGroup)
            }
            is ChallengeCompletedHistoryEvent -> {
                buildChallengeCompletedItems(
                    rewardsRepository,
                    eventsGroup,
                    profileId
                )
            }
            is PersonalChallengeCompletedHistoryEvent -> {
                buildPersonalChallengeCompletedItems(
                    rewardsRepository,
                    eventsGroup,
                    profileId
                )
            }
            is TierReachedHistoryEvent -> {
                buildTierReachedItems(
                    rewardsRepository,
                    eventsGroup
                )
            }
            is SmilesRedeemedHistoryEvent -> {
                buildSmilesRedeemedItems(
                    rewardsRepository,
                    eventsGroup
                )
            }
            is SmilesTransferHistoryEvent -> {
                buildSmilesTransferItems(
                    profileManager,
                    eventsGroup
                )
            }
            is StreakCompletedHistoryEvent -> {
                buildStreakCompletedItems(
                    eventsGroup
                )
            }
            is AccountCreatedHistoryEvent -> {
                buildAccountCreatedItems(
                    eventsGroup
                )
            }
            is SmilesExpiredHistoryEvent -> {
                buildSmilesExpiredItems(eventsGroup)
            }
            is ReferralHistoryEvent -> {
                buildReferralItems(eventsGroup)
            }
            is NotificationTappedHistoryEvent -> {
                buildNotificationTappedItems(eventsGroup)
            }
            is QuestionOfTheDayAnsweredHistoryEvent -> {
                buildQuestionOfTheDayAnsweredItems(eventsGroup)
            }
            is AmazonAccountLinkedEvent -> {
                buildAmazonAccountLinkedItems(eventsGroup)
            }
            else -> {
                Single.just(emptyUnknownList)
            }
        }
    }.filter { it != emptyUnknownList }
}

/**
 * Creates a Brushing item from a Brushing event
 */
@VisibleForTesting
internal fun buildBrushingSessionItems(
    brushingsRepository: BrushingsRepository,
    events: List<SmilesHistoryEvent>,
    profileId: Long
): Single<List<SmilesHistoryItem.BrushingSessionItem>> = brushingsRepository.getBrushings(profileId)
    .map { brushings ->
        // if we received a event which is not a BrushingSessionHistoryEvent do nothing
        events.mapNotNull { it as? BrushingSessionHistoryEvent }.map { event ->
            val brushing = brushings.find { it.kolibreeId == event.brushingId }
            SmilesHistoryItem.BrushingSessionItem(
                event.smiles,
                event.creationTime,
                brushing?.game?.let { Game.lookup(it) },
                fromBrushingSessionHistoryStatus(event.status)
            )
        }
    }

/**
 * Creates a Brushing item from a OfflineBrushingEvent
 */
@VisibleForTesting
internal fun buildOfflineBrushingSessionItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.BrushingSessionItem>> = Single.just(
    // if we received a event which is not a BrushingSessionHistoryEvent do nothing
    events.mapNotNull { it as? OfflineBrushingSessionHistoryEvent }.map { event ->
        SmilesHistoryItem.BrushingSessionItem(
            event.smiles,
            event.creationTime,
            Game.OFFLINE,
            fromBrushingSessionHistoryStatus(event.status)
        )
    }
)

/**
 * Creates a Challenge item from a Challenge event
 */
@VisibleForTesting
internal fun buildChallengeCompletedItems(
    rewardsRepository: RewardsRepository,
    events: List<SmilesHistoryEvent>,
    profileId: Long
): Single<List<SmilesHistoryItem.ChallengeCompletedItem>> =
    rewardsRepository.completedChallenges(profileId).toSingle()
        .map { challenges ->
            // if we received a event which is not a ChallengeCompletedHistoryEvent do nothing
            events.mapNotNull { it as? ChallengeCompletedHistoryEvent }.map { event ->
                val challenge = challenges.find { it.id == event.challengeId }
                SmilesHistoryItem.ChallengeCompletedItem(
                    event.smiles,
                    event.creationTime,
                    challenge?.name,
                    challenge?.pictureUrl
                )
            }
        }

/**
 * Creates a PersonalChallenge item from a PersonalChallenge event
 */
@VisibleForTesting
internal fun buildPersonalChallengeCompletedItems(
    rewardsRepository: RewardsRepository,
    events: List<SmilesHistoryEvent>,
    profileId: Long
): Single<List<SmilesHistoryItem.PersonalChallengeCompletedItem>> =
    rewardsRepository.completedChallenges(profileId).toSingle()
        .map { challenges ->
            // if we received a event which is not a TierReachedHistoryEvent do nothing
            events.mapNotNull { it as? PersonalChallengeCompletedHistoryEvent }.map { event ->
                val challenge = challenges.find { it.id == event.challengeId }
                SmilesHistoryItem.PersonalChallengeCompletedItem(
                    event.smiles,
                    event.creationTime,
                    challenge?.name,
                    challenge?.pictureUrl
                )
            }
        }

/**
 * Creates a Tier item from a Tier event
 */
@VisibleForTesting
internal fun buildTierReachedItems(
    rewardsRepository: RewardsRepository,
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.TierReachedItem>> = rewardsRepository.tiers()
    .take(1).firstOrError()
    .map { tiers ->
        // if we received a event which is not a TierReachedHistoryEvent do nothing
        events.mapNotNull { it as? TierReachedHistoryEvent }.map { event ->
            val tier = tiers.find { it.level == event.tierLevel }
            SmilesHistoryItem.TierReachedItem(
                event.smiles,
                event.creationTime,
                tier?.rank,
                tier?.smilesPerBrushing.toString(),
                tier?.pictureUrl
            )
        }
    }

/**
 * Creates a Redeem item from a Redeem event
 */
@VisibleForTesting
internal fun buildSmilesRedeemedItems(
    rewardsRepository: RewardsRepository,
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.SmilesRedeemedItem>> = rewardsRepository.prizes()
    .take(1).firstOrError()
    .map { prizes ->
        // if we received a event which is not a SmilesRedeemedHistoryEvent do nothing
        events.mapNotNull { it as? SmilesRedeemedHistoryEvent }.map { event ->
            val prize = prizes.find { it.id == event.rewardsId }
            SmilesHistoryItem.SmilesRedeemedItem(
                event.smiles,
                event.creationTime,
                prize?.title
            )
        }
    }

/**
 * Creates a Transfer item from a Transfer event
 */
@VisibleForTesting
internal fun buildSmilesTransferItems(
    profileManager: ProfileManager,
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.SmilesTransferItem>> =
    profileManager.getProfilesLocally().map { profiles ->
        // if we received a event which is not a SmilesTransferHistoryEvent do nothing
        events.mapNotNull { it as? SmilesTransferHistoryEvent }.map { event ->
            val profileRelated = profiles.find { it.id == event.relatedProfileId }

            SmilesHistoryItem.SmilesTransferItem(
                event.smiles,
                event.creationTime,
                profileRelated?.firstName
            )
        }
    }

/**
 * Creates a Streak item from a Streak event
 */
@VisibleForTesting
internal fun buildStreakCompletedItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.StreakCompletedItem>> = Single.just(
    // if we received a event which is not a StreakCompletedHistoryEvent do nothing
    events.mapNotNull { it as? StreakCompletedHistoryEvent }.map { event ->
        SmilesHistoryItem.StreakCompletedItem(
            event.smiles,
            event.creationTime
        )
    }
)

/**
 * Creates a SmilesExpiredItem from a SmilesExpiredHistoryEvent
 */
@VisibleForTesting
internal fun buildSmilesExpiredItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.SmilesExpiredItem>> = Single.just(
    // if we received a event which is not a SmilesExpiredHistoryEvent do nothing
    events.mapNotNull { it as? SmilesExpiredHistoryEvent }.map { event ->
        SmilesHistoryItem.SmilesExpiredItem(
            event.smiles,
            event.creationTime
        )
    }
)

/**
 * Creates a ReferralItem from a ReferralEvent
 */
@VisibleForTesting
internal fun buildReferralItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.ReferralItem>> = Single.just(
    // if we received a event which is not a ReferralHistoryEvent do nothing
    events.mapNotNull { it as? ReferralHistoryEvent }.map { event ->
        SmilesHistoryItem.ReferralItem(
            event.smiles,
            event.creationTime
        )
    }
)

/**
 * Creates an AccountCreated item from an AccountCreated event
 */
@VisibleForTesting
internal fun buildAccountCreatedItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.AccountCreatedItem>> = Single.just(
    // if we received a event which is not a AccountCreatedHistoryEvent do nothing
    events.mapNotNull { it as? AccountCreatedHistoryEvent }.map { event ->
        SmilesHistoryItem.AccountCreatedItem(
            event.smiles,
            event.creationTime
        )
    }
)

/**
 * Creates a NotificationTappedItem from a NotificationTappedHistoryEvent
 */
@VisibleForTesting
internal fun buildNotificationTappedItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.NotificationTappedItem>> = Single.just(
    // if we received a event which is not a NotificationTappedHistoryEvent do nothing
    events.mapNotNull { it as? NotificationTappedHistoryEvent }.map { event ->
        SmilesHistoryItem.NotificationTappedItem(
            event.smiles,
            event.creationTime
        )
    }
)

/**
 * Creates a QuestionOfTheDayAnsweredItem from a QuestionOfTheDayAnsweredHistoryEvent
 */
@VisibleForTesting
internal fun buildQuestionOfTheDayAnsweredItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.QuestionOfTheDayAnsweredItem>> = Single.just(
    // if we received a event which is not a QuestionOfTheDayAnsweredHistoryEvent do nothing
    events.mapNotNull { it as? QuestionOfTheDayAnsweredHistoryEvent }.map { event ->
        SmilesHistoryItem.QuestionOfTheDayAnsweredItem(
            event.smiles,
            event.creationTime
        )
    }
)

/**
 * Creates a ActivityCompletedItem from a ActivityCompletedHistoryEvent
 */
@VisibleForTesting
internal fun buildActivityCompletedItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.ActivityCompletedItem>> = Single.just(
    events.mapNotNull { it as? ActivityCompletedHistoryEvent }.map { event ->
        SmilesHistoryItem.ActivityCompletedItem(
            event.smiles,
            event.creationTime
        )
    }
)

/**
 * Creates a ShortTaskItem from a ShortTaskCompletedHistoryEvent
 */
@VisibleForTesting
internal fun buildShortTaskCompletedItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.ShortTaskItem>> = Single.just(
    events.mapNotNull { it as? ShortTaskCompletedHistoryEvent }.map { event ->
        SmilesHistoryItem.ShortTaskItem(
            event.smiles,
            event.creationTime,
            event.shortTask
        )
    }
)

/**
 * Creates a AmazonAccountLinkedItem from a AmazonAccountLinkedEvent
 */
@VisibleForTesting
internal fun buildAmazonAccountLinkedItems(
    events: List<SmilesHistoryEvent>
): Single<List<SmilesHistoryItem.AmazonAccountLinkedItem>> = Single.just(
    events.mapNotNull { it as? AmazonAccountLinkedEvent }.map { event ->
        SmilesHistoryItem.AmazonAccountLinkedItem(
            event.smiles,
            event.creationTime
        )
    }
)

/**
 * This helper function create an observable of List<SmilesHistoryEvent> each item contain items of the
 * same java class type.
 */
@VisibleForTesting
internal fun groupEventObservable(events: List<SmilesHistoryEvent>): Observable<List<SmilesHistoryEvent>> =
    Observable.fromIterable(
        events.groupByTo(HashMap()) { it::class.java }.toList()
    ).map { it.second as List<SmilesHistoryEvent> }

/**
 * Sort and Reduce a source of List<SmilesHistoryItem> to a Single List<SmilesHistoryItem> date ordered
 */
@VisibleForTesting
internal fun Observable<List<SmilesHistoryItem>>.sortByDate(): Single<List<SmilesHistoryItem>> =
    reduceWith({
        sortedSetOf(Comparator<SmilesHistoryItem> { item1, item2 ->
            item2.creationTime.compareTo(item1.creationTime)
        })
    }) { list, items ->
        list.apply { addAll(items) }
    }.map { it.toList() }
