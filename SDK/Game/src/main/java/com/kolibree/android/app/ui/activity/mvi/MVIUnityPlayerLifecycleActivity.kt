/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.activity.mvi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.kolibree.account.utils.ToothbrushesForProfileUseCase
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.dagger.ApplicationContext
import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.app.lifecycle.LifecycleMonitor
import com.kolibree.android.app.loader.GameService
import com.kolibree.android.app.loader.GameServiceLoaderCallback
import com.kolibree.android.app.loader.entity.DownloadError
import com.kolibree.android.app.loader.entity.Downloading
import com.kolibree.android.app.loader.entity.GameClosed
import com.kolibree.android.app.loader.entity.GameLoaded
import com.kolibree.android.app.loader.entity.GameState
import com.kolibree.android.app.loader.entity.MandatoryUpdateRequired
import com.kolibree.android.app.loader.entity.NotInstalled
import com.kolibree.android.app.loader.entity.Ready
import com.kolibree.android.app.loader.entity.UpdateAvailable
import com.kolibree.android.app.ui.activity.mvi.MVIUnityPlayerLifecycleActivity.Companion.createGameIntent
import com.kolibree.android.app.ui.fragment.BaseUnityGameFragment
import com.kolibree.android.app.ui.fragment.UnityHostActivity
import com.kolibree.android.app.unity.KolibreeUnityPlayer
import com.kolibree.android.app.unity.UnityGame
import com.kolibree.android.app.unity.UnityGameModule
import com.kolibree.android.app.unity.UnityGameResult
import com.kolibree.android.extensions.hasFlags
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.lifecycle.InternalLifecycle
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.BackgroundJobManager
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.game.R
import com.kolibree.game.databinding.ActivityGameMviBinding
import com.kolibree.sdkws.core.IKolibreeConnector
import com.unity3d.player.UnityPlayer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.max
import timber.log.Timber

