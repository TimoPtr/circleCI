/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.persitence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.shop.data.persitence.model.CartEntryEntity
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
internal interface CartDao : Truncable {

    @Query("SELECT * FROM cart_entries WHERE profileId = :profileId")
    fun getCartEntriesForProfile(profileId: Long): List<CartEntryEntity>

    @Query("SELECT * FROM cart_entries WHERE profileId = :profileId")
    fun getCartEntriesForProfileStream(profileId: Long): Flowable<List<CartEntryEntity>>

    @Query("SELECT * FROM cart_entries WHERE profileId = :profileId AND productId = :productId AND variantId = :variantId")
    fun getEntryByProfileAndProductVariant(profileId: Long, productId: String, variantId: String): CartEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEntry(entry: CartEntryEntity)

    @Query("DELETE FROM cart_entries WHERE profileId = :profileId")
    fun truncateEntriesForProfile(profileId: Long)

    @Query("DELETE FROM cart_entries")
    override fun truncate(): Completable
}
