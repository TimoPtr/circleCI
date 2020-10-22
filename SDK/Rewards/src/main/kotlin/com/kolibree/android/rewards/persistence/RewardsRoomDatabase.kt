package com.kolibree.android.rewards.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kolibree.android.rewards.feedback.FeedbackEntity
import com.kolibree.android.rewards.models.CategoryEntity
import com.kolibree.android.rewards.models.ChallengeEntity
import com.kolibree.android.rewards.models.ChallengeProgressEntity
import com.kolibree.android.rewards.models.LifetimeSmilesEntity
import com.kolibree.android.rewards.models.PrizeEntity
import com.kolibree.android.rewards.models.ProfileSmilesEntity
import com.kolibree.android.rewards.models.ProfileTierEntity
import com.kolibree.android.rewards.models.SmilesHistoryEventEntity
import com.kolibree.android.rewards.models.TierEntity
import com.kolibree.android.rewards.persistence.RewardsRoomDatabase.Companion.DATABASE_VERSION
import com.kolibree.android.rewards.persistence.migration.V2AddPersonalChallengeTableMigration
import com.kolibree.android.rewards.persistence.migration.V3RecreatePersonalChallengeTableMigration
import com.kolibree.android.rewards.persistence.migration.V4UpdateSyncFieldsPersonalChallengeTableMigration
import com.kolibree.android.rewards.persistence.migration.V5AddLifetimeSmilesTableMigration
import com.kolibree.android.rewards.personalchallenge.data.persistence.PersonalChallengeDao
import com.kolibree.android.rewards.personalchallenge.data.persistence.model.PersonalChallengeEntity

@Database(
    entities = [
        ChallengeEntity::class,
        CategoryEntity::class,
        ChallengeProgressEntity::class,
        TierEntity::class,
        ProfileTierEntity::class,
        ProfileSmilesEntity::class,
        PrizeEntity::class,
        SmilesHistoryEventEntity::class,
        FeedbackEntity::class,
        PersonalChallengeEntity::class,
        LifetimeSmilesEntity::class
    ],
    version = DATABASE_VERSION
)
internal abstract class RewardsRoomDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "rewards.db"
        const val DATABASE_VERSION = 5

        val migrations = arrayOf(
            V2AddPersonalChallengeTableMigration,
            V3RecreatePersonalChallengeTableMigration,
            V4UpdateSyncFieldsPersonalChallengeTableMigration,
            V5AddLifetimeSmilesTableMigration
        )
    }

    abstract fun challengesDao(): ChallengesDao
    abstract fun categoriesDao(): CategoriesDao
    abstract fun challengeProgressDao(): ChallengeProgressDao
    abstract fun tiersDao(): TiersDao
    abstract fun profileTierDao(): ProfileTierDao
    abstract fun profileSmilesDao(): ProfileSmilesDao
    abstract fun prizeDao(): PrizeDao
    abstract fun smilesHistoryEventsDao(): SmilesHistoryEventsDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun personalChallengeDao(): PersonalChallengeDao
    abstract fun lifetimeSmilesDao(): LifetimeSmilesDao
}
