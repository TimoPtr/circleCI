/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selecttoothbrush

import com.kolibree.account.utils.ToothbrushesForProfileUseCase
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@VisibleForApp
interface SelectToothbrushUseCase {

    /**
     * Displays dialog that allows user to select a brush.
     *
     * Returns [AccountToothbrush] if selected, completes otherwise.
     */
    fun selectToothbrush(): Maybe<KLTBConnection>
}

internal class SelectToothbrushUseCaseImpl @Inject constructor(
    private val toothbrushProvider: ToothbrushesForProfileUseCase,
    private val navigator: SelectToothbrushNavigator,
    private val iconProvider: SelectToothbrushIconProvider
) : SelectToothbrushUseCase {

    override fun selectToothbrush(): Maybe<KLTBConnection> {
        return toothbrushProvider
            .currentProfileToothbrushesOnceAndStream()
            .firstElement()
            .subscribeOn(Schedulers.io())
            .flatMap { toothbrushes ->
                when {
                    toothbrushes.isEmpty() -> Maybe.empty()
                    toothbrushes.size == 1 -> Maybe.just(toothbrushes.first())
                    else -> showPicker(toothbrushes)
                }
            }
    }

    private fun showPicker(toothbrushes: List<KLTBConnection>): Maybe<KLTBConnection> {
        return Maybe.just(toothbrushes)
            .mapToToothbrushInfo()
            .flatMap(navigator::selectToothbrush)
            .map(SelectToothbrushItem::connection)
    }

    private fun Maybe<List<KLTBConnection>>.mapToToothbrushInfo(): Maybe<List<SelectToothbrushItem>> {
        return map { toothbrushes -> toothbrushes.map(::toothbrushInfo) }
    }

    private fun toothbrushInfo(connection: KLTBConnection): SelectToothbrushItem {
        return SelectToothbrushItem(
            connection = connection,
            iconRes = iconProvider.getIconFor(connection)
        )
    }
}
