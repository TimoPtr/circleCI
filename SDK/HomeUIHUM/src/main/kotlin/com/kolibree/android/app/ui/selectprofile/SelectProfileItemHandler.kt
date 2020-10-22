/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectprofile

import com.kolibree.account.ProfileFacade
import com.kolibree.android.failearly.FailEarly
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class SelectProfileItemHandler @Inject constructor(
    private val profileFacade: ProfileFacade,
    private val navigator: SelectProfileNavigator
) {
    fun handle(item: SelectProfileItem): Completable = when (item) {
        is ProfileItem -> changeProfileCompletable(item)
        is AddProfileItem -> showAddProfileScreenCompletable()
    }

    private fun changeProfileCompletable(item: ProfileItem): Completable {
        return if (item.profileId == profileFacade.currentProfile?.id) {
            Completable.complete()
        } else {
            profileFacade.setActiveProfileCompletable(item.profileId)
                .subscribeOn(Schedulers.io())
        }
    }

    private fun showAddProfileScreenCompletable(): Completable = Completable.fromAction {
        FailEarly.failIfNotExecutedOnMainThread()
        navigator.showAddProfileScreen()
    }.subscribeOn(AndroidSchedulers.mainThread())
}
