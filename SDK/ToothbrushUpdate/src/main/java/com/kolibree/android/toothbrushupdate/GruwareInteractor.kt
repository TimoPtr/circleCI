/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.toothbrushupdate

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.errors.NetworkNotAvailableException
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.sdkws.core.GruwareRepository
import com.kolibree.sdkws.data.model.GruwareData
import com.kolibree.sdkws.data.model.GruwareData.Companion.empty
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import timber.log.Timber

/**
 * Responsible for returning GruwareData for a given KLTBConnection
 *
 *
 * Created by miguelaragues on 4/5/18.
 */
@Keep
internal class GruwareInteractor @Inject constructor(
    private val networkChecker: NetworkChecker,
    private val gruwareDataStore: GruwareDataStore,
    private val gruwareRepository: GruwareRepository
) {

    /**
     * Returns a deferred Single that
     *
     *
     * - If there's a locally cached, non-empty GruwareData, it'll return it immediately
     *
     *
     * - If there's no locally cached GruwareData and there's no internet, it'll emit a
     * ConnectivityException
     *
     *
     * - If there's internet and there's no locally cached GruwareData, it'll request a GruwareData
     * for the KLTBConnection
     *
     *
     * - Emits IOException if there's an error while attempting to download the files
     */
    @Suppress("LongMethod")
    fun getGruware(connection: KLTBConnection): Single<GruwareData> {
        if (gruwareDataStore.containsGruwareFor(connection)) {
            return Single.just(gruwareDataStore.gruwareFor(connection))
        }
        if (!networkChecker.hasConnectivity()) {
            return Single.error(NetworkNotAvailableException())
        }
        val toothbrush = connection.toothbrush()
        return gruwareRepository
            .getGruwareInfo(
                toothbrush.model.internalName.toLowerCase(Locale.getDefault()),
                toothbrush.hardwareVersion.toString(),
                toothbrush.serialNumber,
                toothbrush.firmwareVersion.toString()
            )
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { throwable: Throwable ->
                if (throwable is IOException) {
                    Timber.e(throwable)
                    return@onErrorResumeNext Single.just(empty())
                }
                Single.error(throwable)
            }
            .map { gruwareData: GruwareData ->
                gruwareData.apply { validate() }
            }
            .doOnSuccess { gruwareData: GruwareData ->
                onNewGruwareData(
                    connection,
                    gruwareData
                )
            }
    }

    @VisibleForTesting
    fun onNewGruwareData(connection: KLTBConnection, gruwareData: GruwareData) {
        if (gruwareData.isNotEmpty()) gruwareDataStore.saveGruware(connection, gruwareData)
    }
}
