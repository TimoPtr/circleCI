/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.toParsedResponseSingle
import com.kolibree.android.partnerships.data.api.PartnershipApi
import com.kolibree.android.partnerships.data.api.PartnershipApiMapper
import com.kolibree.android.partnerships.data.api.model.PartnershipResponse
import com.kolibree.android.partnerships.data.persistence.PartnershipDao
import com.kolibree.android.partnerships.data.persistence.PartnershipPersistenceMapper
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.domain.model.PartnershipStatus
import com.kolibree.android.utils.callSafely
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Uses plugin architecture and allows adding support for new partnerships through common
 * interface.
 *
 * In order to add support to new partnership, you need to:
 * - declare your new partnership ID in [Partner] enum
 * - provide implementations for API mapper, persistence mapper and persistence DAO
 * - inject all 3 implementation into Dagger graph, using `@IntoMap` bindings and corresponding `@PartnerKey`
 *
 * @see Partner
 * @see PartnershipApiMapper
 * @see PartnershipPersistenceMapper
 * @see PartnershipDao
 */
@VisibleForApp
interface PartnershipStatusRepository {

    /**
     * Re-fetches data for all registered partnerships from the API for current
     * account and profile and store them in respective databases.
     *
     * @return [Completable] which completes when data re-fetching process ends.
     */
    fun refreshPartnerships(
        accountId: Long,
        profileId: Long
    ): Completable

    /**
     * Subscribes for status updates of particular [Partner] for current account and profile.
     *
     * Stream can complete, normally on logout.
     *
     * @return [Flowable] with stream of partnership statuses.
     */
    fun getPartnershipStatus(
        accountId: Long,
        profileId: Long,
        partner: Partner
    ): Flowable<PartnershipStatus>

    /**
     * Unlocks partnership specified in [partner] for [accountId] and [profileId].
     *
     * Once unlock completes successfully, it automatically subscribes to [refreshPartnerships]
     *
     * Internet is required for this operation to complete. Offline mode is not supported.
     *
     * On error, it can emit a [DiscountCodeNotAvailableException], an [ApiError] or any
     * [Throwable]
     *
     * @return [Completable] which completes when partnership is unlocked and refreshed
     */
    fun unlockPartnership(
        accountId: Long,
        profileId: Long,
        partner: Partner
    ): Completable

    /**
     * Disables partnership specified in [partner] for [accountId] and [profileId].
     *
     * Once disable completes successfully, it automatically subscribes to [refreshPartnerships]
     *
     * Internet is required for this operation to complete. Offline mode is not supported.
     *
     * @return [Completable] which completes when partnership is disabled and refreshed
     */
    fun disablePartnership(
        accountId: Long,
        profileId: Long,
        partner: Partner
    ): Completable
}

/**
 * Collection of one-way mappers between generic partnership API and domain objects for each
 * supported partnership. Repository uses it to retrieve API mapper to handle each partnership's
 * API payload.
 *
 * @see PartnershipApiMapper for information how to add your mapper to this collection.
 */
internal typealias PartnershipApiMappers = Map<Partner, @JvmSuppressWildcards PartnershipApiMapper>

/**
 * Collection of two-way mappers between domain objects and data entities for each supported
 * partnership. Repository uses it to retrieve persistence mapper to handle each partnership's
 * data payload before storing and after retrieving data from the database.
 *
 * @see PartnershipPersistenceMapper for information how to add your mapper to this collection.
 */
internal typealias PartnershipPersistenceMappers =
    Map<Partner, @JvmSuppressWildcards PartnershipPersistenceMapper>

/**
 * Collection of DAOs for every supported partnership. Repository uses it to retrieve
 * DAO object to save/retrieve partnership data from the database.
 *
 * @see PartnershipDao for information how to add your DAO to this collection
 */
internal typealias PartnershipDaos = Map<Partner, @JvmSuppressWildcards PartnershipDao>

internal class PartnershipStatusRepositoryImpl @Inject constructor(
    private val api: PartnershipApi,
    private val apiMappers: PartnershipApiMappers,
    private val daos: PartnershipDaos,
    private val persistenceMappers: PartnershipPersistenceMappers
) : PartnershipStatusRepository {

    override fun unlockPartnership(
        accountId: Long,
        profileId: Long,
        partner: Partner
    ): Completable =
        api.unlockPartnership(
            accountId = accountId,
            profileId = profileId,
            partnerId = partner.partnerName
        )
            .toParsedResponseSingle()
            .toUnlockSpecificError()
            .ignoreElement()
            .andThen(refreshPartnerships(accountId, profileId))

    override fun disablePartnership(
        accountId: Long,
        profileId: Long,
        partner: Partner
    ): Completable =
        api.disablePartnership(
            accountId = accountId,
            profileId = profileId,
            partnerId = partner.partnerName
        )
            .toParsedResponseSingle()
            .ignoreElement()
            .andThen(refreshPartnerships(accountId, profileId))

    override fun refreshPartnerships(
        accountId: Long,
        profileId: Long
    ): Completable =
        api.getPartnerships(accountId, profileId)
            .toParsedResponseSingle()
            .map { response -> apiResponseToStatus(profileId, response) }
            .flatMapCompletable { statusList -> persistStatusList(statusList) }

    override fun getPartnershipStatus(
        accountId: Long,
        profileId: Long,
        partner: Partner
    ): Flowable<PartnershipStatus> =
        daos.getValue(partner)
            .findBy(profileId)
            .map { entity -> persistenceMappers.getValue(partner).entityToStatus(entity) }
            .doOnError { e ->
                val message = "getPartnershipStatus received an error, " +
                    "terminating partnership updates (message: ${e.message})"
                FailEarly.fail(exception = e, message = message)
            }

    private fun apiResponseToStatus(
        profileId: Long,
        response: PartnershipResponse
    ): List<PartnershipStatus> {
        val parsedPartners = mutableListOf<PartnershipStatus>()
        response.data.forEach { (partnerName, partnerData) ->
            callSafely {
                // This way, if one transaction/mapping fails, we can still try to handle others
                val status = apiMappers.getValue(Partner.from(partnerName))
                    .apiResponseToStatus(profileId, partnerData)
                parsedPartners.add(status)
            }
        }
        return parsedPartners
    }

    private fun persistStatusList(list: List<PartnershipStatus>) =
        Completable.fromCallable {
            list.forEach { item ->
                // This way, if one transaction/mapping fails, we can still try to handle others
                callSafely { persistResponseItem(item) }
            }
        }

    private fun persistResponseItem(item: PartnershipStatus) {
        daos.getValue(item.partner).insertOrReplace(
            persistenceMappers.getValue(item.partner).statusToEntity(item)
        )
    }
}

private fun <T> Single<T>.toUnlockSpecificError(): Single<T> =
    onErrorResumeNext { throwable ->
        if (throwable.isDiscountNotAvailableError()) {
            Single.error(DiscountCodeNotAvailableException(throwable))
        } else {
            Single.error(throwable)
        }
    }

/**
 * @see https://kolibree.atlassian.net/wiki/spaces/SOF/pages/873627657/Headspace+partnership#Unlock-Headspace-card
 */
private fun Throwable.isDiscountNotAvailableError(): Boolean {
    return this is ApiError &&
        internalErrorCode == UNLOCK_INTERNAL_ERROR &&
        httpCode == DISCOUNT_CODE_NOT_AVAILABLE_ERROR_CODE
}

private const val UNLOCK_INTERNAL_ERROR = 115
private const val DISCOUNT_CODE_NOT_AVAILABLE_ERROR_CODE = 503
