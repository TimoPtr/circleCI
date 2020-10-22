/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.tbreplace

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadCondition
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject
import org.threeten.bp.LocalDate

@VisibleForApp
interface HeadReplacementUseCase {
    fun isDisplayable(mac: String): Maybe<LocalDate>
    fun setReplaceHeadShown(mac: String, currentReplacementDate: LocalDate): Completable
}

internal class HeadReplacementUseCaseImpl @Inject constructor(
    private val brushHeadConditionUseCase: BrushHeadConditionUseCase,
    private val headReplacementProvider: HeadReplacementProvider
) : HeadReplacementUseCase {

    /**
     * If the Dialog replacement should be displayed, this function
     * returns a [Maybe] which is emitting the toothbrush [mac] & the replacement [LocalDate].
     * Or returns an empty [Maybe] otherwise.
     */
    override fun isDisplayable(mac: String): Maybe<LocalDate> {
        return brushHeadConditionUseCase.headCondition(mac)
            .filter { it.condition == BrushHeadCondition.NEEDS_REPLACEMENT }
            .flatMap { headCondition ->
                shouldShow(mac, headCondition.lastReplacementDate).filter { it }
                    .map { headCondition.lastReplacementDate }
            }
    }

    override fun setReplaceHeadShown(mac: String, currentReplacementDate: LocalDate): Completable {
        return headReplacementProvider.setWarningHiddenDate(mac, currentReplacementDate)
    }

    /**
     * @return a [Single] with `true` if the current head replacement date
     * is different from the "never show again" replacement date saved in db.
     * Return `false` otherwise.
     */
    private fun shouldShow(mac: String, currentReplacementDate: LocalDate): Single<Boolean> {
        return headReplacementProvider.getWarningHiddenDate(mac).map { warningHiddenDate ->
            !currentReplacementDate.isEqual(warningHiddenDate)
        }
    }
}
