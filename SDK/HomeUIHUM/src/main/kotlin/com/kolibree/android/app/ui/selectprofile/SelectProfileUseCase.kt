/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectprofile

import com.kolibree.android.annotation.VisibleForApp
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

@VisibleForApp
interface SelectProfileUseCase {

    /**
     * Prepares available profile items.
     */
    fun prepareItems(): Single<List<SelectProfileItem>>

    /**
     * Handles selected item.
     * This method will start AddProfile screen or change active profile.
     */
    fun handleSelectedItem(selectedItem: SelectProfileItem): Completable
}

internal class SelectProfileUseCaseImpl @Inject constructor(
    private val selectProfileItemsProvider: SelectProfileItemsProvider,
    private val selectProfileItemHandler: SelectProfileItemHandler
) : SelectProfileUseCase {

    override fun prepareItems(): Single<List<SelectProfileItem>> {
        return selectProfileItemsProvider.selectProfileItems()
    }

    override fun handleSelectedItem(selectedItem: SelectProfileItem): Completable {
        return selectProfileItemHandler.handle(selectedItem)
    }
}

internal object NotSupportedSelectProfileUseCase : SelectProfileUseCase {
    override fun prepareItems(): Single<List<SelectProfileItem>> = Single.never()

    override fun handleSelectedItem(selectedItem: SelectProfileItem): Completable =
        Completable.complete()
}
