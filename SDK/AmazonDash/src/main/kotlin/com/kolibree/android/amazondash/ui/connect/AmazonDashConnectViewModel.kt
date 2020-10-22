/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.ui.connect

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.kolibree.android.amazondash.R
import com.kolibree.android.amazondash.data.model.AmazonDashException
import com.kolibree.android.amazondash.domain.AmazonDashExtractTokenUseCase
import com.kolibree.android.amazondash.domain.AmazonDashLinkUseCase
import com.kolibree.android.amazondash.domain.AmazonDashSendTokenUseCase
import com.kolibree.android.amazondash.ui.AmazonDashAnalytics
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.network.api.ApiErrorCode
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime
import timber.log.Timber

internal class AmazonDashConnectViewModel(
    initialViewState: AmazonDashConnectViewState,
    private val navigator: AmazonDashConnectNavigator,
    private val linkUseCase: AmazonDashLinkUseCase,
    private val sendUseCase: AmazonDashSendTokenUseCase,
    private val extractTokenUseCase: AmazonDashExtractTokenUseCase
) : BaseViewModel<AmazonDashConnectViewState, AmazonDashConnectAction>(initialViewState) {

    val confirmationButtonEnabled = mapNonNull(
        viewStateLiveData,
        initialViewState.confirmationButtonEnabled
    ) { state ->
        state.confirmationButtonEnabled
    }

    val dismissButtonEnabled = mapNonNull(
        viewStateLiveData,
        initialViewState.dismissButtonEnabled
    ) { state ->
        state.dismissButtonEnabled
    }

    val dismissButtonVisible = mapNonNull(
        viewStateLiveData,
        initialViewState.dismissButtonVisible
    ) { state ->
        state.dismissButtonVisible
    }

    val congratulationsVisible = mapNonNull(
        viewStateLiveData,
        initialViewState.congratulationsVisible
    ) { state ->
        state.congratulationsVisible
    }

    val logo = mapNonNull(
        viewStateLiveData,
        initialViewState.logoRes
    ) { state ->
        state.logoRes
    }

    val title = mapNonNull(
        viewStateLiveData,
        initialViewState.titleRes
    ) { state ->
        state.titleRes
    }

    val body = mapNonNull(
        viewStateLiveData,
        initialViewState.bodyRes
    ) { state ->
        state.bodyRes
    }

    val description = mapNonNull(
        viewStateLiveData,
        initialViewState.description
    ) { state ->
        state.description
    }

    val loadingVisible = mapNonNull(
        viewStateLiveData,
        initialViewState.loadingVisible
    ) { state ->
        state.loadingVisible
    }

    val confirmationButton = mapNonNull(
        viewStateLiveData,
        R.string.amazon_dash_connect_confirm_button
    ) { state ->
        state.confirmButtonRes
    }

    fun onNewIntent(intent: Intent?) {
        val viewState = getViewState() ?: run {
            FailEarly.fail(message = "ViewState not available!")
            return
        }

        if (viewState.isLoading || viewState.isSuccess) {
            // We are already in progress or we are done
            return
        }

        disposeOnCleared { handleIntent(intent) }
    }

    fun onConfirmClick() {
        val viewState = getViewState() ?: run {
            FailEarly.fail(message = "ViewState not available!")
            return
        }

        if (viewState.isSuccess) {
            AmazonDashAnalytics.congratsNotNow()
            navigator.finish()
            return
        }

        AmazonDashAnalytics.allow()
        disposeOnStop(::loadLink)
    }

    fun onDismissClick() {
        AmazonDashAnalytics.notNow()
        navigator.finish()
    }

    private fun loadLink(): Disposable {
        return linkUseCase.getLink()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe { updateViewState { copy(isLoading = true) } }
            .doFinally { updateViewState { copy(isLoading = false) } }
            .subscribe(navigator::open, ::showError)
    }

    private fun handleIntent(intent: Intent?): Disposable {
        return extractTokenUseCase
            .extractTokenFrom(intent)
            .flatMapSingleElement(::sendToken)
            .doOnSubscribe { updateViewState { copy(isLoading = true) } }
            .doFinally { updateViewState { copy(isLoading = false) } }
            .subscribeOn(Schedulers.io())
            .subscribe(::showCongratulations, ::showError)
    }

    private fun sendToken(token: String): Single<OffsetDateTime> {
        return Single.defer {
            val requestTime = TrustedClock.getNowOffsetDateTime()
            sendUseCase.sendToken(token).toSingleDefault(requestTime)
        }
    }

    private fun showCongratulations(sendRequestTime: OffsetDateTime) {
        AmazonDashAnalytics.congrats()
        pushActionWhenResumed(AmazonDashConnectAction.SetResult(sendRequestTime))
        updateViewState { copy(isSuccess = true) }
    }

    private fun showError(exception: Throwable) {
        val error = when {
            exception is AmazonDashException -> exception.error
            exception.isAmazonError() -> Error.from(exception)
            else -> Error.from(R.string.amazon_dash_connect_error_unknown)
        }

        pushActionWhenResumed(AmazonDashConnectAction.ShowError(error))
        Timber.e(exception)
    }

    private fun Throwable.isAmazonError(): Boolean {
        return this is ApiError && AMAZON_ERRORS.contains(internalErrorCode)
    }

    class Factory @Inject constructor(
        private val navigator: AmazonDashConnectNavigator,
        private val linkUseCase: AmazonDashLinkUseCase,
        private val sendUseCase: AmazonDashSendTokenUseCase,
        private val extractTokenUseCase: AmazonDashExtractTokenUseCase
    ) : BaseViewModel.Factory<AmazonDashConnectViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AmazonDashConnectViewModel(
                viewState ?: AmazonDashConnectViewState.initial(),
                navigator,
                linkUseCase,
                sendUseCase,
                extractTokenUseCase
            ) as T
    }

    companion object {
        private val AMAZON_ERRORS = listOf(
            ApiErrorCode.AMAZON_DRS_AUTHENTICATION_FAILED,
            ApiErrorCode.AMAZON_DRS_UNABLE_TO_CONNECT
        )
    }
}
