package com.kolibree.android.sdk.persistence.repo

import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 *
 */
@Deprecated(
    "Favor use of ToothbrushRepository. See https://jira.kolibree.com/browse/KLTB002-3812",
    replaceWith = ReplaceWith("ToothbrushRepository")
)
@Keep
interface AccountToothbrushRepository {
    @WorkerThread
    fun listAll(): List<AccountToothbrush>

    /**
     * @return Flowable that will emit all AccountToothbrush in the database on subscription, and then
     *     every time there's a database change
     */
    fun listAllStream(): Flowable<List<AccountToothbrush>>

    fun getAccountToothbrushes(accountId: Long): Single<List<AccountToothbrush>>

    fun getAraCount(accountId: Long): Single<Int>

    fun isAssociated(accountId: Long, mac: String): Single<Boolean>

    fun remove(mac: String): Completable

    fun rename(mac: String, name: String): Completable

    fun deleteAll(): Completable

    fun addToothbrushes(toothbrushes: List<AccountToothbrush>): Single<Boolean>

    fun associate(
        toothbrush: Toothbrush,
        accountId: Long,
        profileId: Long
    ): Single<AccountToothbrush>

    fun getAccountToothbrush(mac: String): Maybe<AccountToothbrush>

    fun readAccountToothbrush(mac: String): Flowable<AccountToothbrush>

    fun getToothbrushProfileId(mac: String): Single<Long>

    fun associate(toothbrush: AccountToothbrush, newAccountId: Long, newProfileId: Long): Completable
}
