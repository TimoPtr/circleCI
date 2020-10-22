/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.persistence

import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.partnerships.data.persistence.model.PartnershipEntity
import io.reactivex.Flowable

/**
 * Base interface for partnership DAOs.
 *
 * If you want to add support for new partnership, you need to provide implementation of this
 * interface and inject it into Dagger graph with matching [Partner] map key.
 *
 * Example:
 * ```
 * @Provides
 * @IntoMap
 * @PartnerKey(Partner.MY_NEW_PARTNERSHIP)
 * fun provideDao(impl: MyNewPartnershipDao): PartnershipDao = impl
 * ```
 */
internal interface PartnershipDao : Truncable {

    fun insertOrReplace(entity: PartnershipEntity)

    fun findBy(profileId: Long): Flowable<PartnershipEntity>
}