/**
 * Parent class for [Activity] that host a Unity Game.
 *
 * ## Contract
 * ### Descendants
 * Descendants must fulfil the following conditions
 * - There can only be one descendant per Application. Only one Activity can host a [UnityPlayer]
 * - In manifest, declare `android:launchMode="singleInstance"`. This class must live in its own
 * task an only be killed when user exits the application
 * - Create the intent via createGameIntent (declared in this class)
 *
 * ### Launchers
 * [Activity] that starts a descendant activity must declare `android:launchMode="singleTop"` or
 * `android:launchMode="singleInstance"`.
 * Instead of exiting, [MVIUnityPlayerLifecycleActivity] will invoke startActivity on the launcher.
 * If the class does not declare `singleTop` or `singleInstance`, the main Activity stack will host
 * multiple Launcher Activity instances.
 *
 * In [createGameIntent], Launcher activity is passed as AFTER_FINISH
 *
 * ## Responsibilities
 * It has multiple responsibilities that should be split at some point in the future
 * - Hosts a [KolibreeUnityPlayer]
 * - Interacts with [GameService]
 * - Listens to [KolibreeServiceInteractor] events
 * - Once service is connected, it subscribes to [GameState] stream from [GameService] and reacts
 * to different events
 * - When Activity's stack is restored, determines whether task should be killed or the game resumed
 *
 * ## Lifecycle
 * Given a MainActivity that will start a descendant of this class, the lifecycle goes through the
 * following states
 *
 * t0 - MainActivity is the head of the Task (t1802)
 *
 * ```
 * Hist #1: ActivityRecord{476785e u0 MainActivity t1802}
 * Hist #0: ActivityRecord{88e5738 u0 HomeScreenActivity t1802}
 * ```
 *
 * - t1 - Launch GameMiddlewareActivity, descendant of this class, on a separate Task (t1803)
 * MainActivity stays as head of t1802
 *
 * ```
 * Hist #0: ActivityRecord{4fbb7ca u0 GameMiddlewareActivity t1803}
 * Hist #1: ActivityRecord{476785e u0 MainActivity t1802}
 * Hist #0: ActivityRecord{88e5738 u0 HomeScreenActivity t1802}
 * ```
 *
 * - t2 - Exit GameMiddlewareActivity. It will stay on its own stack because
 * [MVIUnityPlayerLifecycleActivity] invokes startActivity(MainActivity.class) instead of finish
 * MainActivity's task will come to foreground
 *
 * ```
 * Hist #1: ActivityRecord{476785e u0 MainActivity t1802}
 * Hist #0: ActivityRecord{88e5738 u0 HomeScreenActivity t1802}
 * Hist #0: ActivityRecord{4fbb7ca u0 GameMiddlewareActivity t1803}
 * ```
 *
 * - t3 - User presses back. HomeScreenActivity is now Head of task t1802
 *
 * ```
 * Hist #0: ActivityRecord{88e5738 u0 HomeScreenActivity t1802}
 * Hist #0: ActivityRecord{4fbb7ca u0 GameMiddlewareActivity t1803}
 * ```
 *
 * - t4 - User presses back. t1802 finishes and t1803 is brought to foreground
 *
 * For an instant, this is the applications Activity stack
 * ```
 * Hist #0: ActivityRecord{4fbb7ca u0 GameMiddlewareActivity t1803}
 * ```
 *
 * When bringing `t1803` to the foreground, [MVIUnityPlayerLifecycleActivity] detects the activity
 * wasn't launched by the user and finishes instead of resuming, thus the application's Tasks have
 * all finished
 *
 * ### Restoring from background
 *
 * If the user minimizes the app before t2, when GameMiddlewareActivity (t1803) is in foreground,
 * there are two ways to restore the application
 *
 * #### Tap on the application icon or Notification with deep link
 * MainActivity (t1802) will be brought to foreground instead of GameMiddlewareActivity (t1803).
 * The Activity stack will be identical to the one at t2.
 *
 * If the user then launches the same game, GameMiddlewareActivity will resume. In any case, this is
 * a behavior we should improve. GameMiddlewareActivity (t1803) should be brought to foreground
 * after tap on app icon.
 *
 * #### Recents
 *
 * If the user uses recents to navigate to the application, GameMiddlewareActivity (t1803) will be
 * the foreground task.
 *
 * ## Known issues
 * Current implementation only allows to play the game on first launch. For some reason, on 2nd
 * launch of the activity the vibration events are ignored.
 */
