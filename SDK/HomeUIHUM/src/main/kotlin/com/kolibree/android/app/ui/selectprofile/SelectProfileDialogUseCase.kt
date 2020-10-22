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
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

@VisibleForApp
interface SelectProfileDialogUseCase {
    fun showDialogAndHandleSelectedItem(): Completable
}

internal class SelectProfileDialogUseCaseImpl @Inject constructor(
    private val selectProfileUseCase: SelectProfileUseCase,
    private val navigator: SelectProfileNavigator
) : SelectProfileDialogUseCase {

    override fun showDialogAndHandleSelectedItem(): Completable {
        return selectProfileUseCase.prepareItems()
            .showProfileDialog()
            .handleSelectedItem()
    }

    private fun Single<List<SelectProfileItem>>.showProfileDialog(): Maybe<SelectProfileItem> {
        return flatMapMaybe { items ->
            Maybe.just(items)
                .flatMap(navigator::selectProfileDialogMaybe)
        }
    }

    private fun Maybe<SelectProfileItem>.handleSelectedItem(): Completable {
        return flatMapCompletable { item ->
            selectProfileUseCase.handleSelectedItem(item)
        }
    }
}
