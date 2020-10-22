package com.kolibree.android.sdk.persistence.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Created by guillaumeagis on 13/05/18.
 * Store Toothbrush info for an user into Room
 */

@Dao
internal interface AccountToothbrushDao {

    @Query("SELECT * FROM account_tootbrushes WHERE account_id = :accountId")
    fun getAccountToothbrushes(accountId: Long): Maybe<List<AccountToothbrush>>

    @Query(
        "SELECT CASE WHEN EXISTS (" +
            "SELECT * FROM account_tootbrushes WHERE account_id = :accountId AND mac = :mac" +
            ") " +
            "THEN CAST(1 AS BIT) " +
            "ELSE CAST(0 AS BIT) END"
    )
    fun isAssociated(accountId: Long, mac: String): Single<Boolean>

    @Query("UPDATE account_tootbrushes SET name =:name WHERE mac =:mac")
    fun rename(name: String, mac: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(param: List<AccountToothbrush>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(param: AccountToothbrush)

    @Query("DELETE FROM account_tootbrushes WHERE mac = :mac")
    fun delete(mac: String)

    @Query("DELETE FROM account_tootbrushes")
    fun deleteAll()

    @Query("SELECT * FROM account_tootbrushes")
    fun listAll(): List<AccountToothbrush>

    @Query("SELECT * FROM account_tootbrushes")
    fun listAllStream(): Flowable<List<AccountToothbrush>>

    @Query("SELECT * FROM account_tootbrushes WHERE profile_id = :profileId")
    fun listAllWithProfileIdStream(profileId: Long): Flowable<List<AccountToothbrush>>

    @Query("SELECT * FROM account_tootbrushes WHERE mac = :mac")
    fun read(mac: String): Flowable<List<AccountToothbrush>>

    @Query("SELECT profile_id FROM account_tootbrushes WHERE mac = :mac")
    fun getToothbrushProfileId(mac: String): Single<Long>
}
