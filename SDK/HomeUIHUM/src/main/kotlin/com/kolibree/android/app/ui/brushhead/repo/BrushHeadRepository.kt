/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.repo

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.brushhead.api.BrushHeadInformationApi
import com.kolibree.android.app.ui.brushhead.api.model.request.BrushHeadInformationResponse
import com.kolibree.android.app.ui.brushhead.api.model.request.data.BrushHeadData
import com.kolibree.android.app.ui.brushhead.repo.mapper.BrushHeadInformationResponseMapper
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.commons.models.StrippedMac
import com.kolibree.android.network.toParsedResponseSingle
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime
import retrofit2.Response

@VisibleForApp
interface BrushHeadRepository {
    fun hasBrushHeadReplacedDateOnce(mac: String): Single<Boolean>
    fun brushHeadInformationOnce(mac: String): Single<BrushHeadInformation>
    fun sendReplacedDateToApiCompletable(
        mac: String,
        serialNumber: String,
        replacedDate: OffsetDateTime
    ): Completable

    fun newBrushHeadCompletable(mac: String): Completable
    fun getLastDateSentToApiMaybe(mac: String): Maybe<OffsetDateTime>
    fun setLastDateSentToApiCompletable(mac: String, replacedDate: OffsetDateTime): Completable
    fun getBrushHeadInformationFromApi(
        mac: String,
        serialNumber: String
    ): Single<BrushHeadInformation>

    fun writeBrushHeadInfo(mac: BrushHeadInformation): Completable
}

internal class BrushHeadRepositoryImpl @Inject constructor(
    private val replaceDateReader: BrushHeadInformationReader,
    private val replaceDateWriter: BrushHeadReplacedDateWriter,
    private val brushHeadInformationApi: BrushHeadInformationApi,
    private val accountDatastore: AccountDatastore,
    private val brushHeadDateSendApiProvider: BrushHeadDateSendApiProvider
) : BrushHeadRepository {

    override fun newBrushHeadCompletable(mac: String): Completable {
        return Completable.fromAction {
            replaceDateWriter.writeReplacedDateNow(mac)
        }
    }

    @Suppress("LongMethod")
    override fun sendReplacedDateToApiCompletable(
        mac: String,
        serialNumber: String,
        replacedDate: OffsetDateTime
    ): Completable {
        if (serialNumber.isBlank() || mac.isBlank()) {
            return Completable.error(getEmptyErrorException(serialNumber, mac))
        }

        return accountDatastore.getAccountMaybe()
            .flatMapSingle {
                callReplaceBrushHeadApi(
                    accountInternal = it,
                    mac = StrippedMac.fromMac(mac),
                    serialNumber = serialNumber,
                    replacedDate = replacedDate
                )
            }
            .toParsedResponseSingle()
            .flatMapCompletable {
                brushHeadDateSendApiProvider.setLastReplacedDateSentCompletable(mac, replacedDate)
            }
    }

    private fun callReplaceBrushHeadApi(
        accountInternal: AccountInternal,
        mac: StrippedMac,
        serialNumber: String,
        replacedDate: OffsetDateTime
    ): Single<Response<BrushHeadInformationResponse>> {
        return brushHeadInformationApi.updateBrushHead(
            accountId = accountInternal.id,
            profileId = accountInternal.ownerProfileId,
            macAddress = mac,
            serialNumber = serialNumber,
            body = BrushHeadData(replacedDate)
        )
    }

    override fun hasBrushHeadReplacedDateOnce(mac: String): Single<Boolean> {
        return replaceDateReader.read(mac).isEmpty.map { !it }
    }

    override fun brushHeadInformationOnce(mac: String): Single<BrushHeadInformation> {
        return replaceDateReader.read(mac)
            .switchIfEmpty(replaceDateWriter.writeReplacedDateNow(mac))
    }

    /**
     * Retrieve the last brush head replaced date sent to the API for a given toothbrush
     */
    override fun getLastDateSentToApiMaybe(mac: String): Maybe<OffsetDateTime> {
        return brushHeadDateSendApiProvider.getLastReplacedDateSentMaybe(mac)
    }

    /**
     * Set the last brush head replaced date sent to the API for a given toothbrush
     */
    override fun setLastDateSentToApiCompletable(
        mac: String,
        replacedDate: OffsetDateTime
    ): Completable {
        return brushHeadDateSendApiProvider.setLastReplacedDateSentCompletable(mac, replacedDate)
    }

    override fun getBrushHeadInformationFromApi(
        mac: String,
        serialNumber: String
    ): Single<BrushHeadInformation> {
        if (serialNumber.isBlank() || mac.isBlank()) {
            return Single.error(getEmptyErrorException(serialNumber, mac))
        }

        return accountDatastore.getAccountMaybe()
            .callBrushBrushHeadInformationApi(mac, serialNumber)
            .toParsedResponseSingle()
            .map { BrushHeadInformationResponseMapper.map(mac, it) }
    }

    override fun writeBrushHeadInfo(brushHeadInformation: BrushHeadInformation): Completable {
        return replaceDateWriter.writeBrushHeadInformation(brushHeadInformation)
    }

    private fun Maybe<AccountInternal>.callBrushBrushHeadInformationApi(
        mac: String,
        serialNumber: String
    ): Single<Response<BrushHeadInformationResponse>> {
        return flatMapSingle { accountInternal ->
            brushHeadInformationApi.getBrushHeadInformation(
                accountId = accountInternal.id,
                profileId = accountInternal.ownerProfileId,
                macAddress = StrippedMac.fromMac(mac),
                serialNumber = serialNumber
            )
        }
    }

    private fun getEmptyErrorException(serialNumber: String, mac: String) =
        IllegalArgumentException("Either serialNumber = '$serialNumber' or mac = '$mac' are empty.")
}
