/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import androidx.room.Update
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.rewards.feedback.FeedbackEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import org.threeten.bp.ZonedDateTime

@Dao
internal interface FeedbackDao : Truncable {
    @Insert
    fun insert(feedbackEntities: List<FeedbackEntity>)

    @Query("SELECT MAX(historyEventDateTime) as dateTime FROM feedback WHERE profileId=:profileId")
    fun mostRecentFeedbackDateTime(profileId: Long): MostRecentFeedbackDatetime?

    @Query("SELECT * FROM feedback WHERE profileId=:profileId AND isConsumed=0 ORDER BY historyEventDateTime DESC")
    fun oldestFeedbackStream(profileId: Long): Flowable<List<FeedbackEntity>>

    @Query("UPDATE feedback SET isConsumed=1 WHERE id IN (:feedbackIds)")
    fun markAsConsumed(feedbackIds: List<Long>): Completable

    @Update
    fun update(feedbackEntity: FeedbackEntity)

    @Query("SELECT * FROM feedback")
    fun readAll(): Flowable<List<FeedbackEntity>>

    @Query("SELECT * FROM feedback WHERE id=:id ")
    fun getFeedback(id: Long): FeedbackEntity?

    @Query("DELETE FROM feedback")
    override fun truncate(): Completable
}

@VisibleForApp
@TypeConverters(SmilesHistoryNullableZoneDateTimeToStringConverter::class)
data class MostRecentFeedbackDatetime(val dateTime: ZonedDateTime?)
