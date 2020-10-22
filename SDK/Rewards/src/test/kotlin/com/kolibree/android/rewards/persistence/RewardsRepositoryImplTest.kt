package com.kolibree.android.rewards.persistence

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.models.CategoryWithProgress
import com.kolibree.android.rewards.models.PrizeEntity
import com.kolibree.android.rewards.models.ProfileSmilesEntity
import com.kolibree.android.rewards.models.TierEntity
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mock

internal class RewardsRepositoryImplTest : BaseUnitTest() {
    @Mock
    lateinit var challengesDao: ChallengesDao

    @Mock
    lateinit var profileSmilesDao: ProfileSmilesDao

    @Mock
    lateinit var profileTierDao: ProfileTierDao

    @Mock
    lateinit var smilesHistoryDao: SmilesHistoryEventsDao

    @Mock
    lateinit var tierDao: TiersDao

    @Mock
    lateinit var prizeDao: PrizeDao

    private lateinit var rewardsRepository: RewardsRepositoryImpl

    override fun setup() {
        super.setup()

        rewardsRepository =
            RewardsRepositoryImpl(
                challengesDao,
                profileSmilesDao,
                profileTierDao,
                tierDao,
                prizeDao,
                smilesHistoryDao
            )
    }

    /*
    CATEGORIES WITH CHALLENGE PROGRESS
     */

    @Test
    fun `maps ChallengeWithProgressInternal to RewardsCategoryWithProgress`() {
        val profileId = 65L
        val daoSubject = PublishProcessor.create<List<ChallengeWithProgressInternal>>()
        whenever(challengesDao.challengeProgressForProfile(profileId)).thenReturn(daoSubject)

        val observer = rewardsRepository.categoriesWithChallengeProgress(profileId).test()

        observer.assertEmpty()

        val randomCategory = "random"
        val rookieCategory = "Rookie"

        val name1 = "name1"
        val description1 = "description1"
        val pictureUrl1 = "pictureUrl1"
        val percentage1 = 50
        val greetingMessage1 = "greetings"
        val smilesReward1 = 88
        val completionTime1 = TrustedClock.getNowZonedDateTime()
        val profileId1 = 1L

        val name2 = "name2"
        val description2 = "description2"
        val pictureUrl2 = "pictureUrl2"
        val percentage2 = 50
        val greetingMessage2 = "greetings"
        val smilesReward2 = 88
        val completionTime2 = TrustedClock.getNowZonedDateTime().minusDays(1)
        val profileId2 = 2L

        val nameRookie = "nameRookie"
        val descriptionRookie = "descriptionRookie"
        val pictureUrlRookie = "pictureUrlRookie"
        val percentageRookie = 50
        val greetingMessageRookie = "greetings"
        val smilesRewardRookie = 88
        val completionTimeRookie = TrustedClock.getNowZonedDateTime().minusDays(2)
        val profileIdRookie = 3L

        val randomChallenge1 = ChallengeWithProgressInternal(
            0L,
            name1,
            description1,
            pictureUrl1,
            randomCategory,
            greetingMessage1,
            smilesReward1,
            percentage1,
            completionTime1,
            profileId1,
            null,
            null
        )

        val randomChallenge2 = ChallengeWithProgressInternal(
            1L,
            name2,
            description2,
            pictureUrl2,
            randomCategory,
            greetingMessage2,
            smilesReward2,
            percentage2,
            completionTime2,
            profileId2,
            null,
            null
        )

        val rookieChallenge = ChallengeWithProgressInternal(
            3L,
            nameRookie,
            descriptionRookie,
            pictureUrlRookie,
            rookieCategory,
            greetingMessageRookie,
            smilesRewardRookie,
            percentageRookie,
            completionTimeRookie,
            profileIdRookie,
            null,
            null
        )

        val randomCategoryWithChallengeProgress = CategoryWithChallengeProgress(
            randomCategory,
            listOf(randomChallenge1, randomChallenge2)
        )
        val rookieCategoryWithChallengeProgress =
            CategoryWithChallengeProgress(rookieCategory, listOf(rookieChallenge))

        val expectedList =
            listOf<CategoryWithProgress>(
                randomCategoryWithChallengeProgress,
                rookieCategoryWithChallengeProgress
            )

        daoSubject.onNext(listOf(randomChallenge1, rookieChallenge, randomChallenge2))

        observer.assertValue(expectedList)
    }

