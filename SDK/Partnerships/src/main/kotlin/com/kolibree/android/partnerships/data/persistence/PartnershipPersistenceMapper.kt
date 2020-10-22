/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.persistence

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.partnerships.data.persistence.model.PartnershipEntity
import com.kolibree.android.partnerships.domain.model.PartnershipStatus

/**
 * Base interface for two-way mappers between data and domain partnership status objects.
 *
 * If you want to add support for new partnership, you need to provide implementation of this
 * interface and inject it into Dagger graph with matching [Partner] map key.
 *
 * Example:
 * ```
 * @Provides
 * @IntoMap
 * @PartnerKey(Partner.MY_NEW_PARTNERSHIP)
 * fun providePersistenceMapper(impl: MyNewPersistenceMapper): PartnershipPersistenceMapper = impl
 * ```
 */
@VisibleForApp
internal interface PartnershipPersistenceMapper {

    /**
     * Maps domain object to its data counterpart
     */
    fun statusToEntity(status: PartnershipStatus): PartnershipEntity

    /**
     * Maps data object to its domain counterpart
     */
    fun entityToStatus(entity: PartnershipEntity): PartnershipStatus
}
