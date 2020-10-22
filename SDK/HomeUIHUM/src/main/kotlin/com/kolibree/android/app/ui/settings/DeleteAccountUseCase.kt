/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import com.kolibree.account.AccountFacade
import com.kolibree.android.google.auth.GoogleSignInWrapper
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class DeleteAccountUseCase @Inject constructor(
    private val accountFacade: AccountFacade,
    private val googleSignInWrapper: GoogleSignInWrapper
) {

    fun deleteAccount(): Completable {
        return accountFacade.deleteAccount()
            .subscribeOn(Schedulers.io())
            .doOnComplete { googleSignInWrapper.revokeAccess() }
    }
}
