/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import android.annotation.SuppressLint
import com.kolibree.android.KolibreeExperimental
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.accountinternal.profile.models.AccountAndProfileIds
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.util.NoSuchElementException
import javax.inject.Inject

@KolibreeExperimental
@VisibleForApp
interface CurrentAccountAndProfileIdsProvider {

    /**
     * @return a Flowable that emits current account and profile IDs, if they're available.
     */
    fun currentAccountAndProfileIdsStream(): Flowable<AccountAndProfileIds>

    /**
     * @return a Single that will emit the current account and profile IDs.
     * If there's none, it'll wait until it's available.
     * @throws [NoSuchElementException] if there is no account or profile available and source
     * streams completed.
     */
    fun currentAccountAndProfileIdsSingle(): Single<AccountAndProfileIds>
}

@KolibreeExperimental
internal class CurrentAccountAndProfileIdsProviderImpl @Inject constructor(
    private val accountDatastore: AccountDatastore,
    private val currentProfileProvider: CurrentProfileProvider
) : CurrentAccountAndProfileIdsProvider {

    @SuppressLint("ExperimentalClassUse")
    override fun currentAccountAndProfileIdsStream(): Flowable<AccountAndProfileIds> {
        return Flowable.combineLatest<AccountInternal, Profile, AccountAndProfileIds>(
            accountDatastore.accountFlowable(),
            currentProfileProvider.currentProfileFlowable(),
            BiFunction<AccountInternal, Profile, AccountAndProfileIds> { account, profile ->
                AccountAndProfileIds(account.id, profile.id)
            }
        ).distinctUntilChanged()
    }

    override fun currentAccountAndProfileIdsSingle(): Single<AccountAndProfileIds> =
        currentAccountAndProfileIdsStream()
            .take(1)
            .singleOrError()
}
