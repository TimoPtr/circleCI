/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.base

import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.utils.DisposableScope
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers
import javax.inject.Provider
import kotlinx.android.parcel.Parcelize
import leakcanary.AppWatcher
import timber.log.Timber

/**
 * Should keep the status to the view with simple type like strings, boolean, numbers
 * This state will be restored by the activity if needed so it need to be Parcelable
 */
@Keep
interface BaseViewState : Parcelable

/**
 * If you don't have viewState just use this object to inherit from the BaseViewModel
 */
@Parcelize
@Keep
object EmptyBaseViewState : BaseViewState

/**
 * Where you need to ask something to the user
 * One shot after emit the Action it will not be available
 * For instance if you need to display a popup to the user send an action,
 * but when the activity will be put in background
 * the dialog will not be prompt again (if you want to do this you might need to use viewState instead
 * which survive)
 */
@Keep
interface BaseAction

/**
 * Indicating that particular view model won't send any actions.
 */
@Keep
object NoActions : BaseAction

/**
 * This is a base for all ViewModels which contain some boilerplate code.
 * It helps us to implement the MVI pattern and can be used with databinding.
 *
 * If you use BaseViewState and you don't have viewState just use EmptyBaseViewState
 */
@Keep
abstract class BaseViewModel<VS : BaseViewState, A : BaseAction>(
    baseViewState: VS,
    /**
     * Provides the first BaseAction to emit to subscribers of [actionsObservable]
     *
     * It's the descendants responsibility to control whether this should be emitted only to first
     * subscriber or also to future subscribers
     *
     * Check https://github.com/kolibree-git/android-monorepo/pull/295#discussion_r334369306 for
     * an example on what can happen on activity recreation
     */
    initialAction: Provider<A?> = Provider { null },
    /**
     * A set of children VMs, composed within this view model. Children VMs can hold different type
     * of view state than [VS] but the type of their action needs to be [A] or its descendant.
     *
     * Empty by default.
     */
    val children: Set<BaseViewModel<*, out A>> = emptySet()
) : ViewModel(), DefaultLifecycleObserver {

    /**
     * Disposable scope that match the lifecycle of the ViewModel
     * you can add disposable to it after the creation of the ViewModel.
     *
     * Cleared in [onCleared] method.
     */
    @VisibleForTesting
    val onClearedDisposables = DisposableScope("onCleared")

    /**
     * Disposable scope that is available after onCreate has been called.
     *
     * Cleared in [onDestroy] method.
     */
    @VisibleForTesting
    val onDestroyDisposables = DisposableScope("onDestroy")

    /**
     * Disposable scope that is available after onStart has been called.
     *
     * Cleared in [onStop] method.
     */
    @VisibleForTesting
    val onStopDisposables = DisposableScope("onStop")

    /**
     * Disposable scope that is available after onResume has been called.
     *
     * Cleared in [onPause] method.
     */
    @VisibleForTesting
    val onPauseDisposables = DisposableScope("onPause")

    /**
     * This Processor is used to keep track of the ViewState, when you want to
     * update the view you should update the ViewState. ViewState should be an immutable object so you should copy
     * the previous one to create a new one and push it to this Subject.
     * To do that you can use the extension function acceptWithPreviousValue
     */
    private val viewStateProcessor = BehaviorProcessor.createDefault<VS>(baseViewState)

    /**
     * This Flowable expose the ViewState and it can be used as a source for LiveData.
     * It will be automatically dispose in onCleared. It operate on it's own thread.
     *
     * TODO maybe add a debounce operator to avoid triggering a redraw of the view too often
     */
    val viewStateFlowable: Flowable<VS> = viewStateProcessor
        .distinctUntilChanged()
        .subscribeOn(Schedulers.single())
        .replay(1)
        .autoConnect(1) {
            onClearedDisposables += it
        }
        .hide()

    /**
     * Same as viewStateFlowable but with LiveData
     */
    protected val viewStateLiveData =
        LiveDataReactiveStreams.fromPublisher(viewStateFlowable.observeOn(AndroidSchedulers.mainThread()))

    /**
     * This Subject is used to send one shot action to the View. So for instance if you want to
     * display a Dialog to the user and you don't care about the configuration change use this Subject.
     */
    private val actionsRelay: PublishRelay<A> = PublishRelay.create()

    /**
     * @return [Observable]<[A]> that will emit one-shot actions to subscribers. If  [_initialAction]
     * is not null, the first subscriber will receive it. Future subscribers will not.
     */
    val actionsObservable: Observable<A> = actionsRelay
        .startWith(Observable.defer<A> {
            val action = initialAction.get()
            if (action != null) {
                Observable.just(action)
            } else {
                Observable.empty<A>()
            }
        })
        .hide()

    /**
     * Holds all actions that should be pushed right after [onResume]
     */
    private val enqueuedActions = mutableListOf<A>()

    /**
     * Initializes [onClearedDisposables]
     */
    init {
        onClearedDisposables.ready()
    }

    /**
     * Initializes [onDestroyDisposables]
     */
    @CallSuper
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Timber.tag(TAG_LIFECYCLE).v("%s - onCreate", javaClass.simpleName)
        onDestroyDisposables.ready()
    }

    /**
     * Initializes [onStopDisposables]
     */
    @CallSuper
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.tag(TAG_LIFECYCLE).v("%s - onStart", javaClass.simpleName)
        onStopDisposables.ready()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Timber.tag(TAG_LIFECYCLE).v("%s - onResume", javaClass.simpleName)
        onPauseDisposables.ready()
        enqueuedActions.apply {
            forEach(::pushAction)
            clear()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        Timber.tag(TAG_LIFECYCLE).v("%s - onPause", javaClass.simpleName)
        super.onPause(owner)
        onPauseDisposables.clear()
    }

    /**
     * Clears [onStopDisposables]
     */
    @CallSuper
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.tag(TAG_LIFECYCLE).v("%s - onStop", javaClass.simpleName)
        onStopDisposables.clear()
    }

    /**
     * Clears [onDestroyDisposables]
     */
    @CallSuper
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Timber.tag(TAG_LIFECYCLE).v("%s - onDestroy", javaClass.simpleName)
        onDestroyDisposables.clear()
    }

    /**
     * Disposes [onClearedDisposables], [onStopDisposables] and [onDestroyDisposables]
     */
    @CallSuper
    override fun onCleared() {
        super.onCleared()
        Timber.tag(TAG_LIFECYCLE).v("%s - onCleared", javaClass.simpleName)

        onClearedDisposables.dispose()
        onPauseDisposables.dispose()
        onStopDisposables.dispose()
        onDestroyDisposables.dispose()

        if (AppWatcher.isInstalled) {
            AppWatcher.objectWatcher.watch(this, javaClass.simpleName)
        }
    }

    /**
     * You are responsible to update the viewState by calling this method he gets the previous
     * viewState and he need to provide a new one. So basically you need to use the copy method
     * from the data class and update the field you want instead of creating a new item from
     * scratch otherwise you will probably break something
     *
     * @param block the lambda that it an extension of the old viewState and return the new ViewState
     */
    @Synchronized
    @VisibleForTesting(otherwise = PROTECTED)
    fun updateViewState(block: VS.() -> VS) {
        viewStateProcessor.value?.let {
            viewStateProcessor.onNext(block(it))
        } ?: FailEarly.fail("BehaviorRelay should have been initialized")
    }

    /**
     * Push an action to a View. If none view subscribe to the actionsObservable it will just be ignore.
     *
     * Note: If your VM has [A] type of [NoActions], calling this is a no-op operation.
     *
     * @param action the action to send
     */
    protected fun pushAction(action: A) {
        when {
            action is NoActions -> FailEarly.fail(
                "NoActions was designed to indicate that no action will be pushed " +
                    "from this VM. pushAction(NoActions) is a no-op operation."
            )
            !actionsRelay.hasObservers() -> FailEarly.fail(
                "actionsRelay doesn't have observers - this is probably because you tried to call " +
                    "pushAction() before your VM host (activity or fragment) subscribes to it."
            )
            else -> actionsRelay.accept(action)
        }
    }

    /**
     * Enqueues [action] that should be pushed when view model [isResumed].
     *
     * If already resumed action will be pushed immediately.
     */
    @VisibleForTesting(otherwise = PROTECTED)
    fun pushActionWhenResumed(action: A) {
        if (isResumed()) {
            pushAction(action)
        } else {
            enqueuedActions.add(action)
        }
    }

    /**
     * Retrieve the current ViewState, it can be null if no ViewState have been set, which should
     * never happen
     */
    fun getViewState(): VS? = viewStateProcessor.value

    /**
     * Add a disposable to [onClearedDisposables] scope.
     * You should use this method so that the subscription will be dispose in [onCleared]
     */
    @VisibleForTesting(otherwise = PROTECTED)
    fun disposeOnCleared(block: () -> Disposable?) {
        onClearedDisposables += block.invoke()
    }

    /**
     * Add a disposable to [onPauseDisposables] scope.
     * You should use this method so that the subscription will be dispose in [onPause]
     */
    @VisibleForTesting(otherwise = PROTECTED)
    fun disposeOnPause(block: () -> Disposable?) {
        onPauseDisposables += block.invoke()
    }

    /**
     * Add a disposable to [onStopDisposables] scope.
     * You should use this method so that the subscription will be dispose in [onStop]
     */
    @VisibleForTesting(otherwise = PROTECTED)
    fun disposeOnStop(block: () -> Disposable?) {
        onStopDisposables += block.invoke()
    }

    /**
     * Add a disposable to [onDestroyDisposables] scope.
     * You should use this method so that the subscription will be dispose in [onDestroy]
     */
    @VisibleForTesting(otherwise = PROTECTED)
    fun disposeOnDestroy(block: () -> Disposable?) {
        onDestroyDisposables += block.invoke()
    }

    protected fun isResumed(): Boolean = onPauseDisposables.isReady.get()

    /**
     * We use a custom factory to take care of the viewState injection
     */
    abstract class Factory<VS : BaseViewState> : ViewModelProvider.Factory {
        var viewState: VS? = null
    }
}

private const val TAG_LIFECYCLE = "ViewModelLifecycle"
