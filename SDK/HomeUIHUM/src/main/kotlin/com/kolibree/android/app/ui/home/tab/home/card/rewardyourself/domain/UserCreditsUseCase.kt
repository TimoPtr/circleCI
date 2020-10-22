/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.rewardyourself.domain

import com.kolibree.android.rewards.SmilesUseCase
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.domain.model.Price
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import javax.inject.Inject

internal interface UserCreditsUseCase {
    fun getUserCredits(): Flowable<Price>
}

internal class UserCreditsUseCaseImpl @Inject constructor(
    private val shopifyClientWrapper: ShopifyClientWrapper,
    private val smilesUseCase: SmilesUseCase
) : UserCreditsUseCase {

    override fun getUserCredits(): Flowable<Price> {
        return Flowables
            .combineLatest(
                shopifyClientWrapper.getStoreDetails().toFlowable(),
                smilesUseCase.smilesAmountStream()
            )
            .map { (storeDetails, smilesCount) ->
                Price.createFromSmiles(smilesCount, storeDetails)
            }
    }
}
