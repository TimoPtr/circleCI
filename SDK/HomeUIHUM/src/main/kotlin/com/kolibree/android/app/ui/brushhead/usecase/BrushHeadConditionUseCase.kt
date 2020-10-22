/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.usecase

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.brushhead.repo.model.BrushHeadInformation
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition.Companion.fromUsedPercentage
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorkerConfigurator
import com.kolibree.android.app.ui.toothbrushsettings.worker.ReplaceBrushHeadWorkerConfigurator.Payload
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit

@VisibleForApp
interface BrushHeadConditionUseCase {
    fun headCondition(mac: String): Single<BrushHeadConditionData>
    fun resetBrushHead(mac: String, serial: String): Single<BrushHeadConditionData>
    fun updateBrushHeadDateIfNeeded(mac: String): Maybe<AccountToothbrush>
    fun getBrushHeadInformationFromApi(
        mac: String,
        serialNumber: String
    ): Single<BrushHeadInformation>

    fun writeBrushHeadInfo(brushHeadInformation: BrushHeadInformation): Completable
}

internal class BrushHeadConditionUseCaseImpl @Inject constructor(
    private val brushHeadRepository: BrushHeadRepository,
    private val replaceBrushHeadWorkerConfigurator: ReplaceBrushHeadWorkerConfigurator,
    private val toothbrushRepository: ToothbrushRepository
) : BrushHeadConditionUseCase {

    override fun headCondition(mac: String): Single<BrushHeadConditionData> {
        return brushHeadRepository.brushHeadInformationOnce(mac)
            .map { brushHeadInfo ->
                BrushHeadConditionData(
                    condition = fromUsedPercentage(brushHeadInfo.percentageLeft),
                    lastReplacementDate = brushHeadInfo.resetDate.toLocalDate()
                )
            }
    }

    override fun resetBrushHead(mac: String, serial: String): Single<BrushHeadConditionData> {
        return brushHeadRepository.newBrushHeadCompletable(mac)
            .andThen(headCondition(mac))
            .doOnSuccess { replaceBrushHeadWorkerConfigurator.configure(Payload(mac, serial)) }
    }

    override fun updateBrushHeadDateIfNeeded(mac: String): Maybe<AccountToothbrush> {
        return getLastLocalReplacedDate(mac)
            .proceedIfLastDateSentIsOlder(mac)
            .flatMap { toothbrushRepository.getAccountToothbrush(mac) }
            .doOnSuccess { accountToothbrush ->
                replaceBrushHeadWorkerConfigurator.configure(Payload(mac, accountToothbrush.serial))
            }
    }

    override fun getBrushHeadInformationFromApi(
        mac: String,
        serialNumber: String
    ): Single<BrushHeadInformation> {
        return brushHeadRepository.getBrushHeadInformationFromApi(mac, serialNumber)
    }

    override fun writeBrushHeadInfo(brushHeadInformation: BrushHeadInformation): Completable {
        return brushHeadRepository.writeBrushHeadInfo(brushHeadInformation)
    }

    private fun getLastLocalReplacedDate(mac: String): Maybe<OffsetDateTime> {
        return brushHeadRepository.hasBrushHeadReplacedDateOnce(mac)
            .filter { it }
            .flatMap {
                brushHeadRepository.brushHeadInformationOnce(mac).map { it.resetDate }.toMaybe()
            }
    }

    private fun Maybe<OffsetDateTime>.proceedIfLastDateSentIsOlder(mac: String): Maybe<OffsetDateTime> {
        return flatMap { localDate ->
            brushHeadRepository.getLastDateSentToApiMaybe(mac)
                .defaultIfEmpty(OffsetDateTime.MIN)
                .filter { lastApiReplacedDate ->
                    localDate.truncatedTo(ChronoUnit.SECONDS).isAfter(lastApiReplacedDate)
                }
                .map { localDate }
        }
    }
}

@VisibleForApp
data class BrushHeadConditionData(
    val condition: BrushHeadCondition,
    val lastReplacementDate: LocalDate
)

@VisibleForApp
enum class BrushHeadCondition {
    GOOD, GETTING_OLDER, NEEDS_REPLACEMENT;

    internal companion object {
        /**
         * @see https://kolibree.atlassian.net/wiki/spaces/PROD/pages/431718454/Toothbrush+settings#Brush-head-condition
         */
        fun fromUsedPercentage(usedPercentage: Int): BrushHeadCondition {
            return when {
                usedPercentage >= GOOD_THRESHOLD -> GOOD
                usedPercentage >= GETTING_OLDER_THRESHOLD -> GETTING_OLDER
                else -> NEEDS_REPLACEMENT
            }
        }
    }
}

private const val GOOD_THRESHOLD = 50
private const val GETTING_OLDER_THRESHOLD = 1
