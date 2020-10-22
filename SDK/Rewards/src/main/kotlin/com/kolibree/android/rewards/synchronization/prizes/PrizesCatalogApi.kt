/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.rewards.synchronization.prizes

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.kolibree.android.synchronizator.models.SynchronizableCatalog
import java.util.Objects
import org.threeten.bp.LocalDate

/**
 * Representation of prize_list.json
 *
 * See https://confluence.kolibree.com/x/aAjp
 */
@Keep
internal data class PrizesCatalogApi(
    @SerializedName("rewards") val prizes: List<PrizeApi>
) : SynchronizableCatalog

@Keep
internal data class PrizeApi(
    val categoryId: Long,
    val category: String,
    val details: List<PrizeDetailsApi>
) {
    // override equals and hashCode because Id is a Long and it might crash on Android 5

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrizeApi

        if (categoryId != other.categoryId) return false
        if (category != other.category) return false
        if (details != other.details) return false

        return true
    }

    override fun hashCode(): Int =
        Objects.hash(categoryId, category, details)
}

@Keep
internal data class PrizeDetailsApi(
    val smilesRequired: Int,
    val purchasable: Boolean,
    val voucherDiscount: Double,
    val description: String,
    val title: String,
    val company: String,
    val pictureUrl: String,
    val creationDate: LocalDate,
    val rewardsId: Long,
    val productId: Int?
) {
    // override equals and hashCode because Id is a Long and it might crash on Android 5

    @Suppress("ComplexMethod")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrizeDetailsApi

        if (smilesRequired != other.smilesRequired) return false
        if (purchasable != other.purchasable) return false
        if (voucherDiscount != other.voucherDiscount) return false
        if (description != other.description) return false
        if (title != other.title) return false
        if (company != other.company) return false
        if (pictureUrl != other.pictureUrl) return false
        if (creationDate != other.creationDate) return false
        if (rewardsId != other.rewardsId) return false
        if (productId != other.productId) return false

        return true
    }

    override fun hashCode(): Int =
        Objects.hash(
            smilesRequired,
            purchasable,
            voucherDiscount,
            description,
            title,
            company,
            pictureUrl,
            creationDate,
            rewardsId,
            productId
        )
}
