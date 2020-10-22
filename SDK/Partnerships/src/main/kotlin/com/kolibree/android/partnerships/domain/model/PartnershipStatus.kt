/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.domain.model

import com.kolibree.android.annotation.VisibleForApp

/**
 * Base type for all partnership status updates.
 */
@VisibleForApp
interface PartnershipStatus {
    val profileId: Long
    val partner: Partner
}
