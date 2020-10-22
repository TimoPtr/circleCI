/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.profile

import com.kolibree.account.Account
import com.kolibree.account.AccountFacade
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.dagger.SingleThreadScheduler
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Sets Owner profile as Active ff the deleted profile was currently active
 */
internal class ResetActiveProfileHook
@Inject constructor(
    private val currentProfileProvider: CurrentProfileProvider,
    private val accountFacade: AccountFacade,
    private val connector: IKolibreeConnector,
    @SingleThreadScheduler private val timeoutScheduler: Scheduler
) : ProfileDeletedHook {
    override fun onProfileDeleted(profileId: Long): Completable {
        return Single.zip(
            currentProfileProvider.currentProfileSingle(),
            accountFacade.getAccountSingle(),
            BiFunction<Profile,
                Account,
                Completable> { currentProfile, account ->
                if (currentProfile.id == profileId) {
                    connector.setActiveProfileCompletable(account.ownerProfileId)
                } else {
                    Completable.complete()
                }
            }
        ).flatMapCompletable { it }
            // If profile was deleted before hook was called, currentProfileSingle returns empty
            // single, which will cause the whole stream to hang forever. We need to prevent that.
            .timeout(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS, timeoutScheduler)
            .onErrorResumeNext {
                accountFacade.getAccountSingle()
                    .flatMapCompletable { connector.setActiveProfileCompletable(it.ownerProfileId) }
                    .onErrorComplete()
            }
    }
}

internal const val COMPLETION_TIMEOUT_SECONDS = 1L
