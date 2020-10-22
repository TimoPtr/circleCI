package com.kolibree.android.sdk.persistence.repo

import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.room.AccountToothbrushDao
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

/**
 * Class responsible for the persistence in the SDK and the app of the Toothbrush connections
 *
 * The goal is to hide the SDK persistence from the rest of the application
 *
 * Created by miguelaragues on 29/9/17.
 */
internal class ToothbrushRepositoryImpl @Inject constructor(
    private val accountToothbrushRepository: AccountToothbrushRepository,
    private val accountToothbrushDao: AccountToothbrushDao
) : ToothbrushRepository {

    /**
     * Persists {@param toothbrush} to Android-SDK and associates it to an {@param accountId}
     *
     * @param toothbrush non null [Toothbrush]
     * @param accountId long accountId
     */
    override fun associate(
        toothbrush: Toothbrush,
        profileId: Long,
        accountId: Long
    ): Completable {
        return accountToothbrushRepository.associate(toothbrush, accountId, profileId)
            .ignoreElement()
    }

    override fun getAccountToothbrushes(accountId: Long): List<AccountToothbrush> {
        return accountToothbrushRepository.getAccountToothbrushes(accountId).blockingGet()
    }

    override fun removeAccountToothbrush(accountToothbrush: AccountToothbrush?): Completable {
        return if (accountToothbrush == null) {
            Completable.complete()
        } else accountToothbrushRepository.remove(
            accountToothbrush.mac
        )
    }

    override fun getAraCount(accountId: Long): Int {
        return accountToothbrushRepository.getAraCount(accountId).blockingGet()
    }

    override fun isAssociated(mac: String, accountId: Long): Boolean {
        return accountToothbrushRepository.isAssociated(accountId, mac).blockingGet()
    }

    override fun listAll(): Single<List<AccountToothbrush>> {
        return Single.defer { Single.just(accountToothbrushRepository.listAll()) }
    }

    override fun listAllOnceAndStream(): Flowable<List<AccountToothbrush>> {
        return accountToothbrushRepository.listAllStream()
    }

    override fun listAllWithProfileIdOnceAndStream(profileId: Long): Flowable<List<AccountToothbrush>> {
        return accountToothbrushDao.listAllWithProfileIdStream(profileId)
    }

    override fun remove(mac: String): Completable {
        return accountToothbrushRepository.remove(mac)
    }

    override fun getAccountToothbrush(mac: String): Maybe<AccountToothbrush> {
        return accountToothbrushRepository.getAccountToothbrush(mac)
    }

    override fun readAccountToothbrush(mac: String): Flowable<AccountToothbrush> {
        return accountToothbrushRepository.readAccountToothbrush(mac)
    }

    override fun addToothbrushes(toothbrushes: List<AccountToothbrush>): Single<Boolean> {
        return accountToothbrushRepository.addToothbrushes(toothbrushes)
    }

    override fun rename(mac: String, name: String): Completable {
        return accountToothbrushRepository.rename(mac, name)
    }

    override fun truncate(): Completable {
        return accountToothbrushRepository.deleteAll()
    }

    override fun associate(
        toothbrush: AccountToothbrush,
        newAccountId: Long,
        newProfileId: Long
    ): Completable {
        return accountToothbrushRepository.associate(toothbrush, newAccountId, newProfileId)
    }

    override fun flagAsDirty(mac: String): Completable = flagAsDirty(mac, isDirty = true)

    override fun cleanDirty(mac: String): Completable = flagAsDirty(mac, isDirty = false)

    private fun flagAsDirty(mac: String, isDirty: Boolean): Completable {
        return getAccountToothbrush(mac)
            .flatMapCompletable { accountToothbrush ->
                Completable.fromAction { accountToothbrushDao.insert(accountToothbrush.copy(dirty = isDirty)) }
            }
    }
}
