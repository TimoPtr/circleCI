/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal.persistence.repo

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.dao.AccountDao
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@SuppressWarnings("TooManyFunctions")
internal class AccountDatastoreImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val profileDatastore: ProfileDatastore
) : AccountDatastore {
    override fun accountFlowable(): Flowable<AccountInternal> {
        return Flowable.combineLatest(
            accountDao.getAccountFlowable(),
            profileDatastore.profilesFlowable(),
            BiFunction<AccountInternal, List<ProfileInternal>, AccountInternal> { account, profiles ->
                account.internalProfiles = profiles

                account
            }
        )
    }

    override fun getAccountMaybe(): Maybe<AccountInternal> {
        return Maybe.zip(
            accountDao.getAccountMaybe(),
            profileDatastore.getProfiles().toMaybe(),
            BiFunction<AccountInternal, List<ProfileInternal>, AccountInternal> { account, profiles ->
                account.internalProfiles = profiles

                account
            }
        ).doOnError(Timber::e)
    }

    override fun truncate() {
        accountDao.truncate()
        profileDatastore.deleteAll().subscribeOn(Schedulers.io()).blockingAwait()
    }

    /**
     * Stores the unique account in the system
     *
     * Assumes internalProfiles, if any, have been set
     *
     * If a previous account existed, it'll be removed
     */
    override fun setAccount(newAccount: AccountInternal) {
        try {
            Completable.fromAction {
                val storedAccount: AccountInternal? = getAccountMaybe().blockingGet()
                if (newAccount != storedAccount || storedAccount.currentProfileId == null) {
                    if (isDifferentUser(storedAccount, newAccount)) {
                        truncate()
                    }

                    storedAccount?.let { newAccount.isAllowDataCollecting = it.isAllowDataCollecting }

                    profileDatastore.addProfiles(newAccount.internalProfiles)

                    sanitizeCurrentProfileId(newAccount, storedAccount)

                    accountDao.insert(newAccount)
                }
            }.subscribeOn(Schedulers.io()).blockingAwait()
        } catch (e: RuntimeException) {
            Timber.e(e)
        }
    }

    private fun isDifferentUser(
        storedAccount: AccountInternal?,
        newAccount: AccountInternal
    ) = storedAccount != null && storedAccount.id != newAccount.id

    private fun sanitizeCurrentProfileId(
        updatedAccount: AccountInternal,
        storedAccount: AccountInternal? = null
    ) {
        if (updatedAccount.currentProfileId == null) {
            if (isDifferentUser(storedAccount, updatedAccount)) {
                updatedAccount.setOwnerProfileAsCurrent()
            } else {
                storedAccount?.currentProfileId?.let { storedCurrentProfileId ->
                    updatedAccount.currentProfileId = storedCurrentProfileId
                } ?: updatedAccount.setOwnerProfileAsCurrent()
            }
        }
    }

    override fun updateCurrentProfileId(account: AccountInternal) {
        sanitizeCurrentProfileId(account)

        accountDao.updateCurrentProfileId(account.currentProfileId)
    }

    override fun updateTokens(account: AccountInternal) {
        accountDao.updateTokens(
            accessToken = account.accessToken,
            refreshToken = account.refreshToken
        )
    }

    override fun updateEmail(account: AccountInternal) {
        accountDao.updateEmail(account.email)
    }

    override fun updateFacebookId(account: AccountInternal) {
        accountDao.updateFacebookId(account.facebookId)
    }

    override fun setUpdateAllowDataCollecting(account: AccountInternal) {
        accountDao.updateAllowDataCollecting(account.isAllowDataCollecting)
    }

    override fun updateAllowDigest(account: AccountInternal) {
        accountDao.updateAllowDigest(account.isDigestEnabled)
    }

    override fun updatePhoneNumber(account: AccountInternal) =
        accountDao.updatePhoneNumber(account.phoneNumber)

    override fun updateWeChatData(account: AccountInternal) =
        accountDao.updateWeChatData(
            account.wcOpenId,
            account.wcUnionId,
            account.wcAccessToken,
            account.wcRefreshToken,
            account.wcExpiresIn,
            account.wcScope
        )

    override fun updateNewsletterSubscription(isNewsletterSubscriptionOn: Boolean) =
        accountDao.updateNewsletterSubscription(isNewsletterSubscriptionOn)
}
