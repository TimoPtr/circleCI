/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.list

import androidx.annotation.Keep
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.dagger.scopes.ActivityScope
import io.reactivex.Observable
import javax.inject.Inject

@Keep
interface ShopListScrollUseCase {

    /**
     * Informs shop that we want to scroll to the specific item
     * If shop fragment is loaded and items are available scroll will be performed immediately
     * If shop fragment doesn't exist scroll will be performed right after first list is loaded
     */
    fun scrollToItem(itemId: String)

    /**
     * Returns latest id that we want to scroll to
     */
    fun getItemIdToScroll(): Observable<String>
}

@ActivityScope
internal class ShopListScrollUseCaseImpl @Inject constructor() : ShopListScrollUseCase {

    private val subject = BehaviorRelay.create<String>()

    override fun scrollToItem(itemId: String) {
        subject.accept(itemId)
    }

    override fun getItemIdToScroll(): Observable<String> {
        return subject
    }
}
