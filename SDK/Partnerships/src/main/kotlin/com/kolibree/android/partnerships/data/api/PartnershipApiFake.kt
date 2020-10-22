/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.api

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.partnerships.data.api.model.PartnershipData
import com.kolibree.android.partnerships.data.api.model.PartnershipResponse
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.headspace.data.api.KEY_DISCOUNT_CODE
import com.kolibree.android.partnerships.headspace.data.api.KEY_POINTS_NEEDED
import com.kolibree.android.partnerships.headspace.data.api.KEY_POINTS_THRESHOLD
import com.kolibree.android.partnerships.headspace.data.api.KEY_REDEEM_URL
import com.kolibree.android.partnerships.headspace.data.api.KEY_STATUS
import com.kolibree.android.partnerships.headspace.data.api.VALUE_STATUS_INACTIVE
import com.kolibree.android.partnerships.headspace.data.api.VALUE_STATUS_UNLOCKED
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Response

// TODO
//  This class is used in Espresso.
//  Don't delete when API is ready, just move it to `androidTestHum` source set
@VisibleForApp
object PartnershipApiFake : PartnershipApi {

    private val state =
        mutableMapOf</* profileId */ Long, PartnershipResponse>()

    private val nextStatesSequence = mutableListOf<PartnershipResponse>()

    init {
        populateHeadspaceStateSequence(this)
    }

    override fun getPartnerships(
        accountId: Long,
        profileId: Long
    ): Single<Response<PartnershipResponse>> {
        synchronized(this) {
            if (nextStatesSequence.isNotEmpty()) {
                setStateInternal(profileId, nextStatesSequence.removeFirst().data)
            }
        }
        return Single.just(Response.success(getState(profileId)))
    }

    override fun unlockPartnership(
        accountId: Long,
        profileId: Long,
        partnerId: String
    ): Single<Response<Unit>> =
        updateAndReturnState(profileId, partnerId) { unlockedState(Partner.from(partnerId)) }

    override fun disablePartnership(
        accountId: Long,
        profileId: Long,
        partnerId: String
    ): Single<Response<Unit>> =
        updateAndReturnState(profileId, partnerId) { inactiveState(Partner.from(partnerId)) }

    private fun getState(profileId: Long) = synchronized(this) {
        state[profileId] ?: run {
            val defaultState = PartnershipResponse(
                defaultState().toResponsePayload()
            )
            state[profileId] = defaultState
            defaultState
        }
    }

    private var responseError: Response<Unit>? = null

    @VisibleForTesting
    fun setApiError(httpErrorCode: Int, internalErrorCode: Int) {
        val apiErrorJson = """
            {
                "display_message": "Something went wrong.",
                "message": "E$internalErrorCode: Something went wrong.",
                "internal_error_code": $internalErrorCode,
                "detail": "Something went wrong.",
                "http_code": $httpErrorCode
            }
        """.trimIndent()

        responseError = Response.error(
            httpErrorCode,
            ResponseBody.create(MediaType.parse("application/json"), apiErrorJson)
        )
    }

    @VisibleForTesting
    fun setError(httpErrorCode: Int) {
        responseError = Response.error(
            httpErrorCode,
            ResponseBody.create(null, "")
        )
    }

    fun setState(profileId: Long, state: Map<Partner, PartnershipData>) {
        setStateInternal(profileId, state.toResponsePayload())
    }

    fun setStateSequence(sequence: List<Map<Partner, PartnershipData>>) {
        synchronized(this) {
            nextStatesSequence.clear()
            nextStatesSequence.addAll(sequence.map { response ->
                PartnershipResponse(response.toResponsePayload())
            })
        }
    }

    private fun setStateInternal(profileId: Long, response: Map<String, PartnershipData>) {
        synchronized(this) {
            state[profileId] = PartnershipResponse(response)
        }
    }

    private fun updateAndReturnState(
        profileId: Long,
        partnerId: String,
        newStateFunction: () -> PartnershipData
    ): Single<Response<Unit>> {
        if (responseError != null) {
            return Single.just<Response<Unit>>(responseError)
                .also {
                    responseError = null
                }
        }

        val profileState = getState(profileId)
        val partnershipData = profileState.data.toMutableMap()
        partnershipData[partnerId] = newStateFunction()
        setStateInternal(profileId, partnershipData)
        return Single.just(Response.success(Unit))
    }

    @VisibleForTesting
    fun defaultState(): Map<Partner, PartnershipData> =
        Partner.values()
            .map { partner -> Pair(partner, defaultState(partner)) }
            .toMap()

    @VisibleForTesting
    fun defaultState(partner: Partner): PartnershipData = when (partner) {
        Partner.HEADSPACE -> inProgressState(partner)
        Partner.TEST_ONLY -> emptyMap()
    }

    @VisibleForTesting
    fun inProgressState(partner: Partner): PartnershipData = when (partner) {
        Partner.HEADSPACE -> mapOf(
            KEY_POINTS_NEEDED to HEADSPACE_POINTS_THRESHOLD,
            KEY_POINTS_THRESHOLD to HEADSPACE_POINTS_THRESHOLD
        )
        Partner.TEST_ONLY -> emptyMap()
    }

    @VisibleForTesting
    fun unlockedState(partner: Partner): PartnershipData = when (partner) {
        Partner.HEADSPACE -> mapOf(
            KEY_STATUS to VALUE_STATUS_UNLOCKED,
            KEY_DISCOUNT_CODE to HEADSPACE_DISCOUNT_CODE,
            KEY_REDEEM_URL to HEADSPACE_REDEEM_URL
        )
        Partner.TEST_ONLY -> emptyMap()
    }

    @VisibleForTesting
    fun inactiveState(partner: Partner): PartnershipData = when (partner) {
        Partner.HEADSPACE -> mapOf(KEY_STATUS to VALUE_STATUS_INACTIVE)
        Partner.TEST_ONLY -> emptyMap()
    }
}

private fun Map<Partner, PartnershipData>.toResponsePayload() =
    map { (partner, payload) -> partner.partnerName to payload }.toMap()

// TODO remove once API is ready
private fun populateHeadspaceStateSequence(fake: PartnershipApiFake) {
    fake.setStateSequence(
        listOf(
            mapOf(
                Partner.HEADSPACE to mapOf(
                    KEY_POINTS_NEEDED to 200,
                    KEY_POINTS_THRESHOLD to 200
                )
            ),
            mapOf(Partner.HEADSPACE to mapOf(KEY_POINTS_NEEDED to 50, KEY_POINTS_THRESHOLD to 200)),
            mapOf(Partner.HEADSPACE to mapOf(KEY_POINTS_NEEDED to 0, KEY_POINTS_THRESHOLD to 200)),
            mapOf(
                Partner.HEADSPACE to mapOf(
                    KEY_STATUS to VALUE_STATUS_UNLOCKED,
                    KEY_DISCOUNT_CODE to HEADSPACE_DISCOUNT_CODE,
                    KEY_REDEEM_URL to HEADSPACE_REDEEM_URL
                )
            ),
            mapOf(Partner.HEADSPACE to mapOf(KEY_STATUS to VALUE_STATUS_INACTIVE))
        )
    )
}

const val HEADSPACE_POINTS_THRESHOLD = 200
const val HEADSPACE_DISCOUNT_CODE = "discount_code"
const val HEADSPACE_REDEEM_URL = "https://www.headspace.com"