    /*
    PROFILE PROGRESS WITH PROFILE ID
     */

    @Test
    fun `profileProgress emits empty list if ProfileSmilesDao emits empty list`() {
        val profileId = 65L
        whenever(profileSmilesDao.read(profileId)).thenReturn(BehaviorProcessor.createDefault(listOf()))

        val observer = rewardsRepository.profileProgress(profileId).test()

        observer.assertValueCount(1)

        assertTrue(observer.values().first().isEmpty())
    }

    @Test
    fun `profileProgress emits list with content from ProfileSmilesDao`() {
        val profileId = 65L
        val smiles = 634
        val profileSmilesEntity = ProfileSmilesEntity(profileId, smiles)
        whenever(profileSmilesDao.read(profileId)).thenReturn(
            BehaviorProcessor.createDefault(
                listOf(
                    profileSmilesEntity
                )
            )
        )

        val observer = rewardsRepository.profileProgress(profileId).test()

        observer.assertValueCount(1)

        assertEquals(profileSmilesEntity, observer.values().first().first())
    }

    /*
    PROFILE PROGRESS
     */
    @Test
    fun `profileProgress emits list of all profileSmiles from ProfileSmilesDao`() {
        val profileSmilesEntity1 = ProfileSmilesEntity(65, 634)
        val profileSmilesEntity2 = ProfileSmilesEntity(66, 634)
        whenever(profileSmilesDao.read()).thenReturn(
            BehaviorProcessor.createDefault(
                listOf(
                    profileSmilesEntity1,
                    profileSmilesEntity2
                )
            )
        )

        val observer = rewardsRepository.profileProgress().test()

        observer.assertValueCount(1)

        assertEquals(
            listOf(
                profileSmilesEntity1,
                profileSmilesEntity2
            ), observer.values().first()
        )
    }

    /*
    TIER
     */

    @Test
    fun `empty tier list`() {
        whenever(tierDao.getTiers()).thenReturn(Flowable.empty())

        val observer = rewardsRepository.tiers().test()

        observer.assertNoValues()
        observer.assertNoErrors()
        observer.assertComplete()
    }

    @Test
    fun `tier list`() {
        val expectedTierEntities = listOf(
            TierEntity(
                0,
                1,
                1,
                "http://google.fr",
                "rank",
                TrustedClock.getNowLocalDate(),
                "message"
            ),
            TierEntity(
                10,
                1,
                1,
                "http://google.fr",
                "rank",
                TrustedClock.getNowLocalDate(),
                "message"
            )
        )

        whenever(tierDao.getTiers()).thenReturn(Flowable.just(expectedTierEntities))

        val observer = rewardsRepository.tiers().test()

        observer.assertValue(expectedTierEntities)
        observer.assertNoErrors()
        observer.assertComplete()
    }

    /*
            PRIZE
            */
    @Test
    fun `empty prize list`() {
        whenever(prizeDao.getPrizes()).thenReturn(Flowable.empty())

        val observer = rewardsRepository.prizes().test()

        observer.assertNoValues()
        observer.assertNoErrors()
        observer.assertComplete()
    }

    @Test
    fun `prize list`() {
        val expectedPrizeEntities = listOf(
            PrizeEntity(
                0,
                "category",
                "hello",
                "world",
                TrustedClock.getNowLocalDate(),
                0,
                false,
                10.0,
                "kolibree",
                "http://google.com"
            ),
            PrizeEntity(
                1,
                "category",
                "hello",
                "world",
                TrustedClock.getNowLocalDate(),
                0,
                false,
                10.0,
                "kolibree",
                "http://google.com"
            )
        )
        whenever(prizeDao.getPrizes()).thenReturn(Flowable.just(expectedPrizeEntities))

        val observer = rewardsRepository.prizes().test()

        observer.assertValue(expectedPrizeEntities)
        observer.assertComplete()
    }
}
