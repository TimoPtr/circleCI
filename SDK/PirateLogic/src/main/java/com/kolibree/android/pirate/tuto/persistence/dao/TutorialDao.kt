package com.kolibree.android.pirate.tuto.persistence.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.pirate.tuto.persistence.model.Tutorial

@Dao
internal interface TutorialDao {

    // not possible to replace the * by a dynamic col name directly
    @Query("SELECT * FROM  tutorial WHERE profileId = :profileId LIMIT 1")
    fun hasSeen(profileId: Long): Tutorial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(param: Tutorial): Long

    @Query("DELETE FROM tutorial")
    fun deleteAll()
}
