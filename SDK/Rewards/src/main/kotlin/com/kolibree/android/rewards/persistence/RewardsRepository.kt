package com.kolibree.android.rewards.persistence

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.models.CategoryWithProgress
import com.kolibree.android.rewards.models.Challenge
import com.kolibree.android.rewards.models.ChallengeWithProgress
import com.kolibree.android.rewards.models.Prize
import com.kolibree.android.rewards.models.ProfileSmiles
import com.kolibree.android.rewards.models.SmilesHistoryEvent
import com.kolibree.android.rewards.models.Tier
import io.reactivex.Flowable
import io.reactivex.Maybe
import javax.inject.Inject

interface ProfileSmilesRepository {

    /**
     * Stream that will emit ProfileSmiles lists with max size 1
     *
     * If there's no ProfileSmiles for the profileId, it'll emit an empty list. Otherwise, it'll emit a list containing
     * a single ProfileSmile
     *
     * It will emit new List if there are changes the persistence layer
     */
    fun profileProgress(profileId: Long): Flowable<List<ProfileSmiles>>

    /**
     * Stream that will emit all the ProfileSmiles
     *
     * It will emit new List if there are changes the persistence layer
     */
    fun profileProgress(): Flowable<List<ProfileSmiles>>

    /**
     * Stream that will emit Tier lists with max size 1
     *
     * If there's no Tier for the profileId, it'll emit an empty list. Otherwise, it'll emit a list containing
     * a single ProfileSmile
     *
     * It will emit new List if there are changes the persistence layer
     */
    fun profileTier(profileId: Long): Flowable<List<ProfileTier>>
}

@VisibleForApp
interface RewardsRepository : ProfileSmilesRepository {
    fun categoriesWithChallengeProgress(profileId: Long): Flowable<List<CategoryWithProgress>>

    fun tiersHigherThanLevel(tierLevel: Int): Maybe<List<Tier>>

    fun completedChallenges(profileId: Long): Maybe<List<Challenge>>

    /**
     * Stream that will emit the list of available prizes is there is any
     */
    fun prizes(): Flowable<List<Prize>>

    /**
     * Stream that will emit the list of available tiers
     */
    fun tiers(): Flowable<List<Tier>>

    /**
     * Stream that will emit lists of SmilesHistoryEvent
     */
    fun smilesHistoryEvents(profileId: Long): Flowable<List<SmilesHistoryEvent>>
}

internal class RewardsRepositoryImpl
@Inject constructor(
    private val challengesDao: ChallengesDao,
    private val profileSmilesDao: ProfileSmilesDao,
    private val profileTierDao: ProfileTierDao,
    private val tierDao: TiersDao,
    private val prizeDao: PrizeDao,
    private val smilesHistoryDao: SmilesHistoryEventsDao
) : RewardsRepository {
    /**
     * Combines all SmilesHistoryEvent exposed by HistoryEventsDao into a single list
     *
     * I'm assuming that whenever the database changes, all functions will emit a new list. If that's not the case, this
     * won't work properly
     */
    override fun smilesHistoryEvents(profileId: Long): Flowable<List<SmilesHistoryEvent>> =
        smilesHistoryDao.historyEntityStream(profileId)

    override fun categoriesWithChallengeProgress(profileId: Long): Flowable<List<CategoryWithProgress>> =
        challengesDao.challengeProgressForProfile(profileId)
            .map { mapToCategoryWithProgress(it) }

    private fun mapToCategoryWithProgress(challengesWithProgress: List<ChallengeWithProgressInternal>) =
        challengesWithProgress.groupBy { it.category }
            .map { entry -> CategoryWithChallengeProgress(entry.key, entry.value) }

    override fun profileProgress(profileId: Long): Flowable<List<ProfileSmiles>> =
        profileSmilesDao.read(profileId)
            .map { it }

    override fun profileProgress(): Flowable<List<ProfileSmiles>> =
        profileSmilesDao.read()
            .map { it }

    override fun profileTier(profileId: Long): Flowable<List<ProfileTier>> =
        profileTierDao.tierForProfile(profileId)

    override fun tiersHigherThanLevel(tierLevel: Int): Maybe<List<Tier>> =
        tierDao.tiersHigherThan(tierLevel).map { it }

    override fun completedChallenges(profileId: Long): Maybe<List<Challenge>> =
        challengesDao.completedChallenges(profileId)
            .map { it }

    override fun prizes(): Flowable<List<Prize>> = prizeDao.getPrizes().map { it }

    override fun tiers(): Flowable<List<Tier>> = tierDao.getTiers().map { it }
}

/**
 * Groups categories and its list of ChallengeWithProgress
 *
 * To be used by consumers of RewardsRepository
 */
internal data class CategoryWithChallengeProgress(
    override val categoryName: String,
    override val challenges: List<ChallengeWithProgress>
) : CategoryWithProgress
