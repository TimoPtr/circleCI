/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.models

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kolibree.android.room.DateConvertersString
import java.util.Objects
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

/**
 * Interface to be consumed by non-persistence clients
 */
@Keep
interface Prize : Parcelable {
    val id: Long
    val category: String
    val description: String
    val title: String
    val creationTime: LocalDate
    val smilesRequired: Int
    val pictureUrl: String
    val purchasable: Boolean
    val voucherDiscount: Double
}

@Keep
@Entity(tableName = "prizes")
@TypeConverters(DateConvertersString::class)
@Parcelize
internal data class PrizeEntity(
    @PrimaryKey override val id: Long,
    override val category: String,
    override val description: String,
    override val title: String,
    override val creationTime: LocalDate,
    override val smilesRequired: Int,
    override val purchasable: Boolean,
    override val voucherDiscount: Double,
    val company: String,
    override val pictureUrl: String,
    val productId: Int? = null
) : Prize {

    // override equals and hashCode because Id is a Long and it might crash on Android 5

    @Suppress("ComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrizeEntity

        if (id != other.id) return false
        if (category != other.category) return false
        if (description != other.description) return false
        if (title != other.title) return false
        if (creationTime != other.creationTime) return false
        if (smilesRequired != other.smilesRequired) return false
        if (purchasable != other.purchasable) return false
        if (voucherDiscount != other.voucherDiscount) return false
        if (company != other.company) return false
        if (pictureUrl != other.pictureUrl) return false
        if (productId != other.productId) return false

        return true
    }

    override fun hashCode(): Int =
        Objects.hash(
            id,
            category,
            description,
            title,
            creationTime,
            smilesRequired,
            purchasable,
            voucherDiscount,
            company,
            pictureUrl,
            productId
        )
}