@VisibleForApp
abstract class MVIUnityPlayerLifecycleActivity<
    VS : UnityGameViewState,
    VMF : BaseUnityViewModel.Factory<VS>,
    VM : BaseUnityViewModel<VS>
    > : BaseMVIActivity<VS, NoActions, VMF, VM, ActivityGameMviBinding>(),
    InternalLifecycle,
    UnityHostActivity,
    KolibreeServiceInteractor.Listener {

    private val lifecycleMonitor = LifecycleMonitor()

    private val transitionAnimator = TransitionAnimator()

    private lateinit var unityContainer: ViewGroup

    private lateinit var splash: View

    private lateinit var progressBar: ProgressBar

    private lateinit var splashMessage: TextView

    fun nonGameViews(): List<View> = listOf(splash, progressBar)

    /**
     * Flags if Activity was explicitely launched by the user or if we are restoring the Task from
     * background
     *
     * See class documentation for details
     */
    private var explicitLaunch = true

    @JvmField
    @field:[Inject Named(UnityGameModule.ATTACH_UNITY_PLAYER_TO_VIEW)]
    var attachUnityPlayerToView: Boolean = true

    @Inject
    override lateinit var gameService: GameService

    @Inject
    override lateinit var kolibreeServiceInteractor: KolibreeServiceInteractor

    // TODO migrate to MVI and move this to VM
    // https://kolibree.atlassian.net/browse/KLTB002-9389
    @Inject
    internal lateinit var kolibreeConnector: IKolibreeConnector

    // TODO migrate to MVI and move this to VM
    // https://kolibree.atlassian.net/browse/KLTB002-9389
    @Inject
    internal lateinit var toothbrushesForProfileUseCase: ToothbrushesForProfileUseCase

    /*
     * IMPORTANT: don't change the name of this variable; referenced from native code
     */
    @Keep
    private var mUnityPlayer: KolibreeUnityPlayer? = null

    val player: KolibreeUnityPlayer?
        get() = mUnityPlayer

    // If set to true, Unity player will tear down itself during the next onPause().
    // It will also kill the host process - as there is no other way to prevent bugs
    // in Unity player when new activity with the player is launched.
    private var tearDownPlayerInOnPause = false

    override fun getLayoutId(): Int = R.layout.activity_game_mvi

    override fun execute(action: NoActions) {
        // no-op
    }

    /**
     * Delegate of the [onCreate] method
     * @param savedInstanceState previous state
     */
    @CallSuper
    override fun onCreateInternal(savedInstanceState: Bundle?) {
        kolibreeServiceInteractor.setLifecycleOwner(this)

        splash = binding.splashScreen
        splash.background = ContextCompat.getDrawable(this, splashDrawable())
        unityContainer = binding.unityView
        progressBar = binding.progressBar

        binding.splashMessage.setText(splashText())

        Timber.tag(TAG_LIFECYCLE).v("%s - onCreateInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.CREATED)
    }

    /**
     * Delegate of the [onStart] method
     */
    @CallSuper
    override fun onStartInternal() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onStartInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.STARTED)
    }

    /**
     * Delegate of the [onResume] method
     */
    @CallSuper
    override fun onResumeInternal() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onResumeInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.RESUMED)

        if (shouldResumeGame()) {
            /*
            onKolibreeServiceConnected will be invoked
             */
            kolibreeServiceInteractor.addListener(this)
        } else {
            finishAndRemoveTask()
        }

        explicitLaunch = false
    }

    /**
     * Since we are leaving Activity in its own stack, when exiting the application, Android will
     * attempt to bring this activity's stack to foreground going through onStart/onResume lifecycle.
     *
     * We don't want the activity to be displayed again unless:
     * 1. We explicitly started it
     * 2. The user had minimized it and is navigating back; thus, quit was never pressed
     */
    private fun shouldResumeGame(): Boolean {
        return explicitLaunch
    }

    /**
     * Delegate of the [onPause] method
     */
    @CallSuper
    override fun onPauseInternal() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onPauseInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.STARTED)
    }

    /**
     * Delegate of the [onStop] method
     */
    @CallSuper
    override fun onStopInternal() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onStopInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.CREATED)
    }

    /**
     * Delegate of the [onDestroy] method
     */
    @CallSuper
    override fun onDestroyInternal() {
        kolibreeServiceInteractor.removeListener(this)
        Timber.tag(TAG_LIFECYCLE).v("%s - onDestroyInternal", javaClass.simpleName)
        lifecycleMonitor.markTransitionToState(state = Lifecycle.State.DESTROYED)
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // note: ContextWrapper is needed to prevent UnityPlayer
        // of applying immersive mode (fullscreen)
        if (attachUnityPlayerToView) {
            KolibreeUnityPlayer.loaderCallback = GameServiceLoaderCallback(gameService)
            mUnityPlayer = createNewPlayerInstance(ContextWrapper(this))
            UnityPlayer.currentActivity = this
        }
        onCreateInternal(savedInstanceState)
    }

    final override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mUnityPlayer?.let {
            unityContainer.addView(it)
            it.requestFocus()
        }
    }

    final override fun onStart() {
        super.onStart()
        ifUnityGameIsOn(
            execute = {
                val gameRunsInLandscape =
                    (supportFragmentManager.fragments.firstOrNull { fragment ->
                        fragment is BaseUnityGameFragment
                    } as? BaseUnityGameFragment)?.game()?.runsInLandscape == true
                requestLandscapeOrientation(gameRunsInLandscape)
            },
            otherwise = { onStartInternal() }
        )
    }

    final override fun onResume() {
        super.onResume()
        resumePlayer()
        ifUnityGameIsOn(
            execute = { /* reserved */ },
            otherwise = { onResumeInternal() }
        )
    }

    final override fun onPause() {
        ifUnityGameIsOn(
            execute = { /* reserved */ },
            otherwise = { onPauseInternal() }
        )
        super.onPause()
        pausePlayer()
        if (tearDownPlayerInOnPause) {
            destroyPlayer()
        }
    }

    final override fun onStop() {
        ifUnityGameIsOn(
            execute = { /* reserved */ },
            otherwise = { onStopInternal() }
        )
        super.onStop()
    }

    final override fun onDestroy() {
        onDestroyInternal()
        super.onDestroy()
        UnityPlayer.currentActivity = null
        destroyPlayer()
    }

    // Low Memory Unity
    final override fun onLowMemory() {
        super.onLowMemory()
        mUnityPlayer?.lowMemory()
    }

    // Trim Memory Unity
    final override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            mUnityPlayer?.lowMemory()
        }
    }

    // This ensures the layout will be correct.
    final override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mUnityPlayer?.configurationChanged(newConfig)
    }

    // Notify Unity of the focus change.
    final override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        mUnityPlayer?.windowFocusChanged(hasFocus)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent)

        explicitLaunch = true
    }

    protected open fun createNewPlayerInstance(contextWrapper: ContextWrapper): KolibreeUnityPlayer {
        return KolibreeUnityPlayer(contextWrapper)
    }

    private fun requestLandscapeOrientation(gameRunsInLandscape: Boolean) {
        requestedOrientation = if (gameRunsInLandscape)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun resumePlayer() {
        mUnityPlayer?.let {
            UnityPlayer.currentActivity = this
            it.resume()
        }
    }

    private fun pausePlayer() {
        mUnityPlayer?.let {
            UnityPlayer.currentActivity = null
            mUnityPlayer?.pause()
        }
    }

    private fun destroyPlayer() {
        mUnityPlayer?.let {
            stopService(Intent(this, KolibreeService::class.java))
            it.performKill = isTaskRoot

            if (it.performKill) {
                viewModel.onTearingDownProcess()
            }

            unityContainer.removeView(it)
            it.tearDownScheduled = false

            it.destroy()
        }
        mUnityPlayer = null
    }

    protected fun restoreLauncherTask() {
        /*
         That's the only way we may leave without freeze or crash - Unity :/

         MVIUnityPlayerLifecycleActivity's [UnityActivity] instance will stay in the Activity stack.

         After activityAfterTerminate is popped, UnityActivity will finish itself unless it's been
         explicitely launched

         See class documentation for details
         */
        startActivity(Intent(this, activityAfterTerminate()).also {
            beforeSendResultIntent(it)
        })
    }

    @CallSuper
    protected open fun beforeSendResultIntent(intent: Intent) {
        // no-op
    }

    final override fun finish() {
        tearDownUnityPlayer()
        super.finish()
    }

    override fun finishAndRemoveTask() {
        tearDownUnityPlayer()
        super.finishAndRemoveTask()
    }

    final override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        tearDownUnityPlayerIfNeeded(intent)
    }

    final override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        tearDownUnityPlayerIfNeeded(intent)
    }

    final override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        tearDownUnityPlayerIfNeeded(intent)
    }

    final override fun checkCallingOrSelfPermission(permission: String): Int {
        /*
        UGLY HACK!!

        Always report to Unity that we have the permission. For now we don't seem to use it so it's not
        a problem, but if some edge case does use it, the app will crash.

        Or if Unity changes something in their side, we can also have problems.

        This was requested here https://jira.kolibree.com/browse/KLTB002-2131

        See https://answers.unity.com/answers/1472450/view.html for explanation
        */
        return PackageManager.PERMISSION_GRANTED
    }

    final override fun dispatchKeyEvent(event: KeyEvent) =
        if (event.action == KeyEvent.ACTION_MULTIPLE) {
            ifUnityGameIsOn(
                execute = { player -> player.dispatchKeyEvent(event) },
                otherwise = { super.dispatchKeyEvent(event) }
            )
        } else {
            super.dispatchKeyEvent(event)
        }

    final override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && allowOnBackPressedOverride()) {
            onBackPressed()
            true
        } else {
            ifUnityGameIsOn(
                execute = { player -> player.onKeyUp(keyCode, event) },
                otherwise = { super.onKeyUp(keyCode, event) }
            )
        }
    }

    protected open fun allowOnBackPressedOverride() = false

    final override fun onKeyDown(keyCode: Int, event: KeyEvent) = ifUnityGameIsOn(
        execute = { player -> player.onKeyDown(keyCode, event) },
        otherwise = { super.onKeyDown(keyCode, event) }
    )

    final override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent) =
        ifUnityGameIsOn(
            execute = { player -> player.onKeyMultiple(keyCode, repeatCount, event) },
            otherwise = { super.onKeyMultiple(keyCode, repeatCount, event) }
        )

    final override fun onKeyLongPress(keyCode: Int, event: KeyEvent) = ifUnityGameIsOn(
        execute = { player -> player.onKeyLongPress(keyCode, event) },
        otherwise = { super.onKeyLongPress(keyCode, event) }
    )

    final override fun onTouchEvent(event: MotionEvent) = ifUnityGameIsOn(
        execute = { player -> player.onTouchEvent(event) },
        otherwise = { super.onTouchEvent(event) }
    )

    final override fun onGenericMotionEvent(event: MotionEvent) = ifUnityGameIsOn(
        execute = { player -> player.onGenericMotionEvent(event) },
        otherwise = { super.onGenericMotionEvent(event) }
    )

    /**
     * When launching Unity game, we need to emulate the state in which host activity goes to stopped state.
     * This way we stay compatible with old, activity-based games.
     */
    private fun emulateLifecyclePauseAndStop() {
        if (!isFinishing) {
            val lifecycleRegistry = super.getLifecycle() as? LifecycleRegistry
            lifecycleRegistry?.currentState = Lifecycle.State.STARTED
            onPauseInternal()
            lifecycleRegistry?.currentState = Lifecycle.State.CREATED
            onStopInternal()
        }
    }

    /**
     * Similarly, when Unity game finishes, we need to emulate activity going back to resumed state.
     * This way we stay compatible with old, activity-based games.
     */
    private fun emulateLifecycleStartAndResume() {
        if (!isFinishing) {
            val lifecycleRegistry = super.getLifecycle() as? LifecycleRegistry
            lifecycleRegistry?.currentState = Lifecycle.State.STARTED
            onStartInternal()
            lifecycleRegistry?.currentState = Lifecycle.State.RESUMED
            onResumeInternal()
        }
    }

    private fun tearDownUnityPlayerIfNeeded(intent: Intent) {
        if (intent.hasFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)) {
            // New activity will tear us down, we have to react.
            // We have to tear down the player, otherwise the app will crash
            tearDownUnityPlayer()
        }
    }

    private fun tearDownUnityPlayer() {
        mUnityPlayer?.let {
            synchronized(it) {
                if (it.tearDownScheduled) {
                    Timber.w("Teardown already scheduled! Skipping")
                    return
                }

                Timber.d("Scheduling UnityPlayer teardown for state: ${lifecycleMonitor.lastLifecycleState}")

                when {
                    // If the activity is resumed, teardown will happen in onPause()
                    lifecycleMonitor.lastLifecycleState.isAtLeast(Lifecycle.State.RESUMED) ->
                        tearDownPlayerInOnPause = true
                    // If we are started - we will finish gracefully in the normal lifecycle.
                    lifecycleMonitor.lastLifecycleState.isAtLeast(Lifecycle.State.STARTED) -> {
                        // no-op
                    }
                    // If we are created and we are about to close, we need to perform full teardown now.
                    // Otherwise player will crash the app. This is usually the case when task is being
                    // cleared and activity didn't have time to reactivate itself.
                    lifecycleMonitor.lastLifecycleState.isAtLeast(Lifecycle.State.CREATED) ->
                        performImmediateTearDown()
                }

                it.tearDownScheduled = true
            }
        }
    }

    private fun performImmediateTearDown() {
        pausePlayer()

        destroyPlayer()
    }

    /**
     * Checks if conditions for game launch are met and launches the game if they are.
     * Result of the operation is returned in [result] if provided.
     *
     * @param gameFragment host fragment of Unity game
     * @param result
     */
    @SuppressLint("ExperimentalClassUse")
    private fun prepareAndLaunch(
        gameFragment: BaseUnityGameFragment,
        result: ((Boolean) -> Unit)
    ) = ifGameCanBeStarted(result) { chosenConnection ->
        gameFragment.setChosenConnection(chosenConnection)

        checkBundlesAndLaunch(gameFragment, result)
    }

    private fun checkBundlesAndLaunch(
        gameFragment: BaseUnityGameFragment,
        result: (Boolean) -> Unit
    ) {
        disposeOnStop {
            gameService.stateObservableForGame(gameFragment.game())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ gameState ->
                    Timber.tag(TAG).d("Downloading state: $gameState")
                    when (gameState) {
                        is Ready, is UpdateAvailable -> {
                            splash.visibility = View.GONE
                            unityContainer.visibility = View.VISIBLE
                            startUnityGame(gameFragment)
                            result.invoke(true)
                        }
                        is Downloading -> notifyProgress(gameState)
                        is NotInstalled, is MandatoryUpdateRequired ->
                            try {
                                gameService.download(gameFragment.game())
                            } catch (t: Throwable) {
                                Timber.e(t)

                                finishOnError()
                            }
                        is DownloadError -> {
                            Timber.e("DownloadError! $gameState")
                            finishOnError()
                            result.invoke(false)
                        }
                        is GameLoaded -> Timber.d("Game ${gameState.game} was loaded")
                        is GameClosed -> onGameClosed(gameState)
                        else -> {
                            Timber.e("Something went wrong! $gameState")
                            result.invoke(false)
                        }
                    }
                }, {
                    Timber.e("Something went wrong!")
                    result.invoke(false)
                })
        }
    }

    private fun onGameClosed(gameState: GameState) {
        Timber.d("Game ${gameState.game} was closed")

        // I don't know what setting success = false implies
        finishUnityGameInternal(UnityGameResult(unityGameFragment().game(), false, null))
    }

    private fun finishOnError() {
        showSomethingWentWrong {
            emulateLifecycleStartAndResume()

            innerOnUnityGameFinished(UnityGameResult(unityGameFragment().game(), false, null))
        }
    }

    private fun notifyProgress(gameState: Downloading) {
        splash.visibility = View.VISIBLE
        unityContainer.visibility = View.INVISIBLE
        /*
        The download taes a while to start, and if we set 0%, the progressbar isn't visible
         */
        progressBar.progress = max(1, gameState.progress)
    }

    private fun prepareAndLaunch(gameFragment: BaseUnityGameFragment) =
        prepareAndLaunch(gameFragment) {
            // no-op
        }

    private inline fun ifGameCanBeStarted(
        noinline result: ((Boolean) -> Unit)?,
        crossinline execute: (KLTBConnection) -> Unit
    ) {
        val currentProfile = kolibreeConnector.currentProfile
        if (currentProfile == null) {
            Timber.e("No profile available, game cannot be started!")
            result?.invoke(false)
            return
        }

        val knownConnections = getAvailableToothbrushesForProfile()

        if (knownConnections.isNullOrEmpty()) {
            Timber.e("No connections available, game cannot be started!")
            result?.invoke(false)
            return
        }

        // TODO this need to be implemented in VM
        //  https://kolibree.atlassian.net/browse/KLTB002-9389
        val firstConnection =
            kolibreeServiceInteractor.service?.knownConnections?.forToothbrush(knownConnections[0])
        if (knownConnections.size == 1 && firstConnection != null) {
            execute(firstConnection)
        } else {
            chooseConnection(execute)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun getAvailableToothbrushesForProfile(): List<AccountToothbrush> {
        return try {
            toothbrushesForProfileUseCase
                .currentProfileAccountToothbrushesOnceAndStream()
                .take(1)
                .blockingSingle()
        } catch (e: Exception) {
            Timber.e(e)
            emptyList()
        }
    }

    private inline fun chooseConnection(crossinline execute: (KLTBConnection) -> Unit) {
        // For now we just pick the first available connection
        // TODO add new toothbrush picker dialog
        //  https://kolibree.atlassian.net/browse/KLTB002-11452
        kolibreeServiceInteractor.service?.knownConnections?.let { connections ->
            if (connections.isNotEmpty()) {
                execute(connections[0])
            }
        }
    }

    private fun startUnityGame(fragment: BaseUnityGameFragment) {
        ifUnityGameIsOn(
            execute = { Timber.w("Cannot launch Unity game from $fragment, another game is already in progress!") },
            otherwise = {
                if (!UnityGame.isSupportedBySystem()) {
                    Timber.e("Cannot launch Unity game for this system")
                    return@ifUnityGameIsOn
                }

                mUnityPlayer?.let {
                    transitionAnimator.showGame(
                        executeAfterAnimation = {
                            emulateLifecyclePauseAndStop()

                            requestLandscapeOrientation(fragment.game().runsInLandscape)

                            supportFragmentManager
                                .beginTransaction()
                                .add(unityContainer.id, fragment)
                                .addToBackStack(fragment::class.java.canonicalName)
                                .commit()
                        }
                    )
                } ?: FailEarly.fail("UnityPlayer is null")
            }
        )
    }

    override fun finishUnityGame(fragment: BaseUnityGameFragment, result: UnityGameResult<*>) {
        ifUnityGameIsOn(
            execute = { finishUnityGameInternal(result) },
            otherwise = { Timber.w("Cannot finish Unity game from $fragment, there is no game in progress!") }
        )
    }

    private fun finishUnityGameInternal(result: UnityGameResult<*>) {
        transitionAnimator.hideGame(
            executeBeforeAnimation = {
                supportFragmentManager.popBackStackImmediate()

                if (result.game.runsInLandscape) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            },
            executeAfterAnimation = {
                innerOnUnityGameFinished(result)
            }
        )
    }

    protected fun <T> ifUnityGameIsOn(
        execute: (UnityPlayer) -> T,
        otherwise: () -> T
    ): T =
        mUnityPlayer?.takeIf {
            supportFragmentManager.fragments.any { fragment -> fragment is BaseUnityGameFragment }
        }?.let { execute(it) } ?: otherwise()

    final override fun onKolibreeServiceConnected(service: KolibreeService) {
        prepareAndLaunch(unityGameFragment())
    }

    final override fun onKolibreeServiceDisconnected() {
        // no-op for now
    }

    private fun innerOnUnityGameFinished(result: UnityGameResult<*>) {
        /*
        As soon as game finishes, don't listen to KolibreeService

        If needed, it will be restored in onResume
         */
        kolibreeServiceInteractor.removeListener(this)

        onUnityGameFinished(result)

        restoreLauncherTask()
    }

    private fun activityAfterTerminate(): Class<AppCompatActivity> {
        val clazz = intent.extras?.getSerializable(ACTIVITY_TO_OPEN)

        @Suppress("UNCHECKED_CAST")
        return clazz as? Class<AppCompatActivity>
            ?: throw IllegalStateException("Expected Class<AppCompatActivity>, got $clazz")
    }

    protected abstract fun showSomethingWentWrong(actionAfterAccept: () -> Unit)

    /**
     * Callback that returns result of finished Unity game.
     *
     * This is invoked just before terminating the activity
     *
     * @param result result object from game
     */
    protected abstract fun onUnityGameFinished(result: UnityGameResult<*>)

    @DrawableRes
    protected abstract fun splashDrawable(): Int

    @StringRes
    protected abstract fun splashText(): Int

    abstract fun unityGameFragment(): BaseUnityGameFragment

    @VisibleForApp
    protected companion object {
        private const val TAG_LIFECYCLE = "UnityMVILifecycle"

        private const val TAG = "UnityMVI"

        private const val ACTIVITY_TO_OPEN = "activity_to_open"

        /**
         * Descendants of [MVIUnityPlayerLifecycleActivity] should use this method to create
         * [Intent]
         */
        fun <AFTER_FINISH : AppCompatActivity,
            HOST : MVIUnityPlayerLifecycleActivity<*, *, *>> Context.createGameIntent(
                hostActivity: Class<HOST>,
                activityAfterGameFinish: Class<AFTER_FINISH>
            ): Intent = Intent(this, hostActivity).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(ACTIVITY_TO_OPEN, activityAfterGameFinish)
        }
    }

    /**
     * Responsible for showing/hiding Unity game fragments + management of view flags.
     * This way game fragment doesn't contain any logic that is not strictly related to the game.
     */
    private inner class TransitionAnimator {

        private var preGamePixelFormat: Int = PixelFormat.OPAQUE

        private var preGameSystemUiVisibility: Int = 0

        inline fun showGame(
            crossinline executeBeforeAnimation: () -> Unit = {},
            crossinline executeAfterAnimation: () -> Unit = {}
        ) {
            val executionBlock = {
                prepareWindowForGame()
                nonGameViews().forEach { view -> view.visibility = View.INVISIBLE }
                player?.view?.visibility = View.VISIBLE
                executeAfterAnimation()
            }

            executeBeforeAnimation()
            executionBlock()
        }

        inline fun hideGame(
            crossinline executeBeforeAnimation: () -> Unit = {},
            crossinline executeAfterAnimation: () -> Unit = {}
        ) {
            val secondExecutionBlock = {
                unityContainer.visibility = View.INVISIBLE
                executeAfterAnimation()
            }

            executeBeforeAnimation()
            secondExecutionBlock()
        }

        private fun prepareWindowForGame() {
            player?.view?.keepScreenOn = true

            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

            preGameSystemUiVisibility = window.decorView.systemUiVisibility

            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

            window.decorView.systemUiVisibility = flags
            window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    window.decorView.systemUiVisibility = flags
                }
            }

            preGamePixelFormat = window.attributes.format
            window.setFormat(PixelFormat.RGBX_8888) // <--- This makes xperia play happy
        }
    }
}

