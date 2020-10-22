/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.inprogress

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.ota.OtaUpdateNavigator
import com.kolibree.android.app.ui.ota.OtaUpdateParams
import com.kolibree.android.app.ui.ota.OtaUpdateSharedViewModel
import com.kolibree.android.app.ui.ota.OtaUpdater
import com.kolibree.android.app.ui.ota.R
import com.kolibree.android.otaTagFor
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.UnknownToothbrushException
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class InProgressOtaViewModel(
    initialViewState: InProgressOtaViewState?,
    private val sharedViewModel: OtaUpdateSharedViewModel,
    private val navigator: OtaUpdateNavigator,
    private val otaUpdateParams: OtaUpdateParams,
    private val serviceProvider: ServiceProvider,
    private val otaUpdater: OtaUpdater
) : BaseViewModel<InProgressOtaViewState, InProgressOtaActions>(
    initialViewState ?: InProgressOtaViewState.initial()
), OtaUpdateSharedViewModel by sharedViewModel {

    val progress: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.progress ?: 0
    }

    val showResult: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.showResult() == true
    }

    val isOtaFailed: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.isOtaFailed == true
    }

    val resultIcon: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.resultIcon() ?: R.drawable.ic_ota_done
    }

    val title: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.title() ?: R.string.in_progress_ota_title
    }

    val content: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.content() ?: R.string.in_progress_ota_content
    }

    fun onDoneClick() {
        if (getViewState()?.isOtaSuccess == true) {
            InProgressOtaAnalytics.done()
        } else {
            InProgressOtaAnalytics.fail()
        }
        navigator.finishScreen()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        var savedConnection: KLTBConnection? = null
        disposeOnDestroy {
            serviceProvider.connectOnce()
                .subscribeOn(Schedulers.io())
                .getConnection()
                .flatMapObservable { connection ->
                    savedConnection = connection
                    otaUpdater.updateToothbrushObservable(connection)
                }
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onOtaUpdateEvent, ::onOtaFailure) {
                    onOtaComplete(savedConnection)
                }
        }
    }

    private fun Single<KolibreeService>.getConnection(): Single<KLTBConnection> =
        map {
            it.getConnection(otaUpdateParams.mac) ?: throw UnknownToothbrushException(
                otaUpdateParams.mac
            )
        }

    private fun onOtaUpdateEvent(event: OtaUpdateEvent) {
        Timber.tag(TAG).d("Event = $event")
        /*
            errorMessageId from the event will always be null it was use in the previous impl
            error will end up in onError block
        */
        event.progress?.let {
            /*
                if one day we need more information about the ota just use the information
                from the event
            */

            updateViewState {
                /* FIXME https://kolibree.atlassian.net/browse/KLTB002-11662
                To reproduce the issue with OTA, set isOtaDone to true when progress = 100,
                and click on done button asap and remove the update in onComplete
                 */
                copy(progress = it)
            }
        }
    }

    private fun onOtaFailure(error: Throwable) {
        // TODO handle error and maybe show failure screen
        // https://kolibree.atlassian.net/browse/KLTB002-11370
        Timber.tag(TAG).e(error)
        updateViewState {
            copy(isOtaFailed = true)
        }
    }

    private fun onOtaComplete(connection: KLTBConnection?) {
        connection?.tag = null
        updateViewState {
            copy(progress = 100, isOtaSuccess = true)
        }
    }

    class Factory @Inject constructor(
        private val sharedViewModel: OtaUpdateSharedViewModel,
        private val navigator: OtaUpdateNavigator,
        private val otaUpdateParams: OtaUpdateParams,
        private val serviceProvider: ServiceProvider,
        private val otaUpdater: OtaUpdater
    ) : BaseViewModel.Factory<InProgressOtaViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            InProgressOtaViewModel(
                viewState,
                sharedViewModel,
                navigator,
                otaUpdateParams,
                serviceProvider,
                otaUpdater
            ) as T
    }
}

private val TAG = otaTagFor(InProgressOtaViewModel::class.java)
