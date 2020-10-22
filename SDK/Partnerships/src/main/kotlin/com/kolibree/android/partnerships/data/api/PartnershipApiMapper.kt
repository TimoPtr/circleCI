/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.api

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.defensive.Preconditions
import com.kolibree.android.partnerships.data.api.model.PartnershipData
import com.kolibree.android.partnerships.domain.model.PartnershipStatus

/**
 * Base interface for one-way mappers between API and domain partnership status objects.
 *
 * If you want to add support for new partnership, you need to provide implementation of this
 * interface and inject it into Dagger graph with matching [Partner] map key.
 *
 * Example:
 * ```
 * @Provides
 * @IntoMap
 * @PartnerKey(Partner.MY_NEW_PARTNERSHIP)
 * fun provideApiMapper(impl: MyNewApiMapper): PartnershipApiMapper = impl
 * ```
 */
@VisibleForApp
internal interface PartnershipApiMapper {

    fun apiResponseToStatus(profileId: Long, data: PartnershipData): PartnershipStatus
}

internal inline fun <reified T> PartnershipData.hasData(key: String): Boolean =
    containsKey(key) && this[key] != null && this[key] is T

internal inline fun <reified T> PartnershipData.getDataValue(key: String): T {
    Preconditions.checkArgument(hasData<T>(key))
    return this[key] as T
}
