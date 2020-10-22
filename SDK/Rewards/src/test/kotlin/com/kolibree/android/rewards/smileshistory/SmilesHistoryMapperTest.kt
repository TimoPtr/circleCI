/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.smileshistory

import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.GameApiConstants
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.Game
import com.kolibree.android.rewards.models.AccountCreatedHistoryEvent
import com.kolibree.android.rewards.models.ActivityCompletedHistoryEvent
import com.kolibree.android.rewards.models.BrushingSessionHistoryEvent
import com.kolibree.android.rewards.models.BrushingSessionHistoryEventStatus
import com.kolibree.android.rewards.models.Challenge
import com.kolibree.android.rewards.models.ChallengeCompletedHistoryEvent
import com.kolibree.android.rewards.models.FakeEvent
import com.kolibree.android.rewards.models.NotificationTappedHistoryEvent
import com.kolibree.android.rewards.models.OfflineBrushingSessionHistoryEvent
import com.kolibree.android.rewards.models.PersonalChallengeCompletedHistoryEvent
import com.kolibree.android.rewards.models.Prize
import com.kolibree.android.rewards.models.QuestionOfTheDayAnsweredHistoryEvent
import com.kolibree.android.rewards.models.ReferralHistoryEvent
import com.kolibree.android.rewards.models.ShortTaskCompletedHistoryEvent
import com.kolibree.android.rewards.models.SmilesExpiredHistoryEvent
import com.kolibree.android.rewards.models.SmilesHistoryEvent
import com.kolibree.android.rewards.models.SmilesRedeemedHistoryEvent
import com.kolibree.android.rewards.models.SmilesTransferHistoryEvent
import com.kolibree.android.rewards.models.StreakCompletedHistoryEvent
import com.kolibree.android.rewards.models.Tier
import com.kolibree.android.rewards.models.TierReachedHistoryEvent
import com.kolibree.android.rewards.persistence.RewardsRepository
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.profile.ProfileManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class SmilesHistoryMapperTest : BaseUnitTest() {

    @Mock
    private lateinit var rewardsRepository: RewardsRepository

    @Mock
    private lateinit var brushingsRepository: BrushingsRepository

    @Mock
    private lateinit var profileManager: ProfileManager

    private val fakeEvent = FakeEvent(
        ProfileBuilder.DEFAULT_ID,
        "fake !! should not displayed",
        101,
        ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 4, ZoneOffset.UTC)
    )

    @Test
    fun `item sorting by creation time from shuffle ordered list`() {

        val itemMock1: SmilesHistoryItem = mock()
        val itemMock2: SmilesHistoryItem = mock()
        val itemMock3: SmilesHistoryItem = mock()

        whenever(itemMock1.creationTime).thenReturn(
            ZonedDateTime.of(
                2018,
                12,
                12,
                6,
                30,
                0,
                4,
                ZoneOffset.UTC
            )
        )
        whenever(itemMock2.creationTime).thenReturn(
            ZonedDateTime.of(
                2019,
                12,
                12,
                6,
                30,
                0,
                4,
                ZoneOffset.UTC
            )
        )
        whenever(itemMock3.creationTime).thenReturn(
            ZonedDateTime.of(
                2020,
                12,
                12,
                6,
                30,
                0,
                4,
                ZoneOffset.UTC
            )
        )

        val testObserver = Observable.just(
            listOf(
                itemMock2,
                itemMock3,
                itemMock1
            )
        ).sortByDate().test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(listOf(itemMock3, itemMock2, itemMock1))
    }

    @Test
    fun `item sorting by creation time from already ordered list`() {

        val itemMock1: SmilesHistoryItem = mock()
        val itemMock2: SmilesHistoryItem = mock()
        val itemMock3: SmilesHistoryItem = mock()

        whenever(itemMock1.creationTime).thenReturn(
            ZonedDateTime.of(
                2018,
                12,
                12,
                6,
                30,
                0,
                4,
                ZoneOffset.UTC
            )
        )
        whenever(itemMock2.creationTime).thenReturn(
            ZonedDateTime.of(
                2019,
                12,
                12,
                6,
                30,
                0,
                4,
                ZoneOffset.UTC
            )
        )
        whenever(itemMock3.creationTime).thenReturn(
            ZonedDateTime.of(
                2020,
                12,
                12,
                6,
                30,
                0,
                4,
                ZoneOffset.UTC
            )
        )

        val expectedItems = listOf(
            itemMock3,
            itemMock2,
            itemMock1
        )

        val testObserver = Observable.just(expectedItems).sortByDate().test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(listOf(itemMock3, itemMock2, itemMock1))
    }

    @Test
    fun `grouping event by class type all same type`() {
        val eventMock1: ChallengeCompletedHistoryEvent = mock()
        val eventMock2: ChallengeCompletedHistoryEvent = mock()
        val eventMock3: ChallengeCompletedHistoryEvent = mock()

        val expectedEvents: List<SmilesHistoryEvent> = listOf(eventMock1, eventMock2, eventMock3)

        val testObserver = groupEventObservable(
            expectedEvents
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue(expectedEvents)
    }

    @Test
    fun `grouping event by class type all same different type`() {
        val eventMock1: ChallengeCompletedHistoryEvent = mock()
        val eventMock2: BrushingSessionHistoryEvent = mock()
        val eventMock3: TierReachedHistoryEvent = mock()

        val testObserver = groupEventObservable(
            listOf(eventMock1, eventMock2, eventMock3)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(3)
    }

    @Test
    fun `buildHistoryItem from unknown events`() {
        val event: FakeEvent = mock()
        val testObserver =
            Observable.just(listOf<SmilesHistoryEvent>(event))
                .buildHistoryItem(mock(), mock(), mock(), 0).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertNoValues()
    }

    @Test
    fun `buildHistoryItem from multiple known events type and one unknown don't throw error and return only first type`() {
        val transferEvent = SmilesTransferHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "smiles transfer message",
            70,
            ZonedDateTime.of(2030, 12, 12, 6, 30, 0, 1, ZoneOffset.UTC),
            90
        )

        val streakEvent = StreakCompletedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "streak completed message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC),
            90
        )

        val testObserver =
            Observable.just(listOf(streakEvent, transferEvent, fakeEvent))
                .buildHistoryItem(mock(), mock(), mock(), 0)
                .test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue {
            it.first() is SmilesHistoryItem.StreakCompletedItem
        }
    }

    @Test
    fun `buildBrushingSessionItems from event and with one unknown`() {
        val brushingId = 0L
        val brushingGame = GameApiConstants.GAME_GO_PIRATE

        val brushingEvent = BrushingSessionHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "hello",
            1,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 2, ZoneOffset.UTC),
            brushingId,
            brushingGame,
            BrushingSessionHistoryEventStatus.COMPLETED
        )

        val brushing: Brushing = mock()
        whenever(brushing.kolibreeId).thenReturn(brushingId)
        whenever(brushing.game).thenReturn(brushingGame)
        whenever(brushingsRepository.getBrushings(any())).thenReturn(Single.just(listOf(brushing)))

        val testObserver = buildBrushingSessionItems(
            brushingsRepository,
            listOf(brushingEvent, fakeEvent),
            0L
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().game == Game.GO_PIRATE
        }
        testObserver.assertValue {
            it.first().creationTime == brushingEvent.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == brushingEvent.smiles
        }
        testObserver.assertValue {
            it.first().status == SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.COMPLETED
        }
    }

    @Test
    fun `buildOfflineBrushingSessionItems from event and with one unknown`() {
        val brushingId = 0L
        val brushingGame = GameApiConstants.GAME_OFFLINE

        val brushingEvent = OfflineBrushingSessionHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "hello",
            1,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 2, ZoneOffset.UTC),
            brushingId,
            brushingGame,
            BrushingSessionHistoryEventStatus.COMPLETED
        )

        val testObserver = buildOfflineBrushingSessionItems(
            listOf(brushingEvent, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().game == Game.OFFLINE
        }
        testObserver.assertValue {
            it.first().creationTime == brushingEvent.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == brushingEvent.smiles
        }
        testObserver.assertValue {
            it.first().status == SmilesHistoryItem.BrushingSessionItem.BrushingSessionItemStatus.COMPLETED
        }
    }

    @Test
    fun `buildChallengeCompletedItems from event and with one unknown`() {
        val challengeId1 = 10L
        val challengeName1 = "Challenge 1"
        val challengePictureUrl1 = "http://ch1"
        val challenge1: Challenge = mock()

        val challengeEvent1 = ChallengeCompletedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message challenge",
            102,
            ZonedDateTime.of(1990, 12, 12, 6, 30, 0, 1, ZoneOffset.UTC),
            challengeId1
        )

        whenever(challenge1.id).thenReturn(challengeId1)
        whenever(challenge1.name).thenReturn(challengeName1)
        whenever(challenge1.pictureUrl).thenReturn(challengePictureUrl1)

        whenever(rewardsRepository.completedChallenges(any())).thenReturn(
            Maybe.just(
                listOf(
                    challenge1
                )
            )
        )

        val testObserver = buildChallengeCompletedItems(
            rewardsRepository,
            listOf(challengeEvent1, fakeEvent),
            0
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().challengeName == challengeName1
        }
        testObserver.assertValue {
            it.first().pictureUrl == challengePictureUrl1
        }
        testObserver.assertValue {
            it.first().creationTime == challengeEvent1.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == challengeEvent1.smiles
        }
    }

    @Test
    fun `buildPersonalChallengeCompletedItems from event and with one unknown`() {
        val challengeId1 = 10L
        val challengeName1 = "Challenge 1"
        val challengePictureUrl1 = "http://ch1"
        val challenge1: Challenge = mock()

        val challengeEvent1 = PersonalChallengeCompletedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message challenge",
            102,
            ZonedDateTime.of(1990, 12, 12, 6, 30, 0, 1, ZoneOffset.UTC),
            challengeId1
        )

        whenever(challenge1.id).thenReturn(challengeId1)
        whenever(challenge1.name).thenReturn(challengeName1)
        whenever(challenge1.pictureUrl).thenReturn(challengePictureUrl1)

        whenever(rewardsRepository.completedChallenges(any())).thenReturn(
            Maybe.just(
                listOf(
                    challenge1
                )
            )
        )

        val testObserver = buildPersonalChallengeCompletedItems(
            rewardsRepository,
            listOf(challengeEvent1, fakeEvent),
            0
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().challengeName == challengeName1
        }
        testObserver.assertValue {
            it.first().pictureUrl == challengePictureUrl1
        }
        testObserver.assertValue {
            it.first().creationTime == challengeEvent1.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == challengeEvent1.smiles
        }
    }

    @Test
    fun `buildTierReachedItems from event and with one unknown`() {
        val tierLevel = 0
        val tierRank = "bronze"
        val smilesPerBrushing = 4
        val tierPicture = "http://t1"
        val tier: Tier = mock()

        val tierEvent = TierReachedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "tier message",
            2,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 0, ZoneOffset.UTC),
            tierLevel
        )

        whenever(tier.level).thenReturn(tierLevel)
        whenever(tier.rank).thenReturn(tierRank)
        whenever(tier.smilesPerBrushing).thenReturn(smilesPerBrushing)
        whenever(tier.pictureUrl).thenReturn(tierPicture)

        whenever(rewardsRepository.tiers()).thenReturn(Flowable.just(listOf(tier)))

        val testObserver = buildTierReachedItems(
            rewardsRepository,
            listOf(tierEvent, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().rank == tierRank
        }
        testObserver.assertValue {
            it.first().smilesPerBrushing == smilesPerBrushing.toString()
        }
        testObserver.assertValue {
            it.first().pictureUrl == tierPicture
        }
        testObserver.assertValue {
            it.first().creationTime == tierEvent.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == tierEvent.smiles
        }
    }

    @Test
    fun `buildSmilesRedeemedItems from event and with one unknown`() {
        val rewardsId = 1L
        val rewardsName = "rewards !"
        val prize: Prize = mock()

        val redeemEvent = SmilesRedeemedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "redeem message",
            10000,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 3, ZoneOffset.UTC),
            rewardsId
        )

        whenever(prize.id).thenReturn(rewardsId)
        whenever(prize.title).thenReturn(rewardsName)

        whenever(rewardsRepository.prizes()).thenReturn(Flowable.just(listOf(prize)))

        val testObserver = buildSmilesRedeemedItems(
            rewardsRepository,
            listOf(redeemEvent, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().rewardsName == rewardsName
        }
        testObserver.assertValue {
            it.first().creationTime == redeemEvent.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == redeemEvent.smiles
        }
    }

    @Test
    fun `buildSmilesTransferItems from event and with one unknown`() {
        val profileId = 90L
        val profileFirstName = "Aria"

        val profile: Profile = mock()

        val transferEvent = SmilesTransferHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "smiles transfer message",
            70,
            ZonedDateTime.of(2030, 12, 12, 6, 30, 0, 1, ZoneOffset.UTC),
            90
        )

        whenever(profile.id).thenReturn(profileId)
        whenever(profile.firstName).thenReturn(profileFirstName)

        whenever(profileManager.getProfilesLocally()).thenReturn(Single.just(listOf(profile)))

        val testObserver = buildSmilesTransferItems(
            profileManager,
            listOf(transferEvent, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == transferEvent.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == transferEvent.smiles
        }
        testObserver.assertValue {
            it.first().relatedProfile == profileFirstName
        }
    }

    @Test
    fun `buildStreakCompletedItems from event and with one unknown`() {
        val streakEvent = StreakCompletedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "streak completed message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC),
            90
        )

        val testObserver = buildStreakCompletedItems(
            listOf(streakEvent, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == streakEvent.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == streakEvent.smiles
        }
    }

    @Test
    fun `buildSmilesExpiredItems from event and with one unknown`() {
        val event = SmilesExpiredHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC)
        )

        val testObserver = buildSmilesExpiredItems(
            listOf(event, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == event.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == event.smiles
        }
    }

    @Test
    fun `buildReferralItems from event and with one unknown`() {
        val event = ReferralHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC)
        )

        val testObserver = buildReferralItems(
            listOf(event, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == event.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == event.smiles
        }
    }

    @Test
    fun `buildAccountCreatedItems from event and with one unknown`() {
        val event = AccountCreatedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC)
        )

        val testObserver = buildAccountCreatedItems(
            listOf(event, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == event.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == event.smiles
        }
    }

    @Test
    fun `buildNotificationTappedItems from event and with one unknown`() {
        val event = NotificationTappedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC)
        )

        val testObserver = buildNotificationTappedItems(
            listOf(event, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == event.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == event.smiles
        }
    }

    @Test
    fun `buildQuestionOfTheDayAnsweredItems from event and with one unknown`() {
        val event = QuestionOfTheDayAnsweredHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC)
        )

        val testObserver = buildQuestionOfTheDayAnsweredItems(
            listOf(event, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == event.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == event.smiles
        }
    }

    @Test
    fun `buildActivityCompletedItems from event and with one unknown`() {
        val event = ActivityCompletedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC)
        )

        val testObserver = buildActivityCompletedItems(
            listOf(event, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == event.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == event.smiles
        }
    }

    @Test
    fun `buildShortTaskCompletedItems from event and with one unknown`() {
        val event = ShortTaskCompletedHistoryEvent(
            ProfileBuilder.DEFAULT_ID,
            "message",
            50,
            ZonedDateTime.of(2019, 12, 12, 6, 30, 0, 10, ZoneOffset.UTC),
            ShortTask.TEST_YOUR_ANGLE
        )

        val testObserver = buildShortTaskCompletedItems(
            listOf(event, fakeEvent)
        ).test()

        testObserver.assertComplete()
        testObserver.assertNoErrors()
        testObserver.assertValue {
            it.size == 1
        }
        testObserver.assertValue {
            it.first().creationTime == event.creationTime
        }
        testObserver.assertValue {
            it.first().smiles == event.smiles
        }
        testObserver.assertValue {
            it.first().shortTask == event.shortTask
        }
    }
}
