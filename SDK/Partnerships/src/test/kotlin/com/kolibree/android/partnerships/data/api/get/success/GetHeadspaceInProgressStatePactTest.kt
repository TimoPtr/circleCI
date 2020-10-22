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
import com.kolibree.android.partnerships.headspace.data.api.KEY_POINTS_NEEDED
import com.kolibree.android.partnerships.headspace.data.api.KEY_POINTS_THRESHOLD
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.InProgress
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import junit.framework.TestCase.assertEquals

class GetHeadspaceInProgressStatePactTest : GetPartnershipStatePactTest() {

    override val expectedResponseBody: DslPart = newJsonBody { root ->
        root.`object`("partners") { partners ->
            partners.`object`("headspace") { headspace ->
                headspace
                    .numberValue(KEY_POINTS_NEEDED, 40)
                    .numberValue(KEY_POINTS_THRESHOLD, 200)
            }
        }
    }.build()

    override fun validateApiResponse(body: PartnershipResponse) {
        val parsedStatus =
            HeadspaceApiMapper.apiResponseToStatus(state.profileId, body.data["headspace"]!!)
        assertEquals(
            InProgress(state.profileId, 40, 200),
            parsedStatus
        )
    }
}
