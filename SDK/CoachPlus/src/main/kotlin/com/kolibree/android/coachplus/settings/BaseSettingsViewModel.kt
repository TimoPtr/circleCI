/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettingsEntity
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.extensions.addSafely
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.sdk.e1.ToothbrushShutdownValve
import com.kolibree.sdkws.core.IKolibreeConnector
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@Keep
abstract class BaseSettingsViewModel<VS : Any>(
    private val settingsRepository: CoachSettingsRepository,
    private val connector: IKolibreeConnector,
    private val serviceInteractor: KolibreeServiceInteractor,
    private val e1ShutdownValve: ToothbrushShutdownValve
) : ViewModel(), DefaultLifecycleObserver {

    private val disposables = CompositeDisposable()
    private var settingsDisposable: Disposable? = null
    @VisibleForTesting
    var preventToothbrushShutdownDisposable: Disposable? = null

    protected var settings: CoachSettings =
        CoachSettingsEntity(profileId = connector.currentProfile!!.id)

    private val viewStateRelay = BehaviorRelay.create<VS>()
    protected lateinit var viewState: VS

    val viewStateObservable: Observable<VS> by lazy {
        viewStateRelay
            .startWith(initialViewState())
            .doOnNext { newViewState -> viewState = newViewState }
            .hide()
    }

    protected fun emitViewState(viewState: VS) {
        this.viewState = viewState
        viewStateRelay.accept(viewState)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    protected fun saveSettingsLocally() {
        disposables += settingsRepository.save(settings)
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    override fun onCreate(owner: LifecycleOwner) {
        serviceInteractor.setLifecycleOwner(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        readSettings()

        preventToothbrushShutdown()
    }

    @VisibleForTesting
    fun readSettings() {
        settingsDisposable.forceDispose()
        settingsDisposable =
            settingsRepository.getSettingsByProfileId(connector.currentProfile!!.id)
                .subscribeOn(Schedulers.io())
                .subscribe({ s ->
                    this.settings = s
                    emitViewState(
                        createViewStateFromSettings()
                    )
                }, Timber::e)

        disposables += settingsDisposable
    }

    abstract fun initialViewState(): VS

    abstract fun createViewStateFromSettings(): VS

    @VisibleForTesting
    fun preventToothbrushShutdown() {
        preventToothbrushShutdownDisposable.forceDispose()
        preventToothbrushShutdownDisposable = e1ShutdownValve.preventShutdownValve()
            .subscribe(
                {
                    // no-op
                },
                Timber::e
            )

        disposables.addSafely(preventToothbrushShutdownDisposable)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

        allowToothbrushShutdown()
    }

    @VisibleForTesting
    fun allowToothbrushShutdown() {
        preventToothbrushShutdownDisposable.forceDispose()
    }
}
