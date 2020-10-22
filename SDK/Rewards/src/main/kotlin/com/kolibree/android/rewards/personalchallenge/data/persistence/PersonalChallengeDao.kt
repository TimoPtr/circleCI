/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity
import com.kolibree.android.synchronizator.data.database.UuidConverters
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.UUID

@Dao
@Suppress("TooManyFunctions")
internal abstract class PersonalChallengeDao {

    @Query("SELECT * FROM personal_challenges WHERE id = :id")
    abstract fun getChallengeByPrimaryId(id: Long): PersonalChallengeEntity?

    @Query("SELECT * FROM personal_challenges WHERE profileId = :profileId")
    abstract fun getChallengeForProfile(profileId: Long): PersonalChallengeEntity?

    @Query("SELECT * FROM personal_challenges WHERE profileId = :profileId")
    abstract fun getChallengeForProfileStream(profileId: Long): Flowable<PersonalChallengeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(entity: PersonalChallengeEntity): Long

    @Transaction
    @Throws(IllegalStateException::class)
    open fun replace(entity: PersonalChallengeEntity): PersonalChallengeEntity {
        truncateForProfile(entity.profileId)
        val id = insert(entity)
        return getChallengeByPrimaryId(id) ?: throw IllegalStateException(
            "No PersonalChallengeEntity for id $id after we just created it!"
        )
    }

    @Query("DELETE FROM personal_challenges WHERE profileId = :profileId")
    abstract fun delete(profileId: Long)

    @Query("DELETE FROM personal_challenges WHERE uuid = :uuid")
    @TypeConverters(UuidConverters::class)
    abstract fun delete(uuid: UUID)

    @Query("DELETE FROM personal_challenges WHERE profileId = :profileId")
    abstract fun truncateForProfile(profileId: Long)

    @Query("DELETE FROM personal_challenges")
    abstract fun truncate(): Completable

    @Query("SELECT * FROM personal_challenges WHERE profileId = :profileId")
    abstract fun getChallengesForProfileStream(profileId: Long): Flowable<List<PersonalChallengeEntity>>

    @Query("SELECT * FROM personal_challenges WHERE uuid = :uuid")
    @TypeConverters(UuidConverters::class)
    abstract fun getByUuid(uuid: UUID): PersonalChallengeEntity
}
