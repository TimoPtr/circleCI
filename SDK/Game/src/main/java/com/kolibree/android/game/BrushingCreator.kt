/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.interactor.LifecycleAwareInteractor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@Keep
abstract class BrushingCreator : LifecycleAwareInteractor<BrushingCreator.Listener>() {
    /**
     * Public listener interface
     */
    @Keep
    interface Listener {
        /**
         * Notified on main thread
         */
        fun onSuccessfullySentData()

        /**
         * Notified on main thread
         */
        fun somethingWrong(error: Throwable)
    }

    abstract fun onBrushingCompleted(
        isManual: Boolean,
        connection: KLTBConnection?,
        brushingData: CreateBrushingData
    )

    abstract fun onBrushingCompletedCompletable(
        isManual: Boolean,
        connection: KLTBConnection?,
        brushingData: CreateBrushingData
    ): Completable
}

internal class BrushingCreatorImpl @VisibleForTesting constructor(
    private val connector: IKolibreeConnector,
    private val appVersions: KolibreeAppVersions,
    private val currentProfileProvider: CurrentProfileProvider,
    private val mainThreadHandler: Handler
) : BrushingCreator() {
    @Inject
    constructor(
        connector: IKolibreeConnector,
        appVersions: KolibreeAppVersions,
        currentProfileProvider: CurrentProfileProvider
    ) : this(
        connector = connector,
        appVersions = appVersions,
        currentProfileProvider = currentProfileProvider,
        mainThreadHandler = Handler(Looper.getMainLooper())
    )

    @VisibleForTesting
    val disposables = CompositeDisposable()

    override fun onDestroyInternal() {
        super.onDestroyInternal()
        disposables.clear()
    }

    override fun onBrushingCompletedCompletable(
        isManual: Boolean,
        connection: KLTBConnection?,
        brushingData: CreateBrushingData
    ): Completable = Completable.defer {
        addSupportData(brushingData, isManual, connection)

        when {
            isManual -> doSendDataAndNotifyListenersCompletable(
                connection = null,
                brushingData = brushingData
            )
            connection == null -> Completable.error(onUnsupportedNullConnection())
            else -> doSendDataAndNotifyListenersCompletable(
                connection = connection,
                brushingData = brushingData
            )
        }
    }

    override fun onBrushingCompleted(
        isManual: Boolean,
        connection: KLTBConnection?,
        brushingData: CreateBrushingData
    ) {
        addSupportData(brushingData, isManual, connection)

        when {
            isManual -> doSendData(connection = null, brushingData = brushingData)
            connection == null -> onUnsupportedNullConnection()
            else -> doSendData(connection = connection, brushingData = brushingData)
        }
    }

    private fun onUnsupportedNullConnection(): IllegalStateException {
        val exception = IllegalStateException(
            "Connection is null for non-manual brushing, cannot proceed with brushing creation!"
        )
        FailEarly.fail(exception = exception)

        notifySomethingWentWrong(exception)
        return exception
    }

    private fun addSupportData(
        brushingData: CreateBrushingData,
        isManual: Boolean,
        connection: KLTBConnection?
    ) {
        brushingData.addSupportData(
            if (isManual) null else connection?.toothbrush()?.serialNumber,
            if (isManual) null else connection?.toothbrush()?.mac,
            appVersions.appVersion,
            appVersions.buildVersion
        )
    }

    /**
     * After vibration stop the FW will keep recording for 20 seconds. After 20 seconds, it stops the
     * recording and discards the last 20 seconds. If we sent "monitor current brushing", it then
     * flushes the data.
     *
     *
     * If we complete a session and disconnect during a 20 seconds window, the FW stores the
     * brushing as offline even if we sent "monitor current", so we end up with duplicated brushings,
     * since we also created the brushing on the app side.
     *
     *
     * Stop vibration has a special mode to stop brushing + force recording stop. This behavior is
     * supported even if the toothbrush isn't vibrating.
     *
     *
     * We can successfully complete a brushing through 2 paths
     *
     *
     *  1. User brushes his goal time
     *  1. User pauses vibration and decides to quit
     *
     *
     *
     * Both success cases create a brushing, hence we need to make sure that we tell the toothbrush
     * to discard its local copy
     *
     *
     * See https://jira.kolibree.com/browse/KLTB002-5725
     */
    @VisibleForTesting
    fun forceOfflineBrushingStop(connection: KLTBConnection?): Completable =
        connection?.vibrator()?.offAndStopRecording() ?: Completable.complete()

    @VisibleForTesting
    fun doSendData(
        connection: KLTBConnection?,
        brushingData: CreateBrushingData
    ) {
        disposables += doSendDataCompletable(connection, brushingData)
            .subscribe(
                { notifySuccessfullySentData() },
                { error -> notifySomethingWentWrong(error) }
            )
    }

    private fun doSendDataAndNotifyListenersCompletable(
        connection: KLTBConnection?,
        brushingData: CreateBrushingData
    ): Completable =
        doSendDataCompletable(connection, brushingData)
            .doOnComplete { notifySuccessfullySentData() }
            .doOnError { error -> notifySomethingWentWrong(error) }

    private fun doSendDataCompletable(
        connection: KLTBConnection?,
        brushingData: CreateBrushingData
    ): Completable =
        currentProfileProvider.currentProfileSingle()
            .map { profile -> connector.withProfileId(profile.id) }
            .doOnSuccess { Timber.d("Pre assign createBrushing on $it with brushingData $brushingData") }
            .flatMap { profileWrapper -> profileWrapper.createBrushingSingle(brushingData) }
            .subscribeOn(Schedulers.io())
            .flatMapCompletable { forceOfflineBrushingStop(connection) }

    /*
    Queue notifications on main thread handler to avoid side effects inside rx streams
     */

    private fun notifySomethingWentWrong(error: Throwable) {
        forEachListener { listener -> mainThreadHandler.post { listener.somethingWrong(error) } }
    }

    private fun notifySuccessfullySentData() {
        forEachListener { mainThreadHandler.post { it.onSuccessfullySentData() } }
    }
}
