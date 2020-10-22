package com.kolibree.android.rewards.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.kolibree.android.rewards.models.ChallengeEntity
import io.reactivex.Flowable
import io.reactivex.Maybe

@Suppress("MaxLineLength")
@Dao
internal interface ChallengesDao {

    @Transaction
    fun replace(challenges: List<ChallengeEntity>) {
        truncate()
        insertAll(challenges)
    }

    @Query("DELETE FROM challenges")
    fun truncate()

    @Insert
    fun insertAll(challenges: List<ChallengeEntity>)

    @Query("SELECT id, name, description, pictureUrl, category, greetingMessage, smilesReward, percentage, completionTime, profileId, `action`, completionDetails FROM challenges c INNER JOIN challenge_progress p ON c.id=p.challengeid WHERE profileId=:profileId ORDER BY id ASC")
    fun challengeProgressForProfile(profileId: Long): Flowable<List<ChallengeWithProgressInternal>>

    @Query("SELECT id, name, category, greetingMessage, description, pictureUrl, smilesReward FROM challenges c INNER JOIN challenge_progress p ON c.id=p.challengeid WHERE profileId=:profileId AND completionTime != '' ORDER BY id ASC")
    fun completedChallenges(profileId: Long): Maybe<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges WHERE id IN (:challengeIds) ORDER BY id ASC")
    fun read(challengeIds: List<Long>): List<ChallengeEntity>
}
