/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mock

import com.fasterxml.jackson.databind.ObjectMapper
import com.kolibree.sdkws.api.response.UpdateToothbrushResponse
import java.io.IOException
import timber.log.Timber

fun updateToothbrushResponseWithKolibreePro(): UpdateToothbrushResponse? {
    try {
        return ObjectMapper()
            .readValue(
                kolibreeProJsonResponse(),
                UpdateToothbrushResponse::class.java
            )
    } catch (e: IOException) {
        Timber.e(e)
    }
    return null
}

fun kolibreeProJsonResponse(): String? {
    return """{
	"tokens": [{
        "name": "",
		"practitioner": {
          "name_title" : "$RESPONSE_PRACTICIONER_NAME"
        },
		"token": "$RESPONSE_AUTH_TOKEN"
	}]
}"""
}

const val RESPONSE_AUTH_TOKEN = "auth_tokeeeeen"
const val RESPONSE_PRACTICIONER_NAME = "BEST DOCTOR"
