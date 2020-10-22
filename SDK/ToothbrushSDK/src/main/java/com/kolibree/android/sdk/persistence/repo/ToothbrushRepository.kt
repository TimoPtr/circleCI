package com.kolibree.android.sdk.persistence.repo

import androidx.annotation.Keep
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

/** Created by miguelaragues on 29/9/17.  */
@Keep
interface ToothbrushRepository : Truncable {
    fun listAll(): Single<List<AccountToothbrush>>

    /**
     * @return Flowable that will emit all AccountToothbrush in the database on subscription, and then
     * every time there's a database change
     */
    fun listAllOnceAndStream(): Flowable<List<AccountToothbrush>>

    /**
     * @return Flowable that will emit all AccountToothbrush associated to profileId on subscription,
     * and then every time there's a database change.
     *
     * It will *NOT* include shared toothbrushes
     * unless the `profileId` is [KolibreeConst.SHARED_MODE_PROFILE_ID]
     */
    fun listAllWithProfileIdOnceAndStream(profileId: Long): Flowable<List<AccountToothbrush>>

    /**
     * Associate a toothbrush to an account
     *
     * @param toothbrush non null [Toothbrush]
     * @param accountId long accountId
     */
    fun associate(toothbrush: Toothbrush, profileId: Long, accountId: Long): Completable

    /**
     * Get all known toothbrushes
     *
     * @return non null [AccountToothbrush] list
     */
    fun getAccountToothbrushes(accountId: Long): List<AccountToothbrush>

    /**
     * Removes the AccountToothbrush from the data layer
     *
     * @param accountToothbrush the accountToothbrush to be removed. If it's null, it'll be ignored
     */
    fun removeAccountToothbrush(accountToothbrush: AccountToothbrush?): Completable

    /**
     * Get how many Aras are owned by an account
     *
     * @param accountId long account ID
     * @return Ara toothbrushes count
     */
    fun getAraCount(accountId: Long): Int

    /**
     * Check if a toothbrush is associated to an account
     *
     * @param mac non null [String] MAC address
     * @param accountId long account ID
     * @return true if associated, false otherwise
     */
    fun isAssociated(mac: String, accountId: Long): Boolean

    /**
     * Rename Ara toothbrush in database
     *
     * @param mac non null MAC address
     * @param name non null name
     */
    fun rename(mac: String, name: String): Completable

    /**
     * Remove a known toothbrush from table
     *
     * @param mac non null MAC address
     */
    fun remove(mac: String): Completable
    fun getAccountToothbrush(mac: String): Maybe<AccountToothbrush>
    fun readAccountToothbrush(mac: String): Flowable<AccountToothbrush>
    fun addToothbrushes(toothbrushes: List<AccountToothbrush>): Single<Boolean>
    fun associate(
        toothbrush: AccountToothbrush,
        newAccountId: Long,
        newProfileId: Long
    ): Completable

    fun flagAsDirty(mac: String): Completable
    fun cleanDirty(mac: String): Completable
}
