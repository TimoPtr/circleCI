/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.usecases

import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.sdk.version.BaseVersion
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Compares persisted data for a given toothbrush vs the data coming from a real connection
 *
 * If they differ, it'll update the persisted data
 */
internal class PersistedToothbrushRefreshUseCase @Inject constructor(
    private val toothbrushRepository: ToothbrushRepository
) {
    /**
     * Runs by default on io [Scheduler]
     *
     * @return Completable that update the persisted versions associated to [connection] if they are
     * different from the ones reported by the toothbrush
     */
    fun maybeUpdateVersions(connection: KLTBConnection): Completable {
        return toothbrushRepository.getAccountToothbrush(connection.toothbrush().mac)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { accountToothbrush ->
                maybeUpdateCompletable(connection, accountToothbrush)
            }
    }

    private fun maybeUpdateCompletable(
        connection: KLTBConnection,
        accountToothbrush: AccountToothbrush
    ): Completable {
        return if (accountToothbrush.needsUpdate(connection)) {
            toothbrushRepository.associate(
                connection.toothbrush(),
                accountToothbrush.profileId,
                accountToothbrush.accountId
            )
                .andThen(toothbrushRepository.flagAsDirty(connection.toothbrush().mac))
        } else {
            Completable.complete()
        }
    }

    private fun AccountToothbrush.needsUpdate(connection: KLTBConnection): Boolean {
        val realFirmwareVersion = connection.toothbrush().firmwareVersion
        val realBootloaderVersion = connection.toothbrush().bootloaderVersion
        val realDspVersion = connection.toothbrush().dspVersion

        val realName = connection.toothbrush().getName()
        val realSerial = connection.toothbrush().serialNumber

        return firmwareVersion.needsUpdate(realFirmwareVersion) ||
            bootloaderVersion.needsUpdate(realBootloaderVersion) ||
            dspVersion.needsUpdate(realDspVersion) ||
            name != realName ||
            serial != realSerial
    }

    /**
     * @return true if [realVersion] is not [SoftwareVersion.NULL] or [DspVersion.NULL] and
     * it's different from the instance
     */
    private fun BaseVersion.needsUpdate(realVersion: BaseVersion): Boolean {
        return (realVersion != SoftwareVersion.NULL || realVersion != DspVersion.NULL) && !equals(
            realVersion
        )
    }
}
