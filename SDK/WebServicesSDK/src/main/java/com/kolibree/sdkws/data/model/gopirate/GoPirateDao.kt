package com.kolibree.sdkws.data.model.gopirate

import android.annotation.SuppressLint
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import io.reactivex.Completable
import io.reactivex.Single

@Dao
internal abstract class GoPirateDao : GoPirateDatastore {
    @Query("DELETE FROM gopirate")
    abstract override fun truncate()

    @Query("SELECT * from gopirate WHERE profile_id=:profileId LIMIT 1")
    abstract override fun getData(profileId: Long): Single<GoPirateData>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun update(goPirateData2: GoPirateData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(goPirateData2: GoPirateData)

    override fun update(data: UpdateGoPirateData, profileId: Long): Completable {
        @Suppress("TooGenericExceptionCaught")
        return Completable.fromAction {
            try {
                updateTransaction(data, profileId)
            } catch (e: Exception) {
                insert(GoPirateData.fromUpdateGoPirateData(profileId, data))
            }
        }
    }

    /*
    After moving to Room 2.2.0, methods annotated with @Transaction must not return deferred/async
    return type io.reactivex.Completable. Since transactions are thread confined and Room cannot
    guarantee that all queries in the method implementation are performed on the same thread.
     */
    @Transaction
    open fun updateTransaction(data: UpdateGoPirateData, profileId: Long) {
        val localGoPirate = getData(profileId).blockingGet()
        update(localGoPirate.update(data))
    }

    override fun update(goPirateData: GoPirateData, profileId: Long): Completable {
        @Suppress("TooGenericExceptionCaught")
        return Completable.fromAction {
            try {
                updateTransaction(goPirateData, profileId)
            } catch (e: Exception) {
                insert(goPirateData)
            }
        }
    }

    @SuppressLint("CheckResult")
    @Transaction
    open fun updateTransaction(goPirateData: GoPirateData, profileId: Long) {
        getData(profileId).blockingGet()
        update(goPirateData)
    }
}
