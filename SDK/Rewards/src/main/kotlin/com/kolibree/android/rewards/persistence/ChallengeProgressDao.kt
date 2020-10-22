package com.kolibree.android.rewards.persistence

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.rewards.models.ChallengeProgressEntity
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import io.reactivex.Completable

@Dao
internal abstract class ChallengeProgressDao : Truncable {

    @Transaction
    open fun replace(challengeProgressCatalog: ChallengeProgressProfileCatalogInternal) {
        if (challengeProgressCatalog.isEmpty()) return

        challengeProgressCatalog.distinctBy { it.profileId }.forEach { truncate(it.profileId) }

        insertAll(challengeProgressCatalog)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(challengeProgressProfileCatalog: ChallengeProgressProfileCatalogInternal)

    @Query("DELETE FROM challenge_progress where profileId=:profileId")
    abstract fun truncate(profileId: Long)

    @Query("DELETE FROM challenge_progress")
    abstract override fun truncate(): Completable
}

/**
 * Abstracts a List<ChallengeProgressEntity> as SynchronizableReadOnly
 *
 * To be produced by ChallengeProgressReadOnlyApi and consumed by ChallengeProgressDatastore
 */
@Keep
internal class ChallengeProgressProfileCatalogInternal : ArrayList<ChallengeProgressEntity>(), SynchronizableReadOnly
