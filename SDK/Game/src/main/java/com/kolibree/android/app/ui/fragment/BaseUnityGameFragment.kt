/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.app.lifecycle.LifecycleMonitor
import com.kolibree.android.app.loader.GameService
import com.kolibree.android.app.ui.activity.UnityPlayerLifecycleActivity
import com.kolibree.android.app.ui.activity.mvi.MVIUnityPlayerLifecycleActivity
import com.kolibree.android.app.unity.UnityGame
import com.kolibree.android.app.unity.UnityGameResult
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.lifecycle.InternalLifecycle
import com.kolibree.android.sdk.connection.KLTBConnection

/**
 * Base class for fragments hosting Unity games.
 *
 * Since we can have only one player in the active application task, Unity fragments doesn't have their own UI.
 * Instead, they delegate this to host activity, which must be of type [UnityPlayerLifecycleActivity]. From there,
 * they receive Unity player where they're executed.
 *
 * @author lookashc
 */
@Suppress("TooManyFunctions")
abstract class BaseUnityGameFragment : BaseGameFragment(), InternalLifecycle {

    private val lifecycleMonitor = LifecycleMonitor()

    override val activity: MVIUnityPlayerLifecycleActivity<*, *, *>?
        get() {
            validateActivity()
            return super.activity as? MVIUnityPlayerLifecycleActivity<*, *, *>
        }

    protected fun unityHostActivity() = activity as UnityHostActivity?

    /**
     * ID of the game this fragments hosts
     */
    abstract fun game(): UnityGame

    private val gameService: GameService?
        get() = unityHostActivity()?.gameService

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Allow descendants to setup themselves
        onCreateInternal(savedInstanceState)

        startGame()
    }

    final override fun onStart() {
        super.onStart()
        onStartInternal()
        onVisible()
    }

    final override fun onResume() {
        super.onResume()
        onResumeInternal()
    }

    final override fun onPause() {
        onPauseInternal()
        super.onPause()
    }

    final override fun onStop() {
        onStopInternal()
        super.onStop()
    }

    final override fun onDestroy() {
        onDestroyInternal()
        super.onDestroy()
    }

    // Unity games doesn't have their own UI, we need to close this method from subclasses.
    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    // Unity games doesn't have their own UI, we need to close this method from subclasses.
    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    // Unity games doesn't have their own UI, we need to close this method from subclasses.
    final override fun setupLayout() {
        // no-op
    }

    /**
     * Delegate of the [onCreate] method
     * @param savedInstanceState previous state
     */
    @CallSuper
    override fun onCreateInternal(savedInstanceState: Bundle?) {
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.CREATED)
    }

    /**
     * Delegate of the [onStart] method
     */
    @CallSuper
    override fun onStartInternal() {
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.STARTED)
    }

    /**
     * Delegate of the [onResume] method
     */
    @CallSuper
    override fun onResumeInternal() {
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.RESUMED)
    }

    /**
     * Delegate of the [onPause] method
     */
    @CallSuper
    override fun onPauseInternal() {
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.STARTED)
    }

    /**
     * Delegate of the [onStop] method
     */
    @CallSuper
    override fun onStopInternal() {
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.CREATED)
    }

    /**
     * Delegate of the [onDestroy] method
     */
    @CallSuper
    override fun onDestroyInternal() {
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.DESTROYED)
    }

    private fun startGame() {
        gameService?.start(game())
    }

    /**
     * Calls thi game to finish - fragment will be removed and host activity will receive posted result.
     * @param result result of the game + extra data if required
     */
    protected fun finishGame(result: UnityGameResult<*>) {
        gameService?.stop(game())
        unityHostActivity()?.finishUnityGame(this, result)
    }

    private fun validateActivity() {
        super.activity?.let {
            FailEarly.failInConditionMet(
                condition = it !is UnityHostActivity,
                message = "Unity game requires UnityHostActivity as a host in order to run, " +
                    "but found ${super.activity?.javaClass}, please check your architecture!"
            ) {
                it.finish()
            }
        }
    }

    abstract fun setChosenConnection(chosenConnection: KLTBConnection)
}

@Keep
interface UnityHostActivity {
    fun finishUnityGame(fragment: BaseUnityGameFragment, result: UnityGameResult<*>)

    val gameService: GameService

    val kolibreeServiceInteractor: KolibreeServiceInteractor
}
