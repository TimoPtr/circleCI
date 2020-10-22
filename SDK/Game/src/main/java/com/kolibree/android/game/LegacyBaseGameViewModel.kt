/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * Base class for ViewModels that need to determine if the active profile can use the active
 * toothbrush or not.
 *
 *
 * If the user can use the toothbrush, we store the data.
 *
 *
 * If the user can't use the toothbrush, we need to ask him who to assign the data to, and once
 * we know that, we can proceed with the data saving.
 *
 *
 * Created by miguelaragues on 16/10/17.
 */
@Keep
@Deprecated("Not compliant to MVI implementation", replaceWith = ReplaceWith("BaseGameViewModel"))
abstract class LegacyBaseGameViewModel<T : GameViewState<T>> protected constructor(
    protected val connector: IKolibreeConnector,
    private val connectionProvider: KLTBConnectionProvider,
    toothbrushMacSingle: Single<String>?,
    protected var appVersions: KolibreeAppVersions,
    private val brushingCreator: BrushingCreator
) : ViewModel() {
    protected val disposables = CompositeDisposable()
    protected val viewStateRelay = PublishRelay.create<T>()

    /**
     * A unique Observable that will emit GameViewState
     *
     *
     * Since Actions are a one-time event, we want to clear it each time it's consumed
     */
    private val viewStateObservable: Observable<T>

    var toothbrushMac: String? = null
        @VisibleForTesting set

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var connection: KLTBConnection? = null

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    var viewState: T
    var brushingData: CreateBrushingData? = null
    var brushingPoints: Int = 0
        private set
    private var toothbrushMacSingle: Single<String>? = null

    val isManual: Boolean
        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        get() = toothbrushMacSingle == null

    init {
        if (toothbrushMacSingle != null) {
            this.toothbrushMacSingle = toothbrushMacSingle
        }
        viewState = initialViewState()
        viewStateObservable = viewStateRelay
            .startWith(initialViewState())
            .doOnSubscribe { init() }
            .doAfterNext { viewStateSent ->
                if (viewStateSent.actionId != GameViewState.ACTION_NONE) {
                    viewState = viewStateSent.withActionId(GameViewState.ACTION_NONE)
                }
            }
            .publish()
            .autoConnect()
    }

    override fun onCleared() {
        super.onCleared()

        disposables.dispose()
    }

    protected fun compositeDisposable(): CompositeDisposable = disposables

    @VisibleForTesting
    fun init() {
        if (!isManual) {
            disposables +=
                toothbrushMacSingle!!
                    .subscribeOn(Schedulers.io())
                    .flatMap { toothbrushMac ->
                        this@LegacyBaseGameViewModel.toothbrushMac = toothbrushMac

                        connectionProvider.existingActiveConnection(toothbrushMac)
                    }
                    .subscribe(
                        { connection ->
                            this@LegacyBaseGameViewModel.connection = connection
                            onConnection()
                        },
                        { throwable ->
                            Timber.e(throwable)

                            viewStateRelay.accept(
                                viewState.withActionId(GameViewState.ACTION_ERROR_SOMETHING_WENT_WRONG)
                            )
                        })
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    open fun onConnection() {
        // Default implementation does nothing
    }

    fun viewStateObservable(): Observable<T> = viewStateObservable

    fun onBrushingCompleted(brushingData: CreateBrushingData, brushingPoints: Int) {
        disposables += brushingCreator.onBrushingCompletedCompletable(
            isManual,
            connection,
            brushingData
        )
            .andThen(onSuccessfullySentDataCompletable())
            .subscribe(
                {
                    // no-op
                },
                Timber::e
            )
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun emitSomethingWentWrong() {
        viewStateRelay.accept(viewState.withActionId(GameViewState.ACTION_ERROR_SOMETHING_WENT_WRONG))
    }

    private fun onSuccessfullySentDataCompletable(): Completable {
        return beforeSendDataSavedCompletable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                viewStateRelay.accept(
                    viewState.withActionId(GameViewState.ACTION_ON_DATA_SAVED)
                )
            }
    }

    /**
     * Children can override this method if they want to take action before we confirm the data is
     * saved.
     *
     *
     * At this point in time, user has selected the profile for the session
     */
    open fun beforeSendDataSavedCompletable(): Completable = Completable.complete()

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun initialViewState(): T

    @Keep
    abstract class InternalFactory protected constructor(
        protected val connector: IKolibreeConnector,
        protected val connectionProvider: KLTBConnectionProvider,
        protected val toothbrushMac: Single<String>?,
        protected val appVersions: KolibreeAppVersions,
        protected val brushingCreator: BrushingCreator
    ) : ViewModelProvider.Factory {

        @Keep
        abstract class InternalBuilder<T : InternalFactory, B : InternalBuilder<T, B>> protected constructor(
            protected val connector: IKolibreeConnector,
            protected val connectionProvider: KLTBConnectionProvider,
            protected val appVersions: KolibreeAppVersions,
            protected val brushingCreator: BrushingCreator
        ) {
            protected var toothbrushMacSingle: Single<String>? = null

            fun withToothbrushMac(toothbrushMacSingle: Single<String>?): B {
                this.toothbrushMacSingle = toothbrushMacSingle

                return this as B
            }

            protected abstract fun build(): T
        }
    }
}
