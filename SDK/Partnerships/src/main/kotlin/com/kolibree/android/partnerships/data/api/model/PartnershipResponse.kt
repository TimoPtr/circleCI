/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.api.model

import com.google.gson.annotations.SerializedName
import com.kolibree.android.annotation.VisibleForApp

internal typealias PartnershipData = Map<String, Any?>

/**
 * Holds current statuses for all supported partnerships for current account and profile.
 *
 * Note that this is a generic response object, hence the data structure for every partnership
 * can be different. It's up to partnership-specific code (especially [PartnershipApiMapper])
 * to transform the payload to useful structure.
 *
 * To see Headspace structure, please check:
 * [https://kolibree.atlassian.net/wiki/spaces/SOF/pages/873627657/Headspace+partnership]
 */
@VisibleForApp
data class PartnershipResponse(
    @SerializedName("partners") val data: Map</* partner name */ String, PartnershipData>
) {
    @Transient
    val partners = data.keys
}
