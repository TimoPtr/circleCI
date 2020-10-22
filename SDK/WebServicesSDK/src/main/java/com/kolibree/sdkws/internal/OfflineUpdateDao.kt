package com.kolibree.sdkws.internal

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
internal abstract class OfflineUpdateDao : OfflineUpdateDatastore {
    @Query("DELETE FROM offlineupdate")
    abstract override fun truncate()

    @Query("SELECT * FROM offlineupdate WHERE profileid=:profileId AND type=:type")
    abstract fun readByProfileAndType(profileId: Long, type: Int): OfflineUpdateInternal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(offlineUpdateInternal: OfflineUpdateInternal)

    @Query("UPDATE offlineupdate SET data=:data WHERE profileid=:profileId AND type=:type")
    abstract fun updateData(profileId: Long, type: Int, data: String?)

    @Update
    abstract fun update(offlineUpdateInternal: OfflineUpdateInternal)

    @Query("DELETE FROM offlineupdate WHERE profileid=:profileId AND type=:type")
    abstract override fun delete(profileId: Long, type: Int)

    /**
     *
     * @return true if account data version has to be incremented (only in case of insert)
     */
    /*
    I don't understand the above comment, just copying from OfflineUpdateAdapter
     */
    @Transaction
    override fun insertOrUpdate(newUpdate: OfflineUpdateInternal): Boolean {
        val previousUpdate = readByProfileAndType(newUpdate.profileId, newUpdate.type)

        if (previousUpdate != null) {
            previousUpdate.merge(newUpdate)

            update(previousUpdate)

            return false
        }

        insert(newUpdate)

        return true
    }

    @Transaction
    override fun getOfflineUpdateForProfileId(profileId: Long, type: Int): OfflineUpdateInternal? {
        var previousUpdate = readByProfileAndType(profileId, type)

        if (previousUpdate != null) {
            delete(profileId, type)
        }

        return previousUpdate
    }
}
