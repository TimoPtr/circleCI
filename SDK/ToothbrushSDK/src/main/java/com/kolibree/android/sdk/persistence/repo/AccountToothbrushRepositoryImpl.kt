package com.kolibree.android.sdk.persistence.repo

import androidx.annotation.WorkerThread
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.room.AccountToothbrushDao
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import timber.log.Timber

internal class AccountToothbrushRepositoryImpl
internal constructor(
    private val accountToothbrushDao: AccountToothbrushDao,
    private val accountDBScheduler: Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())
) : AccountToothbrushRepository {

    override fun readAccountToothbrush(mac: String): Flowable<AccountToothbrush> {
        return accountToothbrushDao.read(mac)
            .flatMap {
                if (it.isEmpty()) return@flatMap Flowable.empty<AccountToothbrush>()

                Flowable.just(it[0])
            }
    }

    override fun getToothbrushProfileId(mac: String): Single<Long> {
        return accountToothbrushDao.getToothbrushProfileId(mac)
    }

    override fun getAccountToothbrush(mac: String): Maybe<AccountToothbrush> {
        return accountToothbrushDao.read(mac)
            .firstElement()
            .flatMap {
                if (it.isEmpty()) return@flatMap Maybe.empty<AccountToothbrush>()

                Maybe.just(it[0])
            }
    }

    @WorkerThread
    override fun listAll() = accountToothbrushDao.listAll()

    override fun listAllStream() = accountToothbrushDao.listAllStream()

    override fun associate(
        toothbrush: Toothbrush,
        accountId: Long,
        profileId: Long
    ): Single<AccountToothbrush> {
        Timber.d("Associating %s to profile %s", toothbrush.getName(), profileId)
        return Single.fromCallable {
            val accountToothbrush = AccountToothbrush(
                accountId = accountId,
                profileId = profileId,
                mac = toothbrush.mac,
                name = toothbrush.getName(),
                firmwareVersion = toothbrush.firmwareVersion,
                hardwareVersion = toothbrush.hardwareVersion,
                bootloaderVersion = toothbrush.bootloaderVersion,
                dspVersion = toothbrush.dspVersion,
                model = toothbrush.model,
                serial = toothbrush.serialNumber
            )

            accountToothbrushDao.insert(accountToothbrush)

            return@fromCallable accountToothbrush
        }.subscribeOn(accountDBScheduler)
    }

    override fun getAccountToothbrushes(accountId: Long): Single<List<AccountToothbrush>> {
        return accountToothbrushDao.getAccountToothbrushes(accountId)
            .subscribeOn(accountDBScheduler)
            .defaultIfEmpty(emptyList())
            .toSingle()
    }

    override fun getAraCount(accountId: Long): Single<Int> {
        return getAccountToothbrushes(accountId).map { it.count { it.model == ARA } }
    }

    override fun addToothbrushes(toothbrushes: List<AccountToothbrush>): Single<Boolean> {
        return Single.just(accountToothbrushDao.insertAll(toothbrushes).sum() > 0L)
    }

    override fun isAssociated(accountId: Long, mac: String): Single<Boolean> =
        accountToothbrushDao.isAssociated(accountId, mac).subscribeOn(accountDBScheduler)

    override fun remove(mac: String): Completable = Completable.fromCallable {
        accountToothbrushDao.delete(mac)
    }.subscribeOn(accountDBScheduler)

    override fun rename(mac: String, name: String): Completable = Completable.fromCallable {
        accountToothbrushDao.rename(name, mac)
    }

    override fun deleteAll(): Completable = Completable.fromCallable {
        accountToothbrushDao.deleteAll()
    }

    override fun associate(toothbrush: AccountToothbrush, newAccountId: Long, newProfileId: Long): Completable {
        return Completable.fromCallable {
            val associatedToothbrush = toothbrush.copy(accountId = newAccountId, profileId = newProfileId)
            accountToothbrushDao.insert(associatedToothbrush)
        }.subscribeOn(accountDBScheduler)
    }
}
