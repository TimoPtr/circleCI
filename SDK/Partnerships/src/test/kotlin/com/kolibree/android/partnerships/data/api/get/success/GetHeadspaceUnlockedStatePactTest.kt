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
import com.kolibree.android.partnerships.headspace.data.api.KEY_DISCOUNT_CODE
import com.kolibree.android.partnerships.headspace.data.api.KEY_REDEEM_URL
import com.kolibree.android.partnerships.headspace.data.api.KEY_STATUS
import com.kolibree.android.partnerships.headspace.data.api.VALUE_STATUS_UNLOCKED
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Unlocked
import io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody
import junit.framework.TestCase.assertEquals

const val VALUE_DISCOUNT_CODE = "TEST_DISCOUNT_CODE"
const val VALUE_REDEEM_URL = "http://redeem.url"

class GetHeadspaceUnlockedStatePactTest : GetPartnershipStatePactTest() {

    override val expectedResponseBody: DslPart = newJsonBody { root ->
        root.`object`("partners") { partners ->
            partners.`object`("headspace") { headspace ->
                headspace
                    .stringValue(KEY_STATUS, VALUE_STATUS_UNLOCKED)
                    .stringValue(KEY_DISCOUNT_CODE,
                        VALUE_DISCOUNT_CODE
                    )
                    .stringValue(KEY_REDEEM_URL,
                        VALUE_REDEEM_URL
                    )
            }
        }
    }.build()

    override fun validateApiResponse(body: PartnershipResponse) {
        val parsedStatus =
            HeadspaceApiMapper.apiResponseToStatus(state.profileId, body.data["headspace"]!!)
        assertEquals(
            Unlocked(state.profileId,
                VALUE_DISCOUNT_CODE,
                VALUE_REDEEM_URL
            ),
            parsedStatus
        )
    }
}
