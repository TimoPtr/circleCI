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
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * After a profile is deleted, we want to check whether any of the toothbrushes is in shared mode.
 *
 * If there's a single profile left and any toothbrush is in shared mode, we will set the profile
 * left as owner
 */
internal class ReassignSharedToothbrushHook
@Inject constructor(
    private val accountFacade: AccountFacade,
    private val serviceProvider: ServiceProvider,
    private val toothbrushRepository: ToothbrushRepository
) : ProfileDeletedHook {
    override fun onProfileDeleted(profileId: Long): Completable {
        return accountFacade.getAccountSingle()
            .flatMapCompletable { account ->
                if (shouldSetConnectionsSingleOwner(account)) {
                    setConnectionsOwnerCompletable(account.ownerProfileId, account.backendId)
                } else {
                    Completable.complete()
                }
            }
    }

    private fun shouldSetConnectionsSingleOwner(account: Account) = account.profiles.size == 1

    private fun setConnectionsOwnerCompletable(profileId: Long, accountId: Long): Completable {
        return serviceProvider.connectOnce()
            .flatMapObservable { Observable.fromIterable(it.knownConnections) }
            .flatMapCompletable { connection ->
                connection.userMode().isSharedModeEnabled()
                    .flatMapCompletable { isShared ->
                        if (isShared) {
                            connection.userMode().setProfileId(profileId)
                                .andThen(
                                    toothbrushRepository.associate(
                                        connection.toothbrush(),
                                        profileId,
                                        accountId
                                    )
                                )
                        } else {
                            Completable.complete()
                        }
                    }
            }
    }
}