@VisibleForApp
abstract class BaseUnityViewModel<VS : UnityGameViewState>(
    viewState: VS,
    private val applicationContext: ApplicationContext,
    private val backgroundJobManager: Set<BackgroundJobManager>
) : BaseViewModel<VS, NoActions>(baseViewState = viewState) {

    fun onTearingDownProcess() {
        /**
         * Schedule offline brushings periodic background extractor before killing the process
         *
         * See https://github.com/kolibree-git/android-monorepo/pull/295
         */
        backgroundJobManager.forEach { it.scheduleJob(applicationContext) }
    }

    /**
     * We use a custom factory to take care of the viewState injection
     */
    @VisibleForApp
    abstract class Factory<VS : UnityGameViewState> constructor(
        protected val applicationContext: ApplicationContext,
        protected val backgroundJobManager: Set<BackgroundJobManager>
    ) : BaseViewModel.Factory<VS>()
}

@VisibleForApp
interface UnityGameViewState : BaseViewState {
    val progressVisible: Boolean
}

private fun List<KLTBConnection>.forToothbrush(toothbrush: AccountToothbrush): KLTBConnection? {
    return firstOrNull {
        it.toothbrush().model == toothbrush.model && it.toothbrush().mac == toothbrush.mac
    } ?: run {
        FailEarly.fail("No connection for ${toothbrush.model} ${toothbrush.mac}")
        null
    }
}
