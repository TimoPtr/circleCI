/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.api.get.success

import au.com.dius.pact.consumer.dsl.DslPart
import com.kolibree.android.partnerships.data.api.model.PartnershipResponse
import com.kolibree.android.partnerships.headspace.data.api.HeadspaceApiMapper
import com.kolibree.android.partnerships.headspace.data.api.KEY_STATUS
import com.kolibree.android.partnerships.headspace.data.api.VALUE_STATUS_INACTIVE
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import junit.framework.TestCase.assertEquals

class GetHeadspaceInactiveStatePactTest : GetPartnershipStatePactTest() {

    override val expectedResponseBody: DslPart = newJsonBody { root ->
        root.`object`("partners") { partners ->
            partners.`object`("headspace") { headspace ->
                headspace
                    .stringValue(KEY_STATUS, VALUE_STATUS_INACTIVE)
            }
        }
    }.build()

    override fun validateApiResponse(body: PartnershipResponse) {
        val parsedStatus =
            HeadspaceApiMapper.apiResponseToStatus(state.profileId, body.data["headspace"]!!)
        assertEquals(HeadspacePartnershipStatus.Inactive(state.profileId), parsedStatus)
    }
}
